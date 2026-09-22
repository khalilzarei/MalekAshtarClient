package com.khz.malekashtarclient.ui.news

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.LocalAppContainer
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.NewsItem
import com.khz.malekashtarclient.domain.model.NewsMedia
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.theme.BlueAccent
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import kotlinx.coroutines.launch

@Composable
fun NewsDetailScreen(
    newsId: Int,
    onBack: () -> Unit
) {
    val viewModel: NewsDetailViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(newsId) {
        viewModel.load(newsId)
    }

    val container = LocalAppContainer
    val token by produceState<String?>(initialValue = null) {
        value = container.sessionManager.getTokenSync()
    }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "جزئیات خبر",
                    onBack = onBack
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (val s = state) {
                    is NewsDetailState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = GoldPrimary)
                        }
                    }
                    is NewsDetailState.Error -> {
                        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(s.message, color = Color(0xFFFF8A80), style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.refresh() }) {
                                    Text("تلاش مجدد", color = GoldPrimary)
                                }
                            }
                        }
                    }
                    is NewsDetailState.Success -> {
                        NewsDetailContent(
                            news = s.news,
                            token = token
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsDetailContent(
    news: NewsItem,
    token: String?
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingMedia by remember { mutableStateOf<NewsMedia?>(null) }
    var previewMedia by remember { mutableStateOf<NewsMedia?>(null) }
    var videoToPlay by remember { mutableStateOf<NewsMedia?>(null) }

    fun startDownload(media: NewsMedia) {
        when (val result = NewsMediaDownloader.download(context, media, token)) {
            is NewsMediaDownloader.Result.Started -> statusMessage = "دانلود «${media.displayName}» شروع شد"
            is NewsMediaDownloader.Result.Failed -> statusMessage = result.message
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val media = pendingMedia
        pendingMedia = null
        if (granted && media != null) startDownload(media)
        else if (!granted) statusMessage = "برای ذخیره فایل، اجازه دسترسی لازم است"
    }

    fun requestDownload(media: NewsMedia) {
        val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        if (needsLegacyPermission) {
            pendingMedia = media
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else startDownload(media)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // اسلایدر بالا - جایگزین هدر
        if (news.hasMedia) {
            item {
                Spacer(Modifier.height(12.dp))
                MediaSliderWithDownload(
                    mediaList = news.media,
                    token = token,
                    onImageClick = { previewMedia = it },
                    onVideoClick = { videoToPlay = it },
                    onDownloadClick = { requestDownload(it) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        // عنوان خبر
        item {
            Text(
                text = news.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        // INFO CHIPS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.07f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, tint = GoldPrimary.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
                        Text(
                            DateUtils.toJalaliReadable(news.displayDate?.take(10)).toPersianDigits(),
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                if (news.hasMedia) {
                    Surface(
                        color = Color.White.copy(alpha = 0.07f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                if (news.videos.isNotEmpty()) Icons.Default.Videocam else Icons.Default.Image,
                                null,
                                tint = GoldPrimary.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                news.mediaSummary ?: "${news.media.size} فایل",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // BODY
        if (!news.body.isNullOrBlank()) {
            item {
                Text(
                    "متن خبر",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                GlassCard3D(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        news.body,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f
                        )
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        statusMessage?.let { msg ->
            item {
                GlassCard3D(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Text(msg, color = GoldPrimary, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }

    // پیش‌نمایش عکس
    previewMedia?.let { media ->
        Dialog(onDismissRequest = { previewMedia = null }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.92f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { previewMedia = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(Icons.Default.Close, "بستن", tint = Color.White)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        val fullUrl = media.streamUrl ?: media.downloadUrl
                        if (!fullUrl.isNullOrBlank()) {
                            AuthenticatedImage(
                                url = fullUrl,
                                token = token,
                                contentDescription = media.displayName,
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(media.displayName, color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        listOfNotNull(
                            if (media.isVideo) "فیلم" else "عکس",
                            media.humanSize.takeIf { it.isNotBlank() },
                            media.humanDuration
                        ).joinToString(" · ").toPersianDigits(),
                        color = Color.White.copy(0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            color = GoldPrimary.copy(0.15f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { requestDownload(media) }
                        ) {
                            Row(
                                Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Download, null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                Text("دانلود", color = GoldPrimary, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                        Surface(
                            color = Color.White.copy(0.1f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { previewMedia = null }
                        ) {
                            Text("بستن", color = Color.White, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
                        }
                    }
                }
            }
        }
    }

    // پخش ویدیو
    videoToPlay?.let { media ->
        val url = media.streamUrl ?: media.downloadUrl
        if (!url.isNullOrBlank()) {
            VideoPlayerDialog(
                videoUrl = url,
                token = token,
                onDismiss = { videoToPlay = null }
            )
        }
    }
}

@Composable
private fun MediaSliderWithDownload(
    mediaList: List<NewsMedia>,
    token: String?,
    onImageClick: (NewsMedia) -> Unit,
    onVideoClick: (NewsMedia) -> Unit,
    onDownloadClick: (NewsMedia) -> Unit,
    modifier: Modifier = Modifier
) {
    if (mediaList.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { mediaList.size })
    val scope = rememberCoroutineScope()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val media = mediaList[page]
                Box(
                    Modifier
                        .fillMaxSize()
                        .clickable {
                            if (media.isVideo) onVideoClick(media) else onImageClick(media)
                        }
                ) {
                    val url = media.thumbnailUrl ?: media.streamUrl ?: media.downloadUrl
                    if (!url.isNullOrBlank()) {
                        AuthenticatedImage(url = url, token = token, contentDescription = media.displayName, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize().background(Color.White.copy(0.05f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.BrokenImage, null, tint = Color.White.copy(0.3f), modifier = Modifier.size(32.dp))
                        }
                    }

                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.65f)))))

                    // دکمه پخش برای ویدیو
                    if (media.isVideo) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(
                                Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.55f))
                                    .border(1.5.dp, Color.White.copy(0.25f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
                            }
                        }
                        media.humanDuration?.let { dur ->
                            Surface(
                                color = Color.Black.copy(0.65f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                            ) {
                                Text(dur.toPersianDigits(), color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    // دکمه دانلود روی هر اسلاید - پایین چپ
                    IconButton(
                        onClick = { onDownloadClick(media) },
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .border(1.dp, Color.White.copy(0.2f), CircleShape)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "دانلود", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    // نام و شماره اسلاید - پایین وسط/راست
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(start = 56.dp, end = 12.dp, bottom = 12.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                media.displayName,
                                color = Color.White.copy(0.9f),
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("${page + 1}/${mediaList.size}".toPersianDigits(), color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // نوع فایل - بالا چپ
                    Surface(
                        color = if (media.isVideo) BlueAccent.copy(0.85f) else GoldPrimary.copy(0.85f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopStart).padding(10.dp)
                    ) {
                        Row(
                            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (media.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                if (media.isVideo) "ویدیو" else "عکس",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // دات‌های پایین
            if (mediaList.size > 1) {
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 56.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(0.35f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(mediaList.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            Modifier
                                .size(if (isSelected) 20.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) GoldPrimary else Color.White.copy(0.4f))
                        )
                    }
                }
            }
        }

        // اطلاعات رسانه فعلی + تامبنیل‌ها
        if (mediaList.size > 1) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                itemsIndexed(mediaList) { index, media ->
                    val isSelected = index == pagerState.currentPage
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(0.06f))
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) GoldPrimary else Color.White.copy(0.1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                    ) {
                        val url = media.thumbnailUrl ?: media.streamUrl ?: media.downloadUrl
                        if (!url.isNullOrBlank()) {
                            AuthenticatedImage(url = url, token = token, contentDescription = media.displayName, modifier = Modifier.fillMaxSize())
                        }
                        if (media.isVideo) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(0.55f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        // آیکون دانلود کوچک روی تامبنیل
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(2.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }
        }
    }
}
