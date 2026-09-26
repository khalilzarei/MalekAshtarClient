package com.khz.malekclient.ui.theme

/**
 * رنگ‌های شیشه‌ای (برای GlassCard3D، GlassTopBar و ...)
 *
 * این‌ها پالت ثابت هستند که در کامپوننت‌های شیشه‌ای استفاده می‌شوند.
 */
object GlassColors {
    /** رنگ پس‌زمینه‌ی کارت‌های شیشه‌ای (شفاف با ته‌رنگ بنفش) */
    val CardBackground = WhiteTransparent15

    /** رنگ حاشیه‌ی کارت‌های شیشه‌ای */
    val CardBorder = GlassBorder

    /** رنگ پس‌زمینه‌ی فیلدهای ورودی */
    val FieldBackground = WhiteTransparent08

    /** رنگ متن اصلی (سفید) */
    val TextPrimary = androidx.compose.ui.graphics.Color.White

    /** رنگ متن ثانویه (سفید کم‌رنگ) */
    val TextSecondary = androidx.compose.ui.graphics.Color(0xB3FFFFFF) // 70% سفید

    /** رنگ متن غیرفعال */
    val TextDisabled = androidx.compose.ui.graphics.Color(0x66FFFFFF) // 40% سفید

    /** رنگ خطا */
    val Error = RedError

    /** رنگ تأکید اصلی (طلایی) */
    val Accent = GoldPrimary

    /** رنگ تأکید ثانویه (آبی) */
    val AccentSecondary = BlueAccent
}
