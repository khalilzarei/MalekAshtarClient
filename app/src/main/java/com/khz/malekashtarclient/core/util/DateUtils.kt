package com.khz.malekashtarclient.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ابزارهای تبدیل تاریخ میلادی ↔ شمسی (جلالی) و قالب‌بندی فارسی
 *
 * - تاریخ‌های API همیشه میلادی YYYY-MM-DD هستند
 * - نمایش در UI همیشه شمسی است
 * - ارقام با toPersianDigits فارسی می‌شوند
 */
object DateUtils {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    /** نام ماه‌های شمسی */
    private val persianMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    /** نام روزهای هفته (شنبه اول) */
    private val persianWeekdays = arrayOf(
        "شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه"
    )

    /** الگوریتم تبدیل میلادی به شمسی */
    private val gregorianDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private val jalaliDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

    /** تشخیص سال کبیسه‌ی میلادی */
    private fun isLeapGregorian(year: Int): Boolean =
        (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

    /** تشخیص سال کبیسه‌ی شمسی (سال‌های +1403) */
    private fun isLeapJalali(year: Int): Boolean {
        val breaks = intArrayOf(
            -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
            1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178
        )
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (year < jm) break
            jp = jm
        }
        var n = year - jp
        return if (n - jump < 6) n - jump + 1 + 7 * jump != 1 else false
        // نسخه‌ی ساده‌تر:
        // val mod = ((year - 474) % 2820 + 2820) % 2820
        // return mod != 0 && (mod % 4 == 0 || ...)
    }

