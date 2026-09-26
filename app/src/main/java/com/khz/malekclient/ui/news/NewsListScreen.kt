package com.khz.malekclient.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.khz.malekclient.core.util.DateUtils
import com.khz.malekclient.core.util.LocalAppContainer
import com.khz.malekclient.core.util.appViewModel
import com.khz.malekclient.core.util.toPersianDigits
import com.khz.malekclient.domain.model.NewsItem
import com.khz.malekclient.ui.components.GlassBackground
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.components.GlassSearchField
import com.khz.malekclient.ui.components.GlassTopBar
import com.khz.malekclient.ui.components.ListState
import com.khz.malekclient.ui.theme.BlueAccent
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.PurplePrimary

@Composable
fun NewsListScreen(
    onBack: () -> Unit,
    onNewsClick: (Int) -> Unit = {}
) {
    val viewModel: NewsListViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }

    val container = LocalAppContainer
    val token by produceState<String?>(initialValue = null) {
        value = container.sessionManager.getTokenSync()
    }

    val filteredItems = remember(
        state,
        query
    ) {
        if (state is ListState.Success) {
            val all = (state as ListState.Success<NewsItem>).items
            if (query.isBlank()) all
            else {
                val q = query.trim()
                all.filter {
                    it.title.contains(
                        q,
                        ignoreCase = true
                    ) || (it.body?.contains(
                        q,
                        ignoreCase = true
                    ) == true)
                }
            }
        } else emptyList()
    }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "اخبار",
                    onBack = onBack
                )
            }) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                ) {
                    GlassSearchField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = "جستجو در اخبار...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when (state) {
                    is ListState.Loading -> {
                        Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoldPrimary)
                        }
                    }

                    is ListState.Error   -> {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    (state as ListState.Error).message,
                                    color = Color(0xFFFF8A80),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.refresh() }) {
                                    Text(
                                        "تلاش مجدد",
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                    }

                    is ListState.Success -> {
                        if (filteredItems.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (query.isBlank()) "خبری برای نمایش وجود ندارد" else "نتیجه‌ای یافت نشد",
                                    color = Color.White.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    filteredItems,
                                    key = { it.id }) { news ->
                                    NewsRow(
                                        news = news,
                                        token = token
                                    ) {
                                        onNewsClick(news.id)
                                    }
                                }
                                item { Spacer(Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsRow(
    news: NewsItem,
    token: String?,
    onClick: () -> Unit
) {
    GlassCard3D(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = 0.dp
    ) {
        Column {
            val cover = news.coverMedia
            if (cover != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp
                            )
                        )
                ) {
                    val url = if (cover.isVideo) cover.thumbnailUrl else cover.streamUrl
                            ?: cover.downloadUrl
                    if (!url.isNullOrBlank()) {
                        AuthenticatedImage(
                            url = url,
                            token = token,
                            contentDescription = news.title,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
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
                                cover.displayName,
                                color = Color.White.copy(0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Black.copy(0.5f)
                                    )
                                )
                            )
                    )
                    if (cover.isVideo) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(
                                        1.dp,
                                        Color.White.copy(0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        cover.humanDuration?.let { dur ->
                            Text(
                                text = dur.toPersianDigits(),
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(
                                        horizontal = 6.dp,
                                        vertical = 2.dp
                                    )
                            )
                        }
                    }
                    if (news.media.size > 1) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                Modifier.padding(
                                    horizontal = 8.dp,
                                    vertical = 4.dp
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Image,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = news.mediaSummary
                                            ?: "${news.media.size} فایل",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            if (news.media.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 8.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    news.media.take(4)
                        .forEach { m ->
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .border(
                                        0.5.dp,
                                        Color.White.copy(0.08f),
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                val url = m.thumbnailUrl
                                        ?: m.streamUrl
                                        ?: m.downloadUrl
                                if (!url.isNullOrBlank()) {
                                    AuthenticatedImage(
                                        url = url,
                                        token = token,
                                        contentDescription = m.displayName,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (m.isVideo) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.5f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    if (news.media.size > 4) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+${news.media.size - 4}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = news.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2
                )
                if (!news.body.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = news.body,
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = GoldPrimary.copy(0.8f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = DateUtils.toJalaliReadable(news.displayDate?.take(10))
                            .toPersianDigits(),
                        color = GoldPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    news.mediaSummary?.let {
                        Text(
                            text = "· $it",
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthenticatedImage(
    url: String,
    token: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
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
    SubcomposeAsyncImage(
        model = request,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    Modifier.size(18.dp),
                    color = GoldPrimary,
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BrokenImage,
                    null,
                    tint = Color.White.copy(0.3f)
                )
            }
        })
}
