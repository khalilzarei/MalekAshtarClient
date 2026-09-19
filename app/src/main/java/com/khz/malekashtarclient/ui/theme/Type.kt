package com.khz.malekashtarclient.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * تایپوگرافی فارسی اپ
 *
 * فونت پیش‌فرض سیستم (SansSerif) برای متن فارسی مناسب است.
 * برای ارقام فارسی، از toPersianDigits استفاده می‌شود (در string format).
 */
private val Base = TextStyle(fontFamily = FontFamily.SansSerif)

val MalekAshtarTypography = Typography(
    displayLarge = Base.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 40.sp),
    displayMedium = Base.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp),
    displaySmall = Base.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp),

    headlineLarge = Base.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, lineHeight = 30.sp),
    headlineMedium = Base.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp),
    headlineSmall = Base.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp),

    titleLarge = Base.copy(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp),
    titleMedium = Base.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    titleSmall = Base.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),

    bodyLarge = Base.copy(fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp),
    bodyMedium = Base.copy(fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 22.sp),
    bodySmall = Base.copy(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp),

    labelLarge = Base.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp),
    labelMedium = Base.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium, lineHeight = 16.sp),
    labelSmall = Base.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp)
)
