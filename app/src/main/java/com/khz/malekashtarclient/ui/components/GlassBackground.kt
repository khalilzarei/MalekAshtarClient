package com.khz.malekashtarclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.khz.malekashtarclient.ui.theme.PurpleBgDeep
import com.khz.malekashtarclient.ui.theme.PurpleBgMid
import com.khz.malekashtarclient.ui.theme.PurplePrimary

/**
 * پس‌زمینه‌ی اصلی اپ — گرادیان عمودی بنفش تیره
 *
 * استفاده: GlassBackground { ... محتوای صفحه ... }
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PurpleBgDeep,
                        PurpleBgMid,
                        PurplePrimary.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        content()
    }
}
