package com.khz.malekashtarclient.core.util

import java.text.NumberFormat
import java.util.Locale

/**
 * ابزار قالب‌بندی مبالغ تومانی با جداکننده‌ی هزارگان فارسی
 *
 * سرور همیشه مبلغ را به تومان می‌فرستد (نه ریال).
 */
object CurrencyUtils {

    /** قالب‌بندی مبلغ به تومان: 1500000 → ۱٬۵۰۰٬۰۰۰ تومان */
    fun formatToman(amount: Long?): String {
        if (amount == null) return "۰ تومان"
        return "${amount.formatThousands()} تومان"
    }

    /** قالب‌بندی بدون واحد: 1500000 → ۱٬۵۰۰٬۰۰۰ */
    fun formatNumber(amount: Long?): String {
        if (amount == null) return "۰"
        return amount.formatThousands()
    }

    /** قالب‌بندی اعداد اعشاری (مثلاً درصد) */
    fun formatDecimal(value: Double?, decimals: Int = 0): String {
        if (value == null) return "۰"
        val nf = NumberFormat.getNumberInstance(Locale("fa"))
        nf.minimumFractionDigits = decimals
        nf.maximumFractionDigits = decimals
        return nf.format(value)
    }

    /** فقط رقم فارسی بدون جداکننده */
    fun toPersianDigits(value: Long?): String = (value ?: 0).toString().toPersianDigits()
}
