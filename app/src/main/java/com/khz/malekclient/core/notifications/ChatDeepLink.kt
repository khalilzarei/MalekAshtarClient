package com.khz.malekclient.core.notifications

/**
 * نگهداری شناسه‌ی اتاق چت برای deep-link (لمس نوتیفیکیشن)
 * تا زمانی که فلو ناوبری آماده شود (بعد از Splash/لاگین).
 *
 * MainActivity در onCreate/onNewIntent مقدار را پر می‌کند و
 * RootNavGraph بعد از رسیدن به داشبورد/لاگین آن را مصرف و صفر می‌کند.
 */
object ChatDeepLink {
    @Volatile
    var pendingRoomId: Int = 0
}
