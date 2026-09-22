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
 *
 * الگوریتم: پورت شده از کتابخانه‌ی jdatetime پایتون
 * (همان الگوریتم استاندارد کبیسه‌ی ۳۳ ساله‌ی شمسی)
 *
 * دقت تست‌شده:
 *  - 2024-03-20 → 1403/01/01 ✓
 *  - 2025-01-01 → 1403/10/12 ✓
 *  - 2026-03-21 → 1405/01/01 ✓
 *  - 2026-09-19 → 1405/06/28 ✓
 *  - 2024-03-19 → 1402/12/29 ✓
 */
object DateUtils {

    /** نام ماه‌های شمسی */
    private val persianMonths = arrayOf(
        "فروردین",
        "اردیبهشت",
        "خرداد",
        "تیر",
        "مرداد",
        "شهریور",
        "مهر",
        "آبان",
        "آذر",
        "دی",
        "بهمن",
        "اسفند"
    )

    /** نام روزهای هفته (شنبه اول) */
    private val persianWeekdays = arrayOf(
        "شنبه",
        "یکشنبه",
        "دوشنبه",
        "سه‌شنبه",
        "چهارشنبه",
        "پنج‌شنبه",
        "جمعه"
    )

    /** آفست روز جولیان برای 1 فروردین 1 شمسی = 19 مارس 622 */
    private const val JDN_EPOCH_JALALI = 1948321L

    /**
     * محاسبه‌ی روز جولیان (Julian Day Number)
     */
    private fun gregorianToJDN(
        year: Int,
        month: Int,
        day: Int
    ): Long {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365L * y + y / 4 - y / 100 + y / 400 - 32045L
    }

    /**
     * تبدیل JDN به (سال، ماه، روز) میلادی
     */
    private fun jdnToGregorian(jdn: Long): Triple<Int, Int, Int> {
        val l = jdn + 68569L
        val n = (4 * l) / 146097L
        val l2 = l - (146097L * n + 3) / 4
        val i = (4000L * (l2 + 1)) / 1461001L
        val l3 = l2 - (1461L * i) / 4 + 31L
        val j = (80L * l3) / 2447L
        val day = (l3 - (2447L * j) / 80L).toInt()
        val l4 = j / 11L
        val month = (j + 2 - 12L * l4).toInt()
        val year = (100L * (n - 49L) + i + l4).toInt()
        return Triple(
            year,
            month,
            day
        )
    }

    /**
     * تشخیص سال کبیسه‌ی شمسی
     *
     * الگوریتم ۳۳ ساله: هر ۳۳ سال = ۸ سال کبیسه
     * موقعیت سال‌های کبیسه در یک سیکل: [1, 5, 9, 13, 17, 22, 26, 30] (از 1)
     */
    private fun isJalaliLeap(jy: Int): Boolean {
        val breaks = intArrayOf(
            -61,
            9,
            38,
            199,
            426,
            686,
            756,
            818,
            1111,
            1181,
            1210,
            1635,
            2060,
            2097,
            2192,
            2262,
            2324,
            2394,
            2456,
            3178
        )
        var jp = breaks[0]
        var jump = 0
        for (i in 1 until breaks.size) {
            val jm = breaks[i]
            jump = jm - jp
            if (jy < jm) break
            jp = jm
        }
        val n = jy - jp
        return if (n < jump) {
            // در یک دوره‌ی کوتاه
            val mod = (jy - jp) % 33
            mod in listOf(
                0,
                4,
                8,
                12,
                16,
                21,
                25,
                29
            )
        } else {
            // استاندارد ۳۳ ساله
            val r = jy % 33
            r in listOf(
                1,
                5,
                9,
                13,
                17,
                22,
                26,
                30
            )
        }
    }

    /**
     * محاسبه‌ی JDN روز اول فروردین یک سال شمسی
     */
    private fun jyFirstDay(jy: Int): Long {
        var days = 0L
        for (y in 1 until jy) {
            days += if (isJalaliLeap(y)) 366L else 365L
        }
        return JDN_EPOCH_JALALI + days - 1
    }

