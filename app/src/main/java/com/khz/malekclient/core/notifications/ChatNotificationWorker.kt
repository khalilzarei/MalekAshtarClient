package com.khz.malekclient.core.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.khz.malekclient.MalekClientApp

/**
 * چک دوره‌ای پیام‌های جدید چت وقتی اپ بسته است.
 *
 * هر ۱۵ دقیقه (کمینه‌ی دوره‌ای WorkManager در Android) اجرا می‌شود،
 * unread_count اتاق‌ها را با baseline ذخیره‌شده مقایسه می‌کند و
 * در صورت پیام جدید، نوتیفیکیشن محلی نشان می‌دهد.
 *
 * در کنار حلقه‌ی فرانت‌گراند (هر ۶۰ ثانیه وقتی اپ باز است)
 * پوشش کامل دو حالت باز/بسته‌ی اپ را می‌دهد.
 */
class ChatNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? MalekClientApp
                ?: return Result.failure()

        val token = app.container.sessionManager.getTokenSync()
        if (token.isNullOrBlank()) return Result.success()

        return try {
            ChatNotificationManager.checkAndNotify(applicationContext, app.container)
            Result.success()
        } catch (e: Exception) {
            // خطای گذرا (شبکه و...) — چند بار تلاش کن
            if (runAttemptCount < 3) Result.retry() else Result.success()
        }
    }
}
