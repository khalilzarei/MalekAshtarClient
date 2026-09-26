package com.khz.malekclient.ui.news

import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.khz.malekclient.core.util.DateUtils
import com.khz.malekclient.core.util.LocalAppContainer
import com.khz.malekclient.core.util.toPersianDigits
import com.khz.malekclient.domain.model.NewsItem
import com.khz.malekclient.domain.model.NewsMedia
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.theme.BlueAccent
import com.khz.malekclient.ui.theme.GlassBorder
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.PurplePrimary
import com.khz.malekclient.ui.theme.WhiteTransparent15
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun NewsSlider(
    news: List<NewsItem>,
    onSeeAll: () -> Unit,
    onNewsClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (news.isEmpty()) return

    val container = LocalAppContainer
    val token by produceState<String?>(initialValue = null) {
        value = container.sessionManager.getTokenSync()
    }

    val pagerState = rememberPagerState(pageCount = { news.size })

    LaunchedEffect(pagerState.pageCount) {
        if (pagerState.pageCount <= 1) return@LaunchedEffect
        while (true) {
            delay(AUTO_ADVANCE_MS.milliseconds)
            if (!pagerState.isScrollInProgress) {
                val next = (pagerState.currentPage + 1) % pagerState.pageCount
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            NewsSlideCard(
                news = news[page],
                token = token,
                onClick = { onNewsClick(news[page].id) })
        }

        if (news.size > 1) {
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(news.size) { index ->
                    val active = index == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(
                                width = if (active) 18.dp else 6.dp,
                                height = 6.dp
                            )
                            .clip(CircleShape)
                            .background(if (active) GoldPrimary else Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsSlideCard(
    news: NewsItem,
    token: String?,
    onClick: () -> Unit
) {
    GlassCard3D(
        onClick = onClick,
        contentPadding = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(COVER_HEIGHT)
        ) {
            // تصویر پس‌زمینه - تمام کارت
            NewsCover(
                news = news,
                token = token,
                modifier = Modifier.fillMaxSize()
            )

            // گرادیان تیره برای خوانایی
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )

            // بَج رسانه بالا
            if (news.hasMedia) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    Text(
                        text = news.mediaSummary.orEmpty(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // بخش شیشه‌ای پایین - تیتر روی عکس
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                WhiteTransparent15,
                                PurplePrimary.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .border(
                        0.5.dp,
                        GlassBorder,
                        RoundedCornerShape(
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = news.title,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = DateUtils.toJalaliReadable(news.displayDate?.take(10))
                                .toPersianDigits(),
                            color = Color.White.copy(alpha = 0.75f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        news.mediaSummary?.let { summary ->
                            Text(
                                text = " · $summary",
                                color = GoldPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCover(
    news: NewsItem,
    token: String?,
    modifier: Modifier = Modifier
) {
    val cover = news.coverMedia
    Box(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        when {
            cover == null -> BrandPlaceholder(
                title = news.title,
                modifier = Modifier.matchParentSize()
            )

            cover.isVideo -> VideoCover(
                media = cover,
                token = token,
                modifier = Modifier.matchParentSize()
            )

            else          -> AuthenticatedAsyncImage(
                url = cover.previewUrl,
                token = token,
                contentDescription = news.title,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun VideoCover(
    media: NewsMedia,
    token: String?,
    modifier: Modifier = Modifier
) {
    val inner = Modifier.fillMaxSize()
    Box(modifier = modifier) {
        if (!media.thumbnailUrl.isNullOrBlank()) {
            AuthenticatedAsyncImage(
                url = media.thumbnailUrl,
                token = token,
                contentDescription = media.displayName,
                modifier = inner
            )
        } else {
            val frame by produceState<ImageBitmap?>(
                initialValue = null,
                media.id,
                token
            ) {
                value = VideoFrameCache.load(
                    media,
                    token
                )
            }
            val bitmap = frame
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = media.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = inner
                )
            } else {
                BrandPlaceholder(
                    title = null,
                    modifier = inner
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "پخش فیلم",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        media.humanDuration?.let { duration ->
            Text(
                text = duration.toPersianDigits(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(
                        horizontal = 6.dp,
                        vertical = 2.dp
                    )
            )
        }
    }
}

@Composable
private fun BrandPlaceholder(
    title: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(
                    PurplePrimary,
                    BlueAccent
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title?.take(40)
                .orEmpty(),
            color = Color.White.copy(alpha = 0.9f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(24.dp)
        )
    }
}

private object VideoFrameCache {
    private const val MAX_ENTRIES = 12
    private val cache = LruCache<String, ImageBitmap>(MAX_ENTRIES)

    suspend fun load(
        media: NewsMedia,
        token: String?
    ): ImageBitmap? {
        val url = media.streamUrl
                ?: media.downloadUrl
                ?: return null
        cache.get(url)
            ?.let { return it }
        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                val headers = if (token.isNullOrBlank()) emptyMap() else mapOf("Authorization" to "Bearer $token")
                retriever.setDataSource(
                    url,
                    headers
                )
                val frame = retriever.getFrameAtTime(
                    1_000_000L,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                frame?.asImageBitmap()
                    ?.also {
                        cache.put(
                            url,
                            it
                        )
                    }
            } catch (e: Exception) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }
    }
}

@Composable
fun AuthenticatedAsyncImage(
    url: String?,
    token: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val request = remember(
        url,
        token
    ) {
        ImageRequest.Builder(context)
            .data(url)
            .apply {
                if (!token.isNullOrBlank()) addHeader(
                    "Authorization",
                    "Bearer $token"
                )
            }
            .crossfade(true)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

private const val AUTO_ADVANCE_MS = 5_000L
private val COVER_HEIGHT = 250.dp
