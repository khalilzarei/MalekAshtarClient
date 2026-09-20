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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.khz.malekashtarclient.ui.theme.GoldOn
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.RedError

/**
 * صفحه‌ی لیست گفتگوها
 *
 * - آیتم‌ها: عنوان (نام طرف / گروه سنی)، آخرین پیام، badge، قفل 🔒، 👥 گروهی
 * - FAB برای گفتگوی جدید → ChatContactsScreen
 */
@Composable
fun ChatRoomListScreen(
    onBack: () -> Unit,
    onOpenRoom: (roomId: Int, userId: Int?, title: String?, isGroup: Boolean) -> Unit,
    onNewConversation: () -> Unit
) {
    val viewModel: ChatRoomListViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "گفتگوها",
                onBack = onBack
            )

            when (val s = state) {
                is ListState.Loading -> LoadingContent()
                is ListState.Error -> ErrorContent(
                    message = s.message,
                    onRetry = { viewModel.refresh() })

                is ListState.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val rooms = s.items as List<ChatRoom>
                    if (rooms.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هنوز گفتگویی ندارید",
                                color = Color.White.copy(0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = rooms,
                                key = { room -> room.id }) { room ->
                                RoomCard(
                                    room = room,
                                    onClick = {
                                        // named arguments در داخل lambda برای function type مجاز نیست
                                        val titleToShow = if (room.isGroup) {
                                            room.ageGroupTitle
                                                    ?: "گروه"
                                        } else {
                                            room.targetUserName
                                        }
                                        onOpenRoom(
                                            room.id,
                                            room.targetUserId,
                                            titleToShow,
                                            room.isGroup
                                        )
                                    })
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onNewConversation,
                containerColor = GoldPrimary,
                contentColor = GoldOn,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "گفتگوی جدید"
                )
            }
        }
    }
}

@Composable
private fun RoomCard(
    room: ChatRoom,
    onClick: () -> Unit
) {
    val title = if (room.isGroup) room.ageGroupTitle
            ?: "گروه" else room.targetUserName
            ?: "گفتگو"

    GlassCard3D(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // آواتار یا آیکون گروه
            if (room.isGroup) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(50))
                        .background(GoldPrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                AvatarView(
                    name = room.targetUserName,
                    avatarUrl = room.targetUserAvatar,
                    size = 48.dp,
                    accentColor = GoldPrimary
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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

                    if (room.isLocked) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "قفل",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                }

                Spacer(Modifier.height(2.dp))

                Text(
                    text = buildString {
                        room.lastMessageSenderName?.takeIf { it.isNotBlank() }
                            ?.let {
                                append("$it: ")
                            }
                        append(room.lastMessageBody?.takeIf { it.isNotBlank() }
                                ?: "پیامی ارسال نشده")
                    },
                    color = Color.White.copy(0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (room.unreadCount > 0) {
                Spacer(Modifier.width(8.dp))
                Badge(containerColor = RedError) {
                    Text(
                        text = room.unreadCount.toPersianDigits(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
