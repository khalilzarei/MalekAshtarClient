package com.khz.malekclient.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ابزار زمان چت — منبع مشترک برای ChatRoomList و ChatScreen
 *
 *  - سرور (timezone = UTC) رشته‌ی created_at را UTC ذخیره می‌کند.
 *  - زمان: با timezone خودِ دستگاه نمایش می‌شود (HH:mm، ارقام فارسی).
 *  - تاریخ: شمسی — «امروز» / «دیروز» / «۲۶ شهریور ۱۴۰۵».
 */
object ChatTimeUtils {

    private val UTC_PATTERNS = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm"
    )

    /** پارس کردن created_at سروری (UTC) به millis — اگر نشد، null */
    fun parseUtcToMillis(raw: String?): Long? {
        val value = raw?.trim()
            .orEmpty()
        if (value.isEmpty()) return null

        for (pattern in UTC_PATTERNS) {
            val sdf = SimpleDateFormat(
                pattern,
                Locale.US
            )
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
    fun formatTimeLocal(millis: Long?): String {
        if (millis == null) return ""
        return try {
            SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date(millis))
                .toPersianDigits()
        } catch (_: Exception) {
            ""
        }
    }

    /** شروعِ روزِ محلیِ (00:00) آن لحظه — برای گروه‌بندی پیام‌ها به روز */
    fun localDayStart(instantMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = instantMillis
        cal.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        cal.set(
            Calendar.MINUTE,
            0
        )
        cal.set(
            Calendar.SECOND,
            0
        )
        cal.set(
            Calendar.MILLISECOND,
            0
        )
        return cal.timeInMillis
    }

    /** شروعِ امروز (00:00 محلی) */
    fun startOfToday(): Long = localDayStart(System.currentTimeMillis())

    /**
     * برچسب جداکننده‌ی تاریخ (شمسی):
     *  - امروز / دیروز
     *  - غیر این‌ها: «۲۶ شهریور ۱۴۰۵» (با نام ماه شمسی)
     */
    fun dayLabel(dayStart: Long): String {
        val todayStart = startOfToday()

        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.timeInMillis = todayStart
        yesterdayCal.add(
            Calendar.DAY_OF_MONTH,
            -1
        )
        yesterdayCal.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        yesterdayCal.set(
            Calendar.MINUTE,
            0
        )
        yesterdayCal.set(
            Calendar.SECOND,
            0
        )
        yesterdayCal.set(
            Calendar.MILLISECOND,
            0
        )
        val yesterdayStart = yesterdayCal.timeInMillis

        return when (dayStart) {
            todayStart -> "امروز"
            yesterdayStart -> "دیروز"
            else -> {
                val gregorian = try {
                    SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.US
                    ).format(Date(dayStart))
                } catch (_: Exception) {
                    ""
                }
                DateUtils.toJalaliReadable(gregorian)
            }
        }
    }
}
