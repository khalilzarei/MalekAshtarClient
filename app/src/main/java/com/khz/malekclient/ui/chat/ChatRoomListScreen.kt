package com.khz.malekclient.ui.chat

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.khz.malekclient.core.util.ChatTimeUtils
import com.khz.malekclient.core.util.appViewModel
import com.khz.malekclient.core.util.toPersianDigits
import com.khz.malekclient.domain.model.ChatRoom
import com.khz.malekclient.ui.components.AvatarView
import com.khz.malekclient.ui.components.ErrorContent
import com.khz.malekclient.ui.components.GlassBackground
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.components.GlassTopBar
import com.khz.malekclient.ui.components.ListState
import com.khz.malekclient.ui.components.LoadingContent
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.RedError

/** حداکثر یک‌بار در هر اجرای اپ (تا اذیت‌کننده نشود) */
private var permissionHintShownThisSession = false

@Composable
fun ChatRoomListScreen(
    onBack: () -> Unit,
    onOpenChat: (Int) -> Unit,
    onOpenContacts: () -> Unit
) {
    val viewModel: ChatRoomListViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    /*
     * Android 13+: اگر اجازه‌ی نوتیفیکیشن داده نشده باشد،
     * بدون آن هیچ نوتیفیکیشن پیام جدیدی نمایش داده نمی‌شود —
     * یک‌بار هشدار می‌دهیم تا کاربر اجازه دهد.
     */
    val showPermissionDialog = remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* نتیجه در چک بعدی اعمال می‌شود */ }

    LaunchedEffect(Unit) {
        if (!permissionHintShownThisSession && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && context.checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionHintShownThisSession = true
            showPermissionDialog.value = true
        }
    }

    if (showPermissionDialog.value) {
        NotificationPermissionDialog(
            onDismiss = { showPermissionDialog.value = false },
            onRequestPermission = {
                showPermissionDialog.value = false
                notificationPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            },
            onOpenSettings = {
                showPermissionDialog.value = false
                context.startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts(
                            "package",
                            context.packageName,
                            null
                        )
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            })
    }

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

            /* شروع گفتگوی جدید (ادمین / مربی) */
            FloatingActionButton(
                onClick = onOpenContacts,
                containerColor = GoldPrimary.copy(alpha = 0.85f),
                contentColor = Color(0xFF1A0533),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Chat,
                    contentDescription = "شروع گفتگوی جدید"
                )
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

            /*
             * طراحی یکسان با اپ ادمین:
             * گروه‌ها تصویر روم (یا حرف اول عنوان) را نشان می‌دهند.
             */
            AvatarView(
                name = avatarName,
                avatarUrl = avatarUrl,
                size = 48.dp,
                accentColor = GoldPrimary
            )

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

                    /*
                     * زمان پیام آخر (شمسی):
                     * امروز → "14:30" / دیروز → "دیروز" / قدیمی‌تر → "26 شهریور 1405"
                     */
                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = formatChatDate(room.lastMessage?.createdAt),

                        color = Color.White.copy(alpha = 0.45f),

                        style = MaterialTheme.typography.labelSmall
                    )
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

/**
 * برچسب زمان پیام آخر — مثل برنامه‌های چت (تاریخ شمسی):
 *  - امروز: "14:30" (با timezone دستگاه)
 *  - دیروز: "دیروز"
 *  - قدیمی‌تر: "26 شهریور 1405" (شمسی)
 *
 * توجه: created_at سرور UTC است — با ChatTimeUtils به timezone دستگاه
 * تبدیل می‌شود تا تاریخ اشتباه نشود (مثلاً پیام 22:00 UTC = صبحِ روز بعد).
 */
private fun formatChatDate(createdAt: String?): String {
    val millis = ChatTimeUtils.parseUtcToMillis(createdAt)
            ?: return ""
    val dayStart = ChatTimeUtils.localDayStart(millis)

    return if (dayStart == ChatTimeUtils.startOfToday()) {
        ChatTimeUtils.formatTimeLocal(millis)
    } else {
        ChatTimeUtils.dayLabel(dayStart)
    }
}

/**
 * هشدار نبودن اجازه‌ی نوتیفیکیشن (Android 13+).
 * بدون این اجازه، پیام جدیدی نوتیفیکیشن نمی‌شود.
 */
@Composable
private fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("اجازه‌ی نوتیفیکیشن")
        },
        text = {
            Text(
                "برای دریافت نوتیفیکیشن پیام‌های جدید چت، " + "به برنامه اجازه‌ی نمایش نوتیفیکیشن بدهید."
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("اجازه بده")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenSettings) {
                    Text("تنظیمات")
                }
                TextButton(onClick = onDismiss) {
                    Text("بعداً")
                }
            }
        })
}