    /** محاسبه‌ی روز سال میلادی (1 تا 365/366) */
    private fun gregorianToJDN(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    /** تبدیل روز جولیان به تاریخ شمسی */
    private fun jdnToJalali(jdn: Int): Triple<Int, Int, Int> {
        val gy = (jdn + 32082).let { (4 * it + 3) / 146097 }
        val dy = (jdn + 32082) - (146097 * gy) / 4
        val gyy = (4 * dy + 3) / 1461
        val gmm = (5 * ((dy - (1461 * gyy) / 4) + 2)) / 153
        val day = dy - (1461 * gyy) / 4 - (153 * gmm + 2) / 5 + 1
        val month = gmm + 3 - 12 * (gmm / 10)
        val year = 100 * gy + gyy - 400 * (gy / 4) + (gy / 4)

        val jy = year - 621
        val r = jalaliToGregorian(jy, 1, 1)
        val jdn2 = gregorianToJDN(r.first, r.second, r.third)
        val dayOfYear = jdn - jdn2 + 1

        return splitJalaliDay(jy, dayOfYear)
    }

    /** تبدیل تاریخ شمسی به میلادی */
    private fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        val days = -355668 + 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33 + 3) / 4) + jd +
            if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
        val gy = 400 * (days / 146097) + (days % 146097) / 36524 +
            if (days % 146097 == 0) 0 else (days % 146097) / 146097 * 4 + 1
        val gd = days - 365 * (gy - 1) - (gy / 4) + (gy % 4 == 0 && gy % 100 != 0 || gy % 400 == 0).let { if (it) 1 else 0 }
        // ساده‌تر:
        val g2 = days - 365 * gy + (gy / 4) - (gy / 100) + (gy / 400)
        val gm = (5 * ((g2 - 1 + 1) / 153) + 2) / 5
        val day = g2 - (153 * gm + 2) / 5 + 1
        val month = gm + 3 - 12 * (gm / 10)
        val year = gy - 4800 + (gm / 10)
        return Triple(year, month, day)
    }

    /** شکستن روز سال به (سال، ماه، روز) شمسی */
    private fun splitJalaliDay(year: Int, dayOfYear: Int): Triple<Int, Int, Int> {
        val isLeap = year % 4 == 3 && year > 0 // تقریب ساده: سال شمسی کبیسه هر ۴ سال یکبار
        val days = if (isLeap) {
            intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 30)
        } else {
            intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        }
        var remaining = dayOfYear
        for (i in 0 until 12) {
            if (remaining <= days[i]) return Triple(year, i + 1, remaining)
            remaining -= days[i]
        }
        return Triple(year, 12, 29)
    }

    /** تبدیل رشته YYYY-MM-DD میلادی به (سال، ماه، روز) شمسی */
    fun gregorianDateStringToJalali(date: String?): Triple<Int, Int, Int>? {
        if (date.isNullOrBlank()) return null
        return try {
            val parts = date.split("-")
            if (parts.size < 3) return null
            val y = parts[0].toInt()
            val m = parts[1].toInt()
            val d = parts[2].toInt()
            val jdn = gregorianToJDN(y, m, d)
            jdnToJalali(jdn)
        } catch (e: Exception) {
            null
        }
    }

    /** تبدیل YYYY-MM-DD میلادی به رشته‌ی شمسی بلند (مثلاً ۱۴۰۳/۰۶/۲۵) */
    fun toJalaliLong(date: String?): String {
        val j = gregorianDateStringToJalali(date) ?: return date ?: ""
        val (y, m, d) = j
        return "${y.toString().toPersianDigits()}/${m.toString().padStart(2, '0').toPersianDigits()}/${d.toString().padStart(2, '0').toPersianDigits()}"
    }

    /** تبدیل YYYY-MM-DD میلادی به رشته‌ی شمسی با نام ماه (مثلاً ۲۵ شهریور ۱۴۰۳) */
    fun toJalaliReadable(date: String?): String {
        val j = gregorianDateStringToJalali(date) ?: return date ?: ""
        val (y, m, d) = j
        val monthName = persianMonths.getOrNull(m - 1) ?: ""
        return "${d.toString().toPersianDigits()} $monthName ${y.toString().toPersianDigits()}"
    }

    /** محاسبه‌ی سن از تاریخ تولد (رشته YYYY-MM-DD میلادی) */
    fun calculateAge(birthDate: String?): Int? {
        val j = gregorianDateStringToJalali(birthDate) ?: return null
        val (jy, _, _) = j
        val today = gregorianDateStringToJalali(
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        ) ?: return null
        return today.first - jy
    }

    /** نام روز هفته از تاریخ YYYY-MM-DD (مثلاً شنبه) */
    fun weekdayName(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val parts = date.split("-")
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            // Calendar.SUNDAY=1..SATURDAY=7 — ما می‌خواهیم شنبه=0
            val gregorianWeekday = cal.get(Calendar.DAY_OF_WEEK) // SUNDAY=1..SATURDAY=7
            // تبدیل به شنبه=0: SATURDAY(7)→6, SUNDAY(1)→0, MONDAY(2)→1, ...
            val persianIndex = when (gregorianWeekday) {
                Calendar.SATURDAY -> 6
                else -> gregorianWeekday - 1
            }
            persianWeekdays.getOrNull(persianIndex) ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /** تبدیل ارقام یک رشته به فارسی */
    fun toPersianDigits(s: String?): String = s?.toPersianDigits() ?: ""

    /** فرمت ساعت از "HH:MM:SS" یا "HH:MM" به "HH:MM" */
    fun formatTime(time: String?): String {
        if (time.isNullOrBlank()) return ""
        return time.take(5)
    }

    /** فقط ساعت و دقیقه از time */
    fun formatTimeHHMM(time: String?): String = formatTime(time)

    /** تاریخ امروز به میلادی YYYY-MM-DD */
    fun todayGregorian(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** تاریخ به فرمت Date → "yyyy-MM-dd" */
    fun toGregorianString(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    /** شمارنده‌ی روزهای بین دو تاریخ شمسی (فقط برای نمایش «X روز دیگر») */
    fun daysUntil(targetDate: String?): Int? {
        if (targetDate.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val target = sdf.parse(targetDate) ?: return null
            val today = sdf.parse(sdf.format(Date())) ?: return null
            val diff = target.time - today.time
            (diff / (1000L * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            null
        }
    }
}
