package com.khz.malekashtarclient.ui.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.FootballSchoolApp
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.domain.model.ChatMessage
import com.khz.malekashtarclient.domain.model.ChatRoom
import com.khz.malekashtarclient.domain.model.ChatRoomUser
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.theme.GoldOn
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * ChatScreen
 *
 * پشتیبانی از دو حالت:
 * 1. ورود با roomId  -> اتاق موجود
 * 2. ورود با targetUserId -> ساخت/دریافت اتاق خصوصی
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    roomId: Int? = null,
    targetUserId: Int? = null,
    initialTitle: String? = null
) {
    val context = LocalContext.current
    val container = (context.applicationContext as FootballSchoolApp).container

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

    /* آواتار و نام کاربر جاری (برای پیام‌های خودی) */
    var myAvatarUrl by remember { mutableStateOf<String?>(null) }
    var myName by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    /* ---------- شناسه کاربر جاری + آواتار ---------- */
    LaunchedEffect(Unit) {
        currentUserId = sessionManager.userId.first()
            ?.toIntOrNull()

        container.authRepository.me()
            .let { result ->
                if (result is NetworkResult.Success) {
                    myAvatarUrl = result.data.avatarUrl
                    myName = result.data.fullName
                }
            }
    }

    /* ---------- بارگذاری اتاق ---------- */
    suspend fun loadRoom() {
        val rid = currentRoomId

        // حالت ساخت اتاق خصوصی جدید
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

                is NetworkResult.Error   -> error = result.message
                is NetworkResult.Loading -> Unit
            }
            return
        }

        // حالت اتاق موجود
        when (val result = chatRepo.rooms()) {
            is NetworkResult.Success -> {
                room = result.data.firstOrNull { it.id == rid }
                if (room == null) error = "اطلاعات گفتگو پیدا نشد"
            }

            is NetworkResult.Error   -> error = result.message
            is NetworkResult.Loading -> Unit
        }
    }

    /* ---------- بارگذاری پیام‌ها ---------- */
    suspend fun loadMessages() {
        val rid = currentRoomId
                ?: return

        when (val result = chatRepo.getMessages(
            roomId = rid,
            limit = 50
        )) {
            is NetworkResult.Success -> {
                messages = result.data
                result.data.maxOfOrNull { it.id }
                    ?.let { lastId ->
                        chatRepo.markAsRead(
                            rid,
                            lastId
                        )
                    }
            }

            is NetworkResult.Error   -> {
                if (messages.isEmpty()) error = result.message
            }

            is NetworkResult.Loading -> Unit
        }
    }

    /* ---------- بارگذاری اولیه ---------- */
    LaunchedEffect(
        roomId,
        targetUserId
    ) {
        loading = true
        error = null
        loadRoom()
        loadMessages()
        loading = false
    }

    /* ---------- Polling پیام‌ها ---------- */
    LaunchedEffect(currentRoomId) {
        while (isActive) {
            delay(3000.milliseconds)

            val rid = currentRoomId
                    ?: continue

            when (val result = chatRepo.getMessages(
                roomId = rid,
                limit = 50
            )) {
                is NetworkResult.Success -> {
                    val merged = (messages + result.data).distinctBy { it.id }
                        .sortedBy { it.id }

                    if (merged != messages) {
                        messages = merged
                        result.data.maxOfOrNull { it.id }
                            ?.let { lastId ->
                                chatRepo.markAsRead(
                                    rid,
                                    lastId
                                )
                            }
                    }
                }

                is NetworkResult.Error   -> Unit
                is NetworkResult.Loading -> Unit
            }
        }
    }

    /* ---------- اطلاعات نمایشی ---------- */
    val currentRoom = room
    val otherUser: ChatRoomUser? = currentRoom?.users?.firstOrNull { it.id != currentUserId }

    val roomTitle = if (currentRoom?.isGroup == true) {
        currentRoom.title.takeIf { it.isNotBlank() }
                ?: initialTitle
                ?: "گروه"
    } else {
        otherUser?.fullName?.takeIf { it.isNotBlank() }
                ?: currentRoom?.title?.takeIf { it.isNotBlank() }
                ?: initialTitle
                ?: "گفتگو"
    }

    val roomImage = if (currentRoom?.isGroup == true) {
        currentRoom.image?.takeIf { it.isNotBlank() }
    } else {
        otherUser?.avatar?.takeIf { it.isNotBlank() }
                ?: currentRoom?.image?.takeIf { it.isNotBlank() }
    }

    val roomSubtitle = if (currentRoom?.isGroup == true) {
        "${currentRoom.users.size} عضو"
    } else {
        otherUser?.role?.takeIf { it.isNotBlank() }
                ?: "گفتگوی خصوصی"
    }

    /* ---------- Scaffold ---------- */
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            GlassTopBar(
                title = roomTitle,
                onBack = onBack
            )
        }) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
        ) {

            /* ---------- هدر ---------- */
            RoomHeader(
                title = roomTitle,
                image = roomImage,
                subtitle = roomSubtitle
            )

            /* ---------- پیام‌ها ---------- */
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when {

                    loading                             -> LoadingState()

                    error != null && messages.isEmpty() -> ErrorState(
                        message = error
                                ?: "خطا در دریافت اطلاعات",
                        onRetry = {
                            scope.launch {
                                loading = true
                                error = null
                                loadRoom()
                                loadMessages()
                                loading = false
                            }
                        })

                    messages.isEmpty()                  -> EmptyState()

                    else                                -> MessagesList(
                        messages = messages,
                        listState = listState,
                        currentUserId = currentUserId,
                        myAvatarUrl = myAvatarUrl,
                        myName = myName
                    )
                }
            }

            /* ---------- نوار ارسال / وضعیت قفل ---------- */
            when {
                currentRoomId == null         -> Unit

                /* گفتگوی قفل‌شده: ارسال فقط توسط ادمین ممکن است */
                currentRoom?.isLocked == true -> {
                    GlassCard3D(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                                top = 8.dp,
                                bottom = 10.dp
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
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

                else                          -> {
                    MessageInputBar(
                        text = messageText,
                        onTextChange = { messageText = it },
                        sending = sending,
                        onSend = {
                            if (messageText.isBlank() || sending) return@MessageInputBar

                            val rid = currentRoomId
                                    ?: return@MessageInputBar
                            val text = messageText.trim()

                            scope.launch {
                                sending = true
                                try {
                                    when (val result = chatRepo.sendMessage(
                                        rid,
                                        text
                                    )) {
                                        is NetworkResult.Success -> {
                                            messages = (messages + result.data).distinctBy { it.id }
                                                .sortedBy { it.id }
                                            messageText = ""
                                        }

                                        is NetworkResult.Error   -> {
                                            Toast.makeText(
                                                context,
                                                result.message,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()
                                        }

                                        is NetworkResult.Loading -> Unit
                                    }
                                } catch (_: Exception) {
                                    Toast.makeText(
                                        context,
                                        "ارسال پیام ناموفق بود",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                } finally {
                                    sending = false
                                }
                            }
                        })
                }
            }
        }
    }

    /* ---------- اسکرول خودکار ---------- */
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.lastIndex)
            } catch (_: Exception) {
                Unit
            }
        }
    }

}