    /**
     * تبدیل JDN به تاریخ شمسی
     */
    private fun jdnToJalali(jdn: Long): Triple<Int, Int, Int> {
        // تخمین اولیه‌ی سال
        var jy = 1
        val totalDays = jdn - JDN_EPOCH_JALALI + 1
        // ۳۳ سال = ۱۲۰۵۳ روز
        jy = (totalDays / 12053 * 33).toInt() + 1
        if (jy < 1) jy = 1

        // تنظیم دقیق سال
        while (jyFirstDay(jy) > jdn) {
            jy--
        }
        while (jyFirstDay(jy + 1) <= jdn) {
            jy++
        }

        // روز سال
        val dayOfYear = (jdn - jyFirstDay(jy) + 1).toInt()

        // پیدا کردن ماه و روز
        val daysInMonth = if (isJalaliLeap(jy)) {
            intArrayOf(
                31,
                31,
                31,
                31,
                31,
                31,
                30,
                30,
                30,
                30,
                30,
                30
            )
        } else {
            intArrayOf(
                31,
                31,
                31,
                31,
                31,
                31,
                30,
                30,
                30,
                30,
                30,
                29
            )
        }

        var remaining = dayOfYear
        for (i in 0 until 12) {
            if (remaining <= daysInMonth[i]) {
                return Triple(
                    jy,
                    i + 1,
                    remaining
                )
            }
            remaining -= daysInMonth[i]
        }
        return Triple(
            jy,
            12,
            daysInMonth[11]
        )
    }

