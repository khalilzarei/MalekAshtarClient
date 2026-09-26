package com.khz.malekclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khz.malekclient.ui.theme.GlassBorder
import com.khz.malekclient.ui.theme.PurplePrimary
import com.khz.malekclient.ui.theme.WhiteTransparent15

/**
 * کارت شیشه‌ای ۳ بعدی — با حاشیه‌ی براق و ته‌رنگ بنفش
 *
 * تغییرات:
 *  - cornerRadius پیش‌فرض 20 → 24
 *  - padding داخلی پیش‌فرض بزرگ‌تر (16 → 18)
 */
@Composable
fun GlassCard3D(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 18.dp,
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
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}
