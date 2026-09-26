package com.khz.malekclient.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.khz.malekclient.R

/**
 * تایپوگرافی فارسی اپ
 *
 * فونت اصلی: وزیر (Vazirmatn) — فونت آزاد فارسی
 * دانلود از: https://github.com/rastikerdar/vazirmatn
 *
 * فایل‌ها باید در res/font/ قرار داده شوند:
 *  - vazirmatn_regular.ttf
 *  - vazirmatn_medium.ttf
 *  - vazirmatn_semibold.ttf
 *  - vazirmatn_bold.ttf
 *
 * اندازه‌ها نسبت به نسخه‌ی قبلی بزرگ‌تر شده‌اند.
 */

val VazirmatnFont = FontFamily(
    Font(
        R.font.vazirmatn_regular,
        FontWeight.Normal
    ),
    Font(
        R.font.vazirmatn_medium,
        FontWeight.Medium
    ),
    Font(
        R.font.vazirmatn_bold,
        FontWeight.Bold
    ),
    Font(
        R.font.vazirmatn_black,
        FontWeight.Black
    )
)

val MalekAshtarTypography = Typography(
    // سرفصل‌ها
    displayLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    displayMedium = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    displaySmall = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp
    ),

    // عنوان‌ها
    headlineLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),

    // عنوان‌های متوسط
    titleLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    ),
    titleSmall = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),

    // متون اصلی (بزرگ‌تر شده)
    bodyLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp
    ),

    // لیبل‌ها
    labelLarge = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    labelMedium = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = VazirmatnFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
