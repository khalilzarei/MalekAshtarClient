package com.khz.malekashtarclient.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.ChatRoom
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.ListState
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.RedError

@Composable
fun ChatRoomListScreen(
    onBack: () -> Unit,
    onOpenChat: (Int) -> Unit
) {
    val viewModel: ChatRoomListViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            GlassTopBar(
                title = "گفتگوها",
                onBack = onBack
            )

            when (val currentState = state) {

                is ListState.Loading -> {

                    LoadingContent()
                }

                is ListState.Error -> {

                    ErrorContent(
                        message = currentState.message,
                        onRetry = {
                            viewModel.refresh()
                        })
                }

                is ListState.Success<*> -> {

                    @Suppress("UNCHECKED_CAST")
                    val rooms = currentState.items as List<ChatRoom>

                    if (rooms.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            Text(
                                text = "هنوز گفتگویی ندارید",
                                color = Color.White.copy(
                                    alpha = 0.6f
                                ),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp),

                            contentPadding = PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),

                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = rooms,
                                key = { it.id }) { room ->

                                ChatRoomItem(
                                    room = room,
                                    onClick = {
                                        onOpenChat(room.id)
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRoomItem(
    room: ChatRoom,
    onClick: () -> Unit
) {
    val title = room.title.takeIf { it.isNotBlank() }
            ?: if (room.isGroup) {
                "گروه"
            } else {
                "گفتگو"
            }

    /*
     * در Private Chat:
     *
     * room.image ممکن است وجود داشته باشد.
     * اگر نبود، avatar کاربر مقابل را نمایش می‌دهیم.
     *
     * نکته:
     * برای تشخیص کاربر مقابل، اینجا نیاز به currentUserId داریم.
     * چون ChatRoomListViewModel قبلاً اطلاعات اتاق را آماده کرده،
     * برای جلوگیری از وابستگی UI به SessionManager، اگر image
     * وجود داشته باشد از همان استفاده می‌کنیم.
     */
    val avatarUrl = room.image?.takeIf { it.isNotBlank() }

    val privateUser = if (!room.isGroup) {
        room.users.firstOrNull()
    } else {
        null
    }

    val avatarName = if (room.isGroup) {
        title
    } else {
        privateUser?.fullName?.takeIf { it.isNotBlank() }
                ?: title
    }

    GlassCard3D(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            if (room.isGroup) {

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            GoldPrimary.copy(
                                alpha = 0.25f
                            )
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Default.Group,

                        contentDescription = "گروه",

                        tint = GoldPrimary,

                        modifier = Modifier.size(24.dp)
                    )
                }

            } else {

                AvatarView(
                    name = avatarName,
                    avatarUrl = avatarUrl,
                    size = 48.dp,
                    accentColor = GoldPrimary
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = title,

                        color = Color.White,

                        style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),

                        modifier = Modifier.weight(1f),

                        maxLines = 1,

                        overflow = TextOverflow.Ellipsis
                    )

                    if (room.isGroup) {

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = "گروه",

                            color = GoldPrimary,

                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    if (room.isLocked) {

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Icon(
                            imageVector = Icons.Default.Lock,

                            contentDescription = "قفل",

                            tint = Color(0xFFFF8A80),

                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.size(2.dp)
                )

                val lastMessage = room.lastMessage

                Text(
                    text = buildString {

                        if (room.isGroup && !lastMessage?.senderName.isNullOrBlank()) {

                            append(
                                lastMessage.senderName
                            )

                            append(": ")
                        }

                        append(lastMessage?.body?.takeIf {
                                it.isNotBlank()
                            }
                                ?: "پیامی ارسال نشده")
                    },

                    color = Color.White.copy(
                        alpha = 0.5f
                    ),

                    style = MaterialTheme.typography.bodySmall,

                    maxLines = 1,

                    overflow = TextOverflow.Ellipsis
                )
            }

            if (room.unreadCount > 0) {

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Badge(
                    containerColor = RedError
                ) {

                    Text(
                        text = if (room.unreadCount > 99) {
                            "99+"
                        } else {
                            room.unreadCount.toString()
                                .toPersianDigits()
                        },

                        color = Color.White,

                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
