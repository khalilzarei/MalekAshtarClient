package com.khz.malekashtarclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.PurpleBgDeep

/**
 * نوار بالای صفحه با پس‌زمینه‌ی شیشه‌ای (گرادیان بنفش)
 *
 * امضای تغییرناپذیر طبق پرامپت:
 *   GlassTopBar(title, onBack?, actions?)
 */
@Composable
fun GlassTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PurpleBgDeep,
                        PurpleBgDeep.copy(alpha = 0.7f)
                    )
                )
            )
            .systemBarsPadding()
            .height(56.dp)
    ) {
        // دکمه‌ی بازگشت
        if (onBack != null) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = GoldPrimary
                )
            }
        }

        // عنوان
        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            maxLines = 1
        )

        // اکشن‌ها (سمت چپ در RTL)
        Box(
            modifier = Modifier.align(Alignment.CenterEnd),
            content = { actions() }
        )
    }
}
