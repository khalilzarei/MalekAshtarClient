package com.khz.malekclient.ui.chat

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.khz.malekclient.MalekClientApp
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.core.util.DateUtils
import com.khz.malekclient.core.util.toPersianDigits
import com.khz.malekclient.domain.model.ChatMessage
import com.khz.malekclient.domain.model.ChatRoom
import com.khz.malekclient.domain.model.ChatRoomUser
import com.khz.malekclient.ui.components.AvatarView
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.milliseconds

private fun chatRoleLabel(role: String?): String {
    return when (role?.trim()?.lowercase(Locale.ROOT)) {
        "player", "athlete" -> "بازیکن"
        "admin", "administrator", "manager", "owner" -> "مدیر"
        "coach", "trainer" -> "مربی"
        "guardian", "parent" -> "سرپرست"
        "teacher" -> "مربی آموزشی"
        "accountant" -> "حسابدار"
        "staff" -> "کادر اجرایی"
        "director" -> "مدیر فنی"
        "user" -> "کاربر"
        null, "" -> "کاربر"
        else -> role.trim()
    }
}

private enum class MessageCheckState {
    NONE,
    SENT,
    DELIVERED,
    READ
}

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    roomId: Int? = null,
    targetUserId: Int? = null,
    initialTitle: String? = null
) {
    val context = LocalContext.current
    val container = (context.applicationContext as MalekClientApp).container
    val chatRepo = container.chatRepository
    val sessionManager = container.sessionManager
    val scope = rememberCoroutineScope()

    var currentRoomId by remember { mutableStateOf(roomId) }
    var room by remember { mutableStateOf<ChatRoom?>(null) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentUserId by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        currentUserId = sessionManager.userId.first()?.toIntOrNull()
    }

    suspend fun loadRoom() {
        error = null
        val rid = currentRoomId
        if (rid == null) {
            if (targetUserId == null || targetUserId <= 0) {
                error = "شناسه کاربر مقصد مشخص نیست"
                return
            }
            when (val result = chatRepo.getOrCreatePrivateRoom(targetUserId)) {
                is NetworkResult.Success -> {
                    room = result.data
                    currentRoomId = result.data.id
                }
                is NetworkResult.Error -> error = result.message
                is NetworkResult.Loading -> Unit
            }
            return
        }
        // تلاش برای گرفتن اتاق از لیست، اگر نبود از getRoom مستقیم اگر موجود باشد
        when (val result = chatRepo.rooms()) {
            is NetworkResult.Success -> {
                val found = result.data.firstOrNull { it.id == rid }
                if (found != null) {
                    room = found
                } else {
                    // fallback: سعی کن مستقیم اتاق را بگیری اگر API دارد، در غیر این صورت خطا
                    room = null
                    error = "اطلاعات گفتگو پیدا نشد"
                }
            }
            is NetworkResult.Error -> error = result.message
            is NetworkResult.Loading -> Unit
        }
    }

    suspend fun loadMessages() {
        val rid = currentRoomId ?: return
        when (val result = chatRepo.getMessages(roomId = rid, limit = 50)) {
            is NetworkResult.Success -> {
                messages = result.data
                result.data.maxOfOrNull { it.id }?.let { lastId ->
                    chatRepo.markAsRead(rid, lastId)
                }
            }
            is NetworkResult.Error -> {
                if (messages.isEmpty()) error = result.message
            }
            is NetworkResult.Loading -> Unit
        }
    }

    LaunchedEffect(roomId, targetUserId) {
        loading = true
        loadRoom()
        loadMessages()
        loading = false
    }

    LaunchedEffect(currentRoomId) {
        while (isActive) {
            delay(3000.milliseconds)
            val rid = currentRoomId ?: continue
            when (val result = chatRepo.getMessages(roomId = rid, limit = 50)) {
                is NetworkResult.Success -> {
                    val merged = (messages + result.data).distinctBy { it.id }.sortedBy { it.id }
                    if (merged != messages) {
                        messages = merged
                        result.data.maxOfOrNull { it.id }?.let { lastId ->
                            chatRepo.markAsRead(rid, lastId)
                        }
                    }
                }
                is NetworkResult.Error -> Unit
                is NetworkResult.Loading -> Unit
            }

            /*
             * به‌روزرسانی اعضای روم (lastReadMessageId)
             * تا تیک سبز ✓✓ به‌محض خواندنِ طرف مقابل نمایش داده شود.
             */
            val roomsResult = chatRepo.rooms()
            if (roomsResult is NetworkResult.Success) {
                roomsResult.data.firstOrNull { it.id == rid }
                    ?.let { room = it }
            }
        }
    }

    val currentRoom = room
    val otherUser: ChatRoomUser? = currentRoom?.users?.firstOrNull { it.id != currentUserId }

    /*
     * اعضای دیگر روم (بدون کاربر لاگین‌شده) — برای محاسبه‌ی تیک‌ها:
     *  ✓    خاکستری = ارسال‌شده (هنوز به اپِ طرف مقابل نرسیده)
     *  ✓✓   خاکستری = تحویل‌شده (اپِ طرف مقابل دریافت کرده)
     *  ✓✓   سبز     = خوانده‌شده (آخرین پیام خوانده‌ی طرف ≥ پیام ما)
     */
    val otherMembers: List<ChatRoomUser> = currentRoom?.users
        ?.filter { it.id != currentUserId }
        .orEmpty()

    fun checkState(msg: ChatMessage): MessageCheckState {
        val readByAll = otherMembers.isNotEmpty() &&
                otherMembers.all { it.lastReadMessageId >= msg.id }
        val delivered = !msg.deliveredAt.isNullOrBlank()
        return when {
            readByAll -> MessageCheckState.READ
            delivered -> MessageCheckState.DELIVERED
            else -> MessageCheckState.SENT
        }
    }

    val roomTitle = if (currentRoom?.isGroup == true) {
        currentRoom.title.takeIf { it.isNotBlank() } ?: initialTitle ?: "گروه"
    } else {
        otherUser?.fullName?.takeIf { it.isNotBlank() }
                ?: currentRoom?.title?.takeIf { it.isNotBlank() }
                ?: initialTitle ?: "گفتگو"
    }

    val roomImage = if (currentRoom?.isGroup == true) {
        currentRoom.image?.takeIf { it.isNotBlank() }
    } else {
        otherUser?.avatar?.takeIf { it.isNotBlank() } ?: currentRoom?.image?.takeIf { it.isNotBlank() }
    }

    val roomSubtitle = if (currentRoom?.isGroup == true) {
        "${currentRoom.users.size} عضو"
    } else {
        otherUser?.role
            ?.let(::chatRoleLabel)
                ?: "گفتگوی خصوصی"
    }

    /*
     * آیتم‌های لیست: پیام‌ها + خط جداکننده‌ی تاریخ (امروز/دیروز/تاریخ)
     * — مثل همه‌ی برنامه‌های چت.
     * زمان‌های سرور UTC است و هنگام نمایش به timezone دستگاه تبدیل می‌شود.
     */
    val chatItems: List<ChatListItem> = remember(messages) {
        buildList {
            var previousDayStart: Long? = null
            for (message in messages) {
                val millis = parseUtcToMillis(message.createdAt)
                val dayStart = millis?.let { localDayStart(it) }
                if (dayStart != null && dayStart != previousDayStart) {
                    add(ChatListItem.DayHeader(dayStart, dayLabel(dayStart)))
                    previousDayStart = dayStart
                }
                add(ChatListItem.Bubble(message))
            }
        }
    }

    TelegramChatBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                val phone = otherUser?.mobile?.trim()
                TelegramChatTopBar(
                    title = roomTitle,
                    subtitle = roomSubtitle,
                    avatarUrl = roomImage,
                    onBack = onBack,
                    showCall = currentRoom?.isGroup == false,
                    callEnabled = !phone.isNullOrBlank(),
                    onCall = {
                        if (phone.isNullOrBlank()) {
                            Toast.makeText(
                                context,
                                "شماره تماس ثبت نشده است",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            try {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:$phone")
                                    }
                                )
                            } catch (_: Exception) {
                                Toast.makeText(
                                    context,
                                    "برنامه تماس در دسترس نیست",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    showLock = currentRoom?.isLocked == true
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = GoldPrimary)
                            }
                        }
                        error != null && messages.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = error ?: "خطا در دریافت اطلاعات",
                                        color = Color(0xFFFF8A80),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            scope.launch {
                                                loading = true
                                                loadRoom()
                                                loadMessages()
                                                loading = false
                                            }
                                        }
                                    ) {
                                        Text(text = "تلاش مجدد", color = GoldPrimary)
                                    }
                                }
                            }
                        }
                        messages.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "هنوز پیامی رد و بدل نشده",
                                        color = Color.White.copy(alpha = 0.6f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "اولین پیام را ارسال کنید",
                                        color = Color.White.copy(alpha = 0.4f),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(
                                    items = chatItems,
                                    key = { item ->
                                        when (item) {
                                            is ChatListItem.Bubble -> "m" + item.message.id
                                            is ChatListItem.DayHeader -> "d" + item.dayStart
                                        }
                                    }
                                ) { item ->
                                    when (item) {
                                        is ChatListItem.DayHeader ->
                                            DateSeparatorHeader(label = item.label)

                                        is ChatListItem.Bubble -> {
                                            val msg = item.message
                                            val mine = msg.senderId == currentUserId
                                            val checks =
                                                if (mine) checkState(msg) else MessageCheckState.NONE

                                            MessageBubble(
                                                message = msg,
                                                isMine = mine,
                                                timeLabel = formatTimeLocal(
                                                    parseUtcToMillis(msg.createdAt)
                                                ),
                                                checkState = checks
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                when {
                    currentRoomId == null -> Unit
                    currentRoom?.isLocked == true -> {
                        GlassCard3D(
                            modifier = Modifier.fillMaxWidth().padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "قفل",
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "این گفتگو توسط مدیر قفل شده است",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    else -> {
                        MessageInputBar(
                            text = messageText,
                            onTextChange = { messageText = it },
                            sending = sending,
                            onSend = {
                                if (messageText.isBlank() || sending) return@MessageInputBar
                                val rid = currentRoomId ?: return@MessageInputBar
                                val text = messageText.trim()
                                scope.launch {
                                    sending = true
                                    try {
                                        when (val result = chatRepo.sendMessage(rid, text)) {
                                            is NetworkResult.Success -> {
                                                messages = (messages + result.data).distinctBy { it.id }
                                                messageText = ""
                                            }
                                            is NetworkResult.Error -> {
                                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                            }
                                            is NetworkResult.Loading -> Unit
                                        }
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "ارسال پیام ناموفق بود", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        sending = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(chatItems.size) {
        if (chatItems.isNotEmpty()) {
            try {
                listState.animateScrollToItem(chatItems.size - 1)
            } catch (_: Exception) {
                Unit
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    timeLabel: String = "",
    checkState: MessageCheckState = MessageCheckState.NONE
) {
    val shape = if (isMine) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    val bgBrush = if (isMine) {
        Brush.linearGradient(
            listOf(
                GoldPrimary.copy(alpha = 0.85f),
                GoldPrimary.copy(alpha = 0.55f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    }

    // LTR فقط برای جای‌گذاری قطعی حباب‌ها: ارسالی راست، دریافتی چپ.
    // محتوای داخل حباب همچنان RTL باقی می‌ماند.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
        ) {
            if (!isMine) {
                AvatarView(
                    name = message.senderName ?: "?",
                    avatarUrl = message.senderAvatar,
                    size = 32.dp,
                    accentColor = Color(0xFF4FC3F7)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .background(brush = bgBrush, shape = shape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column {
                        Text(
                            text = message.body ?: "",
                            color = if (isMine) Color(0xFF1A0533) else Color.White,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timeLabel,
                                color = if (isMine) {
                                    Color(0xFF1A0533).copy(alpha = 0.7f)
                                } else {
                                    Color.White.copy(alpha = 0.5f)
                                },
                                style = MaterialTheme.typography.labelSmall
                            )

                            if (checkState != MessageCheckState.NONE) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (checkState == MessageCheckState.SENT) {
                                        Icons.Filled.Done
                                    } else {
                                        Icons.Filled.DoneAll
                                    },
                                    contentDescription = when (checkState) {
                                        MessageCheckState.SENT -> "ارسال‌شده"
                                        MessageCheckState.DELIVERED -> "تحویل‌شده"
                                        MessageCheckState.READ -> "خوانده‌شده"
                                        MessageCheckState.NONE -> null
                                    },
                                    tint = if (checkState == MessageCheckState.READ) {
                                        Color(0xFF007A33)
                                    } else {
                                        Color(0xFF1A0533).copy(alpha = 0.55f)
                                    },
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    sending: Boolean,
    onSend: () -> Unit
) {
    var emojiOpen by remember { mutableStateOf(false) }
    val emojis = remember {
        listOf(
            "😀", "😂", "😍", "🥰", "😎", "😊", "😉", "😢", "😡",
            "👍", "👏", "🙏", "🔥", "❤️", "🎉",
            "⚽", "🏀", "🏈", "⚾", "🎾", "🏐", "🏉", "🥏",
            "🎱", "🏓", "🏸", "🥅", "🏒", "🏑", "🥍", "🏏",
            "⛳", "🏹", "🎣", "🤿", "🥊", "🥋", "🛹", "🛼",
            "⛸️", "🏂", "🪂", "🏋️‍♂️", "🤼‍♂️", "🤸‍♂️", "🤾‍♂️",
            "🏌️‍♂️", "🏇", "🚴‍♂️", "🚵‍♂️", "🏊‍♂️", "🧗‍♂️"
        )
    }
    val inputShape = RoundedCornerShape(26.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        if (emojiOpen) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xE016232D))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.09f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onTextChange(text + emoji)
                                    emojiOpen = false
                                }
                                .padding(7.dp)
                        )
                    }
                }
            }
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(inputShape)
                        .background(Color(0xD9172731))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.10f),
                            shape = inputShape
                        )
                        .padding(start = 12.dp, end = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp)
                            ) {
                                if (text.isBlank()) {
                                    Text(
                                        text = "پیام خود را بنویسید...",
                                        color = Color.White.copy(alpha = 0.42f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                BasicTextField(
                                    value = text,
                                    onValueChange = onTextChange,
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White
                                    ),
                                    cursorBrush = SolidColor(GoldPrimary),
                                    singleLine = false
                                )
                            }
                        }

                        IconButton(
                            onClick = { emojiOpen = !emojiOpen },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEmotions,
                                contentDescription = "ایموجی",
                                tint = if (emojiOpen) {
                                    GoldPrimary
                                } else {
                                    Color.White.copy(alpha = 0.78f)
                                },
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Color(0xFF3EA4F4).copy(
                                alpha = if (text.isNotBlank() && !sending) 1f else 0.52f
                            )
                        )
                        .then(
                            if (text.isNotBlank() && !sending) {
                                Modifier.clickable { onSend() }
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (sending) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال",
                            tint = Color.White.copy(
                                alpha = if (text.isNotBlank()) 1f else 0.45f
                            ),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/* ═══════════════ آیتم‌های لیست چت (پیام + جداکننده‌ی تاریخ) ═══════════════ */

sealed class ChatListItem {
    data class Bubble(val message: ChatMessage) : ChatListItem()
    data class DayHeader(val dayStart: Long, val label: String) : ChatListItem()
}

/** خط جداکننده‌ی تاریخ روز — مثل تلگرام/واتساپ */
@Composable
private fun DateSeparatorHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.65f),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 14.dp, vertical = 5.dp)
        )
    }
}

/* ═══════════════ تبدیل زمان: UTC سرور ← timezone دستگاه ═══════════════
 * سرور (config timezone = UTC) created_at را UTC ذخیره می‌کند.
 * نمایش باید با timezone خود دستگاه باشد تا درست باشد.
 */

private val UTC_PATTERNS = listOf(
    "yyyy-MM-dd'T'HH:mm:ss",
    "yyyy-MM-dd HH:mm:ss",
    "yyyy-MM-dd HH:mm"
)

/** پارس کردن created_at سروری (UTC) به millis — اگر نشد، null */
private fun parseUtcToMillis(raw: String?): Long? {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return null

    for (pattern in UTC_PATTERNS) {
        val sdf = SimpleDateFormat(pattern, Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val parsed = try {
            sdf.parse(value)
        } catch (_: Exception) {
            null
        }
        if (parsed != null) return parsed.time
    }
    return null
}

/** نمایش HH:mm با timezone دستگاه + ارقام فارسی */
private fun formatTimeLocal(millis: Long?): String {
    if (millis == null) return ""
    return try {
        SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(millis))
            .toPersianDigits()
    } catch (_: Exception) {
        ""
    }
}

/** شروعِ روزِ محلیِ (00:00) آن لحظه — برای گروه‌بندی پیام‌ها به روز */
private fun localDayStart(instantMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = instantMillis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/** شروعِ امروز (00:00 محلی) */
private fun startOfToday(): Long {
    return localDayStart(System.currentTimeMillis())
}

/**
 * برچسب جداکننده‌ی تاریخ:
 *  - امروز / دیروز
 *  - غیر این‌ها: تاریخ کامل با ماه فارسی (مثلاً 26 شهریور 1405)
 */
private fun dayLabel(dayStart: Long): String {
    val todayStart = startOfToday()

    val yesterdayCal = Calendar.getInstance()
    yesterdayCal.timeInMillis = todayStart
    yesterdayCal.add(Calendar.DAY_OF_MONTH, -1)
    yesterdayCal.set(Calendar.HOUR_OF_DAY, 0)
    yesterdayCal.set(Calendar.MINUTE, 0)
    yesterdayCal.set(Calendar.SECOND, 0)
    yesterdayCal.set(Calendar.MILLISECOND, 0)
    val yesterdayStart = yesterdayCal.timeInMillis

    return when (dayStart) {
        todayStart -> "امروز"
        yesterdayStart -> "دیروز"
        else -> {
            // تاریخ شمسی (مثلاً 26 شهریور 1405) — با DateUtils موجود در پروژه
            val gregorian = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(dayStart))
            } catch (_: Exception) {
                ""
            }
            DateUtils.toJalaliReadable(gregorian)
        }
    }
}
