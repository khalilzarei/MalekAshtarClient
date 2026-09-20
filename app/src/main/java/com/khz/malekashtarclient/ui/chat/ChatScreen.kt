package com.khz.malekashtarclient.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.khz.malekashtarclient.FootballSchoolApp
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.ChatMessage
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.theme.GoldOn
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * صفحه‌ی چت — بدون ViewModel (طبق پرامپت)
 *
 * - targetUserId: برای ساخت اتاق دو نفره؛ null برای اتاق‌های موجود
 * - roomId: اگر از لیست اتاق‌ها آمده
 * - polling ۵ ثانیه فقط در STARTED
 * - markAsRead بعد از هر refresh
 * - ارسال در try/finally (sending همیشه آزاد)
 * - اگر isLocked: نوار ارسال با پیام قفل جایگزین می‌شود
 * - نوار ارسال RTL + آیکون Send قرینه
 */
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
    var roomTitle by remember {
        mutableStateOf(
            initialTitle
                    ?: "گفتگو"
        )
    }
    var isGroup by remember { mutableStateOf(false) }
    var isLocked by remember { mutableStateOf(false) }
    var memberCount by remember { mutableStateOf(0) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentUserId by remember { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // ─── بارگذاری اولیه: ساخت/دریافت room (اگر لازم) + loadMessages ───
    LaunchedEffect(
        targetUserId,
        roomId
    ) {
        loading = true
        error = null
        currentUserId = sessionManager.userId.first()
            ?.toIntOrNull()

        if (currentRoomId == null && targetUserId != null) {
            when (val r = chatRepo.getOrCreatePrivateRoom(targetUserId)) {
                is NetworkResult.Success -> {
                    val room = r.data
                    currentRoomId = room.id
                    // برای age_group: targetUserName = null و targetUserName در سرور = ageGroupTitle
                    // برای دو نفره: targetUserName = نام طرف
                    roomTitle = when {
                        room.isGroup -> room.ageGroupTitle
                                ?: room.targetUserName
                                ?: roomTitle

                        else         -> room.targetUserName
                                ?: roomTitle
                    }
                    isGroup = room.isGroup
                    isLocked = room.isLocked
                    memberCount = room.memberCount
                }

                is NetworkResult.Error   -> {
                    error = r.message
                    loading = false
                    return@LaunchedEffect
                }

                else                     -> Unit
            }
        }

        // بارگذاری اولین سری پیام‌ها
        currentRoomId?.let {
            loadMessages(
                chatRepo,
                it
            ) { msgs -> messages = msgs }
        }
        loading = false
    }

    // ─── polling ۵ ثانیه (فقط در ON_START) ───
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                scope.launch {
                    while (true) {
                        currentRoomId?.let {
                            loadMessages(
                                chatRepo,
                                it
                            ) { msgs -> messages = msgs }
                        }
                        delay(5_000)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // اسکرول خودکار به آخرین پیام
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // ─── ارسال پیام ───
    fun onSend() {
        val rid = currentRoomId
                ?: return
        val text = messageText.trim()
        if (text.isEmpty() || sending) return

        scope.launch {
            sending = true
            try {
                when (val r = chatRepo.sendMessage(
                    rid,
                    text
                )) {
                    is NetworkResult.Success -> {
                        messageText = ""
                        loadMessages(
                            chatRepo,
                            rid
                        ) { msgs -> messages = msgs }
                    }

                    is NetworkResult.Error   -> {
                        error = r.message
                        if (r.message.contains("قفل")) isLocked = true
                    }

                    else                     -> Unit
                }
            } finally {
                sending = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        GlassTopBar(
            title = if (isGroup) "$roomTitle (${memberCount.toPersianDigits()} عضو)" else roomTitle,
            onBack = onBack
        )

        Box(Modifier.weight(1f)) {
            when {
                loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoldPrimary)
                }

                error != null && messages.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error!!,
                        color = Color(0xFFFF8A80),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        scope.launch {
                            error = null
                            currentRoomId?.let {
                                loadMessages(
                                    chatRepo,
                                    it
                                ) { msgs -> messages = msgs }
                            }
                        }
                    }) {
                        Text(
                            "تلاش مجدد",
                            color = GoldPrimary
                        )
                    }
                }

                messages.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "هنوز پیامی رد و بدل نشده",
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "اولین پیام را ارسال کنید",
                            color = Color.White.copy(0.4f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { msg ->
                        MessageBubble(
                            message = msg,
                            isMine = msg.senderId == currentUserId,
                            isGroup = isGroup
                        )
                    }
                }
            }
        }

        // نوار ارسال / قفل
        if (currentRoomId != null) {
            if (isLocked) {
                LockedBar()
            } else {
                MessageInputBar(
                    text = messageText,
                    onTextChange = { messageText = it },
                    sending = sending,
                    onSend = { onSend() })
            }
        }
    }
}

