package com.khz.malekclient.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.khz.malekclient.MalekClientApp
import com.khz.malekclient.MainActivity
import com.khz.malekclient.R
import com.khz.malekclient.core.di.AppContainer
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.domain.model.ChatRoom
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * نوتیفیکیشن محلی برای پیام‌های جدید چت (پولینگ، بدون Firebase).
 *
 * رویکرد:
 *  - اپ باز: هر ۶۰ ثانیه + چک فوری هنگام برگشت به فرانت‌گراند
 *  - اپ بسته: هر ۱۵ دقیقه (کمینه‌ی WorkManager)
 *
 * ⚠️ این فایل فقط از APIهای فریم‌ورک اندروید استفاده می‌کند
 * (android.app.Notification) — به androidx.core و NotificationCompat
 * نیازی ندارد تا از خطای Unresolved reference دوری شود.
 */
object ChatNotificationManager {

    private const val CHANNEL_ID = "chat_messages"
    private const val CHANNEL_NAME = "پیام‌های چت"
    private const val CHANNEL_DESCRIPTION = "اعلام پیام‌های جدید در گفتگوها"

    private const val PREFS_NAME = "chat_notification_state"
    private const val UNREAD_PREFIX = "unread_"

    /** extra اینتنت برای deep-link به اتاق چت (لمس نوتیفیکیشن) */
    const val EXTRA_CHAT_ROOM_ID = "chat_room_id"

    /** ساخت کانال نوتیفیکیشن (فقط Android 8+) */
    fun initChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            nm.createNotificationChannel(channel)
        }
    }

    /** آیا دستگاه اجازه‌ی نمایش نوتیفیکیشن را داده؟ (Android 13+ permission) */
    fun canPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /** بررسی پیام‌های جدید و نمایش نوتیفیکیشن محلی در صورت وجود */
    suspend fun checkAndNotify(
        context: Context,
        container: AppContainer
    ) {
        if (!canPostNotifications(context)) return

        when (val result = container.chatRepository.rooms()) {
            is NetworkResult.Success -> handleRooms(
                context,
                result.data
            )

            is NetworkResult.Error   -> Unit // خطای شبکه — ساکت
            is NetworkResult.Loading -> Unit
        }
    }

    private fun handleRooms(
        context: Context,
        rooms: List<ChatRoom>
    ) {
        val prefs = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val currentRoomIds = rooms.map { it.id }
            .toSet()
        val editor = prefs.edit()

        for (room in rooms) {
            // شناسه اتاق در یک متغیر محلی — Int
            val roomId = room.id
            val key = UNREAD_PREFIX + roomId
            val previous = prefs.getInt(
                key,
                -1
            )
            val current = room.unreadCount

            when {
                previous == -1 -> {
                    // اجرای اول برای این اتاق: فقط baseline
                    editor.putInt(
                        key,
                        current
                    )
                }

                current > previous -> {
                    showRoomNotification(
                        context,
                        room,
                        roomId
                    )
                    editor.putInt(
                        key,
                        current
                    )
                }

                else -> {
                    // کاربر در اپ پیام‌ها را خوانده (یا تغییری نیست)
                    editor.putInt(
                        key,
                        current
                    )
                }
            }
        }

        // baseline اتاق‌هایی که دیگر وجود ندارند را پاک کن
        prefs.all.keys.filter { it.startsWith(UNREAD_PREFIX) }
            .mapNotNull {
                it.removePrefix(UNREAD_PREFIX)
                    .toIntOrNull()
            }
            .filter { it !in currentRoomIds }
            .forEach { editor.remove(UNREAD_PREFIX + it) }

        editor.apply()
    }

    private fun showRoomNotification(
        context: Context,
        room: ChatRoom,
        roomId: Int
    ) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // areNotificationsEnabled فقط Android 8+ است
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nm.areNotificationsEnabled()) return

        val title = room.title.ifBlank { "پیام جدید" }

        // همه‌ی داده‌های پیام در متغیرهای محلی — template بدون نقطه
        val lastMessage = room.lastMessage
        val senderName = lastMessage?.senderName
        var text = lastMessage?.body?.takeIf { it.isNotBlank() }
                ?: "پیام جدید"
        if (room.isGroup && !senderName.isNullOrBlank()) {
            text = senderName + ": " + text
        }
        if (text.length > 80) text = text.take(80) + "…"

        val intent = Intent(
            context,
            MainActivity::class.java
        ).apply {
            putExtra(
                EXTRA_CHAT_ROOM_ID,
                roomId
            )
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            roomId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        /*
         * فقط API فریم‌ورک — بدون androidx.core.
         * Android 8+: Builder با کانال + setTag.
         * Android 7 و قدیمی‌تر: Builder ساده (بدون کانال و بدون setTag،
         * چون setTag فریم‌ورکی از API 26 در دسترس است).
         */
        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(text)
                )
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            Notification.Builder(context)
                .setSmallIcon(R.drawable.splash_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(
                    Notification.BigTextStyle()
                        .bigText(text)
                )
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setContentIntent(pendingIntent)
                .build()
        }

        nm.notify(
            roomId,
            notification
        )
    }

    /**
     * آخرین چک (برای جلوگیری از تکرار فوری پشت‌سرهم —
     * هم حلقه‌ی ۶۰ ثانیه‌ای و هم onResume می‌توانند صدا بزنند).
     */
    @Volatile
    private var lastCheckAtMillis: Long = 0L

    /**
     * چک فوری پیام‌های جدید — با guard فاصله‌ی حداقلی.
     * توسط حلقه‌ی ۶۰ ثانیه‌ای و MainActivity.onResume صدا زده می‌شود.
     */
    fun checkOnForeground(
        app: MalekClientApp,
        minIntervalMillis: Long = 15_000L
    ) {
        val now = System.currentTimeMillis()
        if (now - lastCheckAtMillis < minIntervalMillis) return
        lastCheckAtMillis = now

        app.notificationPollingScope.launch {
            val token = app.container.sessionManager.getTokenSync()
            if (token.isNullOrBlank()) return@launch

            runCatching {
                ChatNotificationManager.checkAndNotify(
                    app,
                    app.container
                )
            }
        }
    }

    /**
     * حلقه‌ی چک دوره‌ای وقتی اپ باز است (هر [intervalMillis] میلی‌ثانیه).
     * اولین چک فوری انجام می‌شود تا baseline بلافاصله ثبت شود.
     */
    fun startForegroundPolling(
        app: MalekClientApp,
        intervalMillis: Long = 60_000L
    ) {
        app.notificationPollingScope.launch {
            while (isActive) {
                checkOnForeground(
                    app,
                    minIntervalMillis = 0L
                )
                delay(intervalMillis)
            }
        }
    }
}