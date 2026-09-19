package com.khz.malekashtarclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.ui.theme.GlassBorder
import com.khz.malekashtarclient.ui.theme.PurplePrimary
import com.khz.malekashtarclient.ui.theme.WhiteTransparent15

/**
 * کارت شیشه‌ای ۳ بعدی — با حاشیه‌ی براق و ته‌رنگ بنفش
 *
 * امضای کلی:
 *   GlassCard3D(modifier, cornerRadius, onClick?) { ... محتوا ... }
 */
@Composable
fun GlassCard3D(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val baseModifier = modifier
        .clip(shape)
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    WhiteTransparent15,
                    PurplePrimary.copy(alpha = 0.25f)
                )
            )
        )
        .border(
            width = 1.dp,
            color = GlassBorder,
            shape = shape
        )
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
        )

    Box(modifier = baseModifier) {
        content()
    }
}