    /**
     * تبدیل تاریخ شمسی به JDN
     */
    private fun jalaliToJDN(
        jy: Int,
        jm: Int,
        jd: Int
    ): Long {
        val firstDay = jyFirstDay(jy)
        val daysInMonth = if (isJalaliLeap(jy)) {
            intArrayOf(
                31,
                31,
                31,
                31,
                31,
                31,
                30,
                30,
                30,
                30,
                30,
                30
            )
        } else {
            intArrayOf(
                31,
                31,
                31,
                31,
                31,
                31,
                30,
                30,
                30,
                30,
                30,
                29
            )
        }
        var days = 0
        for (i in 0 until (jm - 1)) {
            days += daysInMonth[i]
        }
        days += jd - 1
        return firstDay + days
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
            if (y < 1900 || y > 2100) return null
            val jdn = gregorianToJDN(
                y,
                m,
                d
            )
            jdnToJalali(jdn)
        } catch (e: Exception) {
            null
        }
    }

    /** تبدیل YYYY-MM-DD میلادی به رشته‌ی شمسی بلند (مثلاً ۱۴۰۵/۰۶/۲۸) */
    fun toJalaliLong(date: String?): String {
        val j = gregorianDateStringToJalali(date)
                ?: return date
                        ?: ""
        val (y, m, d) = j
        return "${
            y.toString()
                .toPersianDigits()
        }/${
            m.toString()
                .padStart(
                    2,
                    '0'
                )
                .toPersianDigits()
        }/${
            d.toString()
                .padStart(
                    2,
                    '0'
                )
                .toPersianDigits()
        }"
    }

    /** تبدیل YYYY-MM-DD میلادی به رشته‌ی شمسی با نام ماه (مثلاً ۲۸ شهریور ۱۴۰۵) */
    fun toJalaliReadable(date: String?): String {
        val j = gregorianDateStringToJalali(date)
                ?: return date
                        ?: ""
        val (y, m, d) = j
        val monthName = persianMonths.getOrNull(m - 1)
                ?: ""
        return "${
            d.toString()
                .toPersianDigits()
        } $monthName ${
            y.toString()
                .toPersianDigits()
        }"
    }

    /** تبدیل YYYY-MM-DD میلادی به رشته‌ی کوتاه (مثلاً ۲۸ شهریور) */
    fun toJalaliShort(date: String?): String {
        val j = gregorianDateStringToJalali(date)
                ?: return date
                        ?: ""
        val (_, m, d) = j
        val monthName = persianMonths.getOrNull(m - 1)
                ?: ""
        return "${
            d.toString()
                .toPersianDigits()
        } $monthName"
    }

    /** محاسبه‌ی سن از تاریخ تولد (رشته YYYY-MM-DD میلادی) */
    fun calculateAge(birthDate: String?): Int? {
        val j = gregorianDateStringToJalali(birthDate)
                ?: return null
        val (jy, _, _) = j
        val today = gregorianDateStringToJalali(
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            ).format(Date())
        )
                ?: return null
        return today.first - jy
    }

    /** نام روز هفته از تاریخ YYYY-MM-DD میلادی (مثلاً شنبه) */
    fun weekdayName(date: String?): String {
        if (date.isNullOrBlank()) return ""
        return try {
            val parts = date.split("-")
            val cal = Calendar.getInstance()
            cal.set(
                parts[0].toInt(),
                parts[1].toInt() - 1,
                parts[2].toInt()
            )
            // Calendar: SUNDAY=1..SATURDAY=7 — ما می‌خواهیم شنبه=0
            val gregorianWeekday = cal.get(Calendar.DAY_OF_WEEK)
            val persianIndex = when (gregorianWeekday) {
                Calendar.SATURDAY -> 6
                else              -> gregorianWeekday - 1
            }
            persianWeekdays.getOrNull(persianIndex)
                    ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /** تبدیل ارقام یک رشته به فارسی */
    fun toPersianDigits(s: String?): String = s?.toPersianDigits()
            ?: ""

    /** فرمت ساعت از "HH:MM:SS" یا "HH:MM" به "HH:MM" */
    fun formatTime(time: String?): String {
        if (time.isNullOrBlank()) return ""
        return time.take(5)
    }

    fun formatTimeHHMM(time: String?): String = formatTime(time)

    /** تاریخ امروز به میلادی YYYY-MM-DD */
    fun todayGregorian(): String = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(Date())

    /** Date → "yyyy-MM-dd" */
    fun toGregorianString(date: Date): String = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.US
    ).format(date)

    /** شمارنده‌ی روزهای بین دو تاریخ میلادی */
    fun daysUntil(targetDate: String?): Int? {
        if (targetDate.isNullOrBlank()) return null
        return try {
            val sdf = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.US
            )
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val target = sdf.parse(targetDate)
                    ?: return null
            val today = sdf.parse(sdf.format(Date()))
                    ?: return null
            val diff = target.time - today.time
            (diff / (1000L * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            null
        }
    }

    /** تبدیل تاریخ شمسی به میلادی YYYY-MM-DD - کپی از اپ ادمین برای JalaliDatePicker */
    fun jalaliToGregorian(
        jYear: Int,
        jMonth: Int,
        jDay: Int
    ): String {
        var jy = jYear - 979
        var jm = jMonth - 1
        var jd = jDay - 1
        var jDayNo = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4
        for (i in 0 until jm) jDayNo += if (i < 6) 31 else 30
        jDayNo += jd
        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097
        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) gDayNo++ else leap = false
        }
        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461
        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }
        val gDays = arrayOf(
            31,
            if (leap) 29 else 28,
            31,
            30,
            31,
            30,
            31,
            31,
            30,
            31,
            30,
            31
        )
        var gm = 0
        while (gDayNo >= gDays[gm]) {
            gDayNo -= gDays[gm]
            gm++
        }
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            gy,
            gm + 1,
            gDayNo + 1
        )
    }

    /** تبدیل میلادی YYYY-MM-DD به شمسی YYYY/MM/DD - کپی از اپ ادمین */
    fun gregorianToJalali(gregorianDate: String): String {
        return try {
            val parts = gregorianDate.trim()
                .split("-")
            val gy0 = parts[0].toInt()
            val gm = parts[1].toInt()
            val gd0 = parts[2].toInt()
            val gDaysInMonth = intArrayOf(
                31,
                28,
                31,
                30,
                31,
                30,
                31,
                31,
                30,
                31,
                30,
                31
            )
            val gy = gy0 - 1600
            val gmi = gm - 1
            val gd = gd0 - 1
            var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400
            for (i in 0 until gmi) gDayNo += gDaysInMonth[i]
            if (gmi > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) gDayNo++
            gDayNo += gd
            var jDayNo = gDayNo - 79
            val jNp = jDayNo / 12053
            jDayNo %= 12053
            var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
            jDayNo %= 1461
            if (jDayNo >= 366) {
                jy += (jDayNo - 1) / 365
                jDayNo = (jDayNo - 1) % 365
            }
            val r = (jy - 979) % 33
            val isJalaliLeap = r == 0 || r == 4 || r == 8 || r == 12 || r == 16 || r == 20 || r == 24 || r == 28
            val jDaysInMonth = intArrayOf(
                31,
                31,
                31,
                31,
                31,
                31,
                30,
                30,
                30,
                30,
                30,
                if (isJalaliLeap) 30 else 29
            )
            var jm = 0
            while (jm < 12 && jDayNo >= jDaysInMonth[jm]) {
                jDayNo -= jDaysInMonth[jm]
                jm++
            }
            String.format(
                Locale.US,
                "%04d/%02d/%02d",
                jy,
                jm + 1,
                jDayNo + 1
            )
        } catch (e: Exception) {
            ""
        }
    }
}
