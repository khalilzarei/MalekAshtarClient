package com.khz.malekashtarclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.PurplePrimary

/**
 * نمایش آواتار به‌صورت دایره‌ای
 * - اگر URL معتبر باشد → Coil لود می‌کند
 * - در غیر این صورت → حرف اول نام با گرادیان رنگ accent
 */
@Composable
fun AvatarView(
    name: String?,
    avatarUrl: String? = null,
    size: Dp = 48.dp,
    accentColor: Color = GoldPrimary
) {
    val safeUrl = avatarUrl?.takeIf { it.isNotBlank() }
    val initial = name?.trim()?.firstOrNull()?.toString() ?: "؟"
    val ctx = LocalContext.current

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        accentColor,
                        accentColor.copy(alpha = 0.4f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (safeUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(safeUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier.size(size)
            )
        } else {
            Text(
                text = initial,
                color = Color(0xFF1A0533),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
