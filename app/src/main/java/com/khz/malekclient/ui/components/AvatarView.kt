package com.khz.malekclient.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.khz.malekclient.R
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.DesignTokens

private fun isDefaultAvatarUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return true
    val low = url.lowercase()
    return low.contains("default_avatars") || low.contains("player_default") || low.contains("user_default") || low.contains("default.png")
}

/**
 * نمایش آواتار به‌صورت دایره‌ای
 * - اگر URL پیش‌فرض سرور باشد (default_avatars/...) → هیچ درخواستی نمی‌رود، حرف اول نمایش داده می‌شود
 * - اگر URL واقعی باشد → Coil با کش دائمی (حافظه+دیسک) لود می‌کند، یک‌بار برای همیشه
 * - اگر Uri لوکال باشد → اولویت با آن (پیش‌نمایش بعد از کراپ)
 */
@Composable
fun AvatarView(
    name: String?,
    avatarUrl: String? = null,
    avatarUri: Any? = null, // برای پیش‌نمایش لوکال بعد از انتخاب عکس
    size: Dp = DesignTokens.Avatar,
    accentColor: Color = GoldPrimary
) {
    val rawUrl = avatarUrl?.takeIf { it.isNotBlank() }
    val wasServerDefault = remember(avatarUrl) {
        avatarUrl?.lowercase()
            ?.contains("default_avatars") == true || avatarUrl?.lowercase()
            ?.contains("player_default") == true || avatarUrl?.lowercase()
            ?.contains("user_default") == true
    }
    val safeUrl = if (isDefaultAvatarUrl(rawUrl)) null else rawUrl
    val initial = name?.trim()
        ?.firstOrNull()
        ?.toString()
            ?: "؟"
    val ctx = LocalContext.current

    // اگر Uri لوکال داریم، اولویت با آن است (برای نمایش فوری بعد از انتخاب)
    val modelData: Any? = avatarUri
            ?: safeUrl

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
            )
            .border(
                width = 2.dp,
                color = GoldPrimary.copy(alpha = 0.95f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (modelData != null) {
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(modelData)
                    .memoryCacheKey(modelData.toString())
                    .diskCacheKey(modelData.toString())
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .networkCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = name,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            if (wasServerDefault) {
                Image(
                    painter = painterResource(id = R.drawable.player_default),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape)
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
}
