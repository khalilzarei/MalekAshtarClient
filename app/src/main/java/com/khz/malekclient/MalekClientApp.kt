package com.khz.malekclient

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.khz.malekclient.core.di.AppContainer
import com.khz.malekclient.core.notifications.ChatNotificationManager
import com.khz.malekclient.core.notifications.ChatNotificationWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.concurrent.TimeUnit

/**
 * Application اصلی — ظرف DI دستی در اینجا نگه‌داری می‌شود.
 *
 * دسترسی از Composable:
 *   val container = (LocalContext.current.applicationContext as FootballSchoolApp).container
 *
 * یا راحت‌تر:
 *   import com.khz.malekclient.core.util.LocalAppContainer
 */
class MalekClientApp : Application() {

    lateinit var container: AppContainer
        private set

    /**
     * scope حلقه‌های پس‌زمینه‌ی خود اپ (پولینگ نوتیفیکیشن چت).
     * از Dispatchers.Default — UI inگیر نیست و تا عمر پروسه زنده می‌ماند.
     */
    val notificationPollingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // ── نوتیفیکیشن چت ──
        // ۱) کانال نوتیفیکیشن
        ChatNotificationManager.initChannel(this)

        // ۲) چک بک‌گراند هر ۱۵ دقیقه (وقتی اپ بسته است)
        val periodicWork = PeriodicWorkRequestBuilder<ChatNotificationWorker>(
            15,
            TimeUnit.MINUTES
        ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "chat_notifications",
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )

        // ۳) چک فرانت‌گراند (وقتی اپ باز و کاربر لاگین است) — هر ۲۰ ثانیه
        ChatNotificationManager.startForegroundPolling(
            this,
            intervalMillis = 20_000L
        )
    }

    override fun onTerminate() {
        notificationPollingScope.cancel()
        super.onTerminate()
    }
}