/** بارگذاری پیام‌ها + markAsRead */
private suspend fun loadMessages(
    chatRepo: com.khz.malekashtarclient.data.repository.ChatRepository,
    roomId: Int,
    onLoaded: (List<ChatMessage>) -> Unit
) {
    when (val r = chatRepo.getMessages(
        roomId,
        limit = 50
    )) {
        is NetworkResult.Success -> {
            onLoaded(r.data)
            r.data.maxOfOrNull { it.id }
                ?.let { lastId ->
                    chatRepo.markAsRead(
                        roomId,
                        lastId
                    )
                }
        }

        is NetworkResult.Error   -> Unit
        else                     -> Unit
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    isGroup: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        if (!isMine) {
            AvatarView(
                name = message.senderName,
                size = 32.dp,
                accentColor = Color(0xFF4FC3F7)
            )
            Spacer(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (isMine) Alignment.End else Alignment.Start) {
            if (isGroup && !isMine && !message.senderName.isNullOrBlank()) {
                Text(
                    text = message.senderName,
                    color = Color(0xFF4FC3F7),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(
                        start = 4.dp,
                        bottom = 2.dp
                    )
                )
            }

            val shape = if (isMine) {
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 4.dp,
                    bottomEnd = 18.dp
                )
            } else {
                RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = 18.dp,
                    bottomEnd = 4.dp
                )
            }

            val bgBrush = if (isMine) {
                Brush.linearGradient(
                    listOf(
                        GoldPrimary.copy(0.85f),
                        GoldPrimary.copy(0.55f)
                    )
                )
            } else {
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(0.15f),
                        Color.White.copy(0.08f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(shape)
                    .background(bgBrush)
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    )
            ) {
                Text(
                    text = message.body,
                    color = if (isMine) GoldOn else Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(2.dp))
            Text(
                text = message.createdAt?.takeLast(8)
                    ?.take(5)
                        ?: "",
                color = Color.White.copy(0.4f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        if (isMine) {
            Spacer(Modifier.width(6.dp))
            AvatarView(
                name = null,
                size = 32.dp,
                accentColor = GoldPrimary
            )
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
    // نوار راست‌چین برای RTL
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        GlassCard3D(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // فیلد متن (وزن ۱، همیشه اول می‌آید = راست در RTL)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(
                            horizontal = 16.dp,
                            vertical = 10.dp
                        )
                ) {
                    if (text.isEmpty()) {
                        Text(
                            "پیام خود را بنویسید...",
                            color = Color.White.copy(0.4f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        cursorBrush = SolidColor(GoldPrimary),
                        singleLine = false
                    )
                }

                Spacer(Modifier.width(8.dp))

                // دکمه ارسال (در RTL در سمت چپ نمایش داده می‌شود)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    GoldPrimary.copy(alpha = if (text.isNotBlank() && !sending) 0.95f else 0.3f),
                                    GoldPrimary.copy(alpha = if (text.isNotBlank() && !sending) 0.5f else 0.15f)
                                )
                            )
                        )
                        .then(
                            if (text.isNotBlank() && !sending) Modifier.clickable(onClick = onSend)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (sending) {
                        CircularProgressIndicator(
                            color = GoldOn,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "ارسال",
                            tint = if (text.isNotBlank()) GoldOn else Color.White.copy(0.4f),
                            modifier = Modifier
                                .size(20.dp)
                                .graphicsLayer(
                                    scaleX = -1f,
                                    scaleY = 1f
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockedBar() {
    GlassCard3D(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔒",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "چت توسط مدیر قفل شده است",
                color = Color.White.copy(0.85f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
