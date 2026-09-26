package com.khz.malekclient.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * تم اصلی اپ: همیشه حالت تیره، گرادیان بنفش، تأکید طلایی
 *
 * تمام رنگ‌ها hardcoded به تم شیشه‌ای داده شده در پرامپت هستند.
 * حالت سیستم (روشن/تیره) نادیده گرفته می‌شود.
 */
private val MalekAshtarColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOn,
    primaryContainer = PurplePrimary,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,

    secondary = BlueAccent,
    onSecondary = androidx.compose.ui.graphics.Color.White,

    background = PurpleBgDeep,
    onBackground = androidx.compose.ui.graphics.Color.White,

    surface = PurpleBgMid,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = PurplePrimary,
    onSurfaceVariant = androidx.compose.ui.graphics.Color.White,

    error = RedError,
    onError = androidx.compose.ui.graphics.Color.White,

    outline = GlassBorder,
    outlineVariant = WhiteTransparent15
)

@Composable
fun MalekAshtarTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // همیشه dark (طبق پرامپت)
    val colorScheme = MalekAshtarColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PurpleBgDeep.toArgb()
                window.navigationBarColor = PurpleBgDeep.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MalekAshtarTypography,
        content = content
    )
}