/* =========================================================
 *  بخش‌های فرعی (Sub-Composables) برای خوانایی بهتر
 * ========================================================= */

@Composable
private fun RoomHeader(
    title: String,
    image: String?,
    subtitle: String
) {
    GlassCard3D(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 8.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(
                name = title,
                avatarUrl = image,
                size = 56.dp,
                accentColor = GoldPrimary
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GoldPrimary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = Color(0xFFFF8A80),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onRetry) {
                Text(
                    text = "تلاش مجدد",
                    color = GoldPrimary
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "هنوز پیامی رد و بدل نشده",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "اولین پیام را ارسال کنید",
                color = Color.White.copy(alpha = 0.4f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MessagesList(
    messages: List<ChatMessage>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentUserId: Int?,
    myAvatarUrl: String?,
    myName: String?
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = messages,
            key = { it.id }) { message ->
            MessageBubble(
                message = message,
                isMine = message.senderId == currentUserId,
                myAvatarUrl = myAvatarUrl,
                myName = myName
            )
        }
    }
}

/* ---------- حباب پیام (طراحی یکسان با اپ ادمین) ---------- */
@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    myAvatarUrl: String?,
    myName: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {

        if (!isMine) {/* آواتار فرستنده (از payload پیام) */
            AvatarView(
                name = message.senderName
                        ?: "?",
                avatarUrl = message.senderAvatar,
                size = 32.dp,
                accentColor = Color(0xFF4FC3F7)
            )
            Spacer(Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {

            val shape = if (isMine) {
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 4.dp
                )
            } else {
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 18.dp
                )
            }

            val background = if (isMine) {
                Brush.linearGradient(
                    listOf(
                        GoldPrimary.copy(alpha = 0.92f),
                        GoldPrimary.copy(alpha = 0.58f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.16f),
                        Color.White.copy(alpha = 0.07f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 290.dp)
                    .background(
                        brush = background,
                        shape = shape
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
            ) {
                Column {
                    Text(
                        text = message.body,
                        color = if (isMine) GoldOn else Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        text = message.createdAt?.takeLast(8)
                            ?.take(5)
                                ?: "",
                        color = if (isMine) {
                            GoldOn.copy(alpha = 0.7f)
                        } else {
                            Color.White.copy(alpha = 0.5f)
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        if (isMine) {
            Spacer(Modifier.width(6.dp))

            /* آواتار کاربر جاری (حرف اول نام به‌عنوان fallback) */
            AvatarView(
                name = myName
                        ?: "من",
                avatarUrl = myAvatarUrl,
                size = 32.dp,
                accentColor = GoldPrimary
            )
        }
    }
}

/* ---------- نوار ورودی پیام ---------- */
@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    sending: Boolean,
    onSend: () -> Unit
) {
    GlassCard3D(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 10.dp
            )
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                /* دکمه ارسال */
                SendButton(
                    enabled = text.isNotBlank() && !sending,
                    sending = sending,
                    onClick = onSend
                )

                Spacer(Modifier.width(8.dp))

                /* فیلد متن */
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(
                            horizontal = 16.dp,
                            vertical = 11.dp
                        )
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "پیام خود را بنویسید...",
                            color = Color.White.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(GoldPrimary),
                        singleLine = false,
                        maxLines = 5
                    )
                }
            }
        }
    }
}

@Composable
private fun SendButton(
    enabled: Boolean,
    sending: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        GoldPrimary.copy(alpha = if (enabled) 0.95f else 0.28f),
                        GoldPrimary.copy(alpha = if (enabled) 0.48f else 0.12f)
                    )
                )
            )
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center) {
        if (sending) {
            CircularProgressIndicator(
                color = GoldOn,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "ارسال",
                tint = if (enabled) GoldOn else Color.White.copy(alpha = 0.35f),
                modifier = Modifier.size(21.dp)
            )
        }
    }
}