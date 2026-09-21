package com.khz.malekashtarclient.ui.news

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.LruCache
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.LocalAppContainer
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.NewsItem
import com.khz.malekashtarclient.domain.model.NewsMedia
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.theme.BlueAccent
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.PurplePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * اسلایدر اخبار بالای داشبورد.
 *
 * - با `HorizontalPager` (از `androidx.compose.foundation.pager` — بدون
 *   نیاز به وابستگی جدید) اخبار را افقی نشان می‌دهد
 * - هر ۵ ثانیه خودکار جلو می‌رود
 * - روی هر اسلاید که بزنید، دیالوگ جزئیات باز می‌شود که در آن
 *   **دانلود همه‌ی عکس‌ها و فیلم‌های آن خبر** ممکن است
 */
@Composable
fun NewsSlider(
    news: List<NewsItem>,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (news.isEmpty()) return

    val context = LocalContext.current

    // توکن برای دانلود و برای ساخت فریم پیش‌نمایش ویدیو
    val container = LocalAppContainer
    val token by produceState<String?>(initialValue = null) {
        value = container.sessionManager.getTokenSync()
    }

    val pagerState = rememberPagerState(pageCount = { news.size })

    // چرخش خودکار
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

    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }

    Column(modifier = modifier) {

        // ── سرتیتر ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "اخبار",
                color = GoldPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "همه",
                color = BlueAccent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSeeAll() }
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ))
        }

        // ── اسلایدر ──
        HorizontalPager(
            state = pagerState,
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            NewsSlideCard(
                news = news[page],
                token = token,
                onClick = { selectedNews = news[page] })
        }

        // ── نشانگر صفحه ──
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
                            .background(
                                if (active) GoldPrimary else Color.White.copy(alpha = 0.3f)
                            )
                    )
                }
            }
        }
    }

    // ── دیالوگ جزئیات + دانلود ──
    selectedNews?.let { item ->
        NewsDetailDialog(
            news = item,
            token = token,
            onDismiss = { selectedNews = null })
    }
}

/* ═══════════════════════════════════════════════════════════════
 |  کارت هر اسلاید
 ═══════════════════════════════════════════════════════════════ */

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
        Column {

            NewsCover(
                news = news,
                token = token,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(COVER_HEIGHT)
            )

            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = news.title,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DateUtils.toJalaliReadable(news.displayDate?.take(10))
                            .toPersianDigits(),
                        color = Color.White.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    news.mediaSummary?.let { summary ->
                        Text(
                            text = " · $summary",
                            color = GoldPrimary.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * ناحیه‌ی تصویر اسلاید.
 *  - عکس  → با Coil لود می‌شود (ImageLoader احراز هویت‌شده در
 *           FootballSchoolApp تنظیم شده است)
 *  - فیلم → تصویر پیش‌نمایش، یا فریم استخراج‌شده، یا جایگزین گرادیانی
 *           همیشه با بَج «پخش» و مدت زمان
 *  - بدون رسانه → گرادیان برند
 */
@Composable
private fun NewsCover(
    news: NewsItem,
    token: String?,
    modifier: Modifier = Modifier
) {
    val cover = news.coverMedia

    Box(
        modifier = modifier.clip(
            RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp
            )
        )
    ) {
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

        // بَج نوع رسانه در گوشه
        if (news.hasMedia) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = news.mediaSummary.orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
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

        // ۱) تصویر پیش‌نمایش سرور، اگر موجود باشد
        if (!media.thumbnailUrl.isNullOrBlank()) {
            AuthenticatedAsyncImage(
                url = media.thumbnailUrl,
                token = token,
                contentDescription = media.displayName,
                modifier = inner
            )
        } else {
            // ۲) استخراج فریم از خود ویدیو (سمت کلاینت، بدون نیاز به ffmpeg روی سرور)
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

        // بَج پخش + مدت زمان
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

/* ═══════════════════════════════════════════════════════════════
 |  دیالوگ جزئیات خبر + دانلود رسانه‌ها
 ═══════════════════════════════════════════════════════════════ */

@Composable
private fun NewsDetailDialog(
    news: NewsItem,
    token: String?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var pendingMedia by remember { mutableStateOf<NewsMedia?>(null) }

    fun startDownload(media: NewsMedia) {
        when (val result = NewsMediaDownloader.download(
            context,
            media,
            token
        )) {
            is NewsMediaDownloader.Result.Started -> statusMessage = "دانلود «${media.displayName}» شروع شد"

            is NewsMediaDownloader.Result.Failed  -> statusMessage = result.message
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val media = pendingMedia
        pendingMedia = null

        when {
            granted && media != null -> startDownload(media)
            !granted                 -> statusMessage = "برای ذخیره‌ی فایل، اجازه‌ی دسترسی به حافظه لازم است"
        }
    }

    /**
     * روی Android 9 و پایین‌تر، نوشتن در Downloads به مجوز نیاز دارد.
     * از Android 10 (scoped storage) به بعد DownloadManager خودش به
     * Downloads عمومی می‌نویسد و مجوزی لازم نیست.
     */
    fun requestDownload(media: NewsMedia) {
        val needsLegacyPermission = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED

        if (needsLegacyPermission) {
            pendingMedia = media
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            startDownload(media)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("بستن") }
        },
        title = {
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {

                news.body?.takeIf { it.isNotBlank() }
                    ?.let { body ->
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                if (!news.hasMedia) {
                    Text(
                        text = "این خبر فایل پیوستی ندارد.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Text(
                        text = "فایل‌های پیوست",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    news.media.forEach { media ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (media.isVideo) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = media.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                val details = listOfNotNull(
                                    if (media.isVideo) "فیلم" else "عکس",
                                    media.humanSize.takeIf { it.isNotBlank() },
                                    media.humanDuration
                                ).joinToString(" · ")

                                Text(
                                    text = details.toPersianDigits(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(onClick = { requestDownload(media) }) {
                                Text("دانلود")
                            }
                        }
                    }
                }

                statusMessage?.let { message ->
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldPrimary
                    )
                }
            }
        })
}

/* ═══════════════════════════════════════════════════════════════
 |  کش فریم پیش‌نمایش ویدیو
 ═══════════════════════════════════════════════════════════════ */

/**
 * استخراج و کش‌کردن یک فریم از ویدیو برای استفاده به‌عنوان پیش‌نمایش.
 *
 * چرا سمت کلاینت: برای ساخت thumbnail سمت سرور به ffmpeg نیاز است که روی
 * اکثر هاست‌های اشتراکی نصب نیست. `MediaMetadataRetriever` امکانات
 * خود اندروید است و می‌تواند از یک URL شبکه‌ای هم فریم بگیرد.
 *
 * کش در حافظه با ظرفیت محدود است تا اسلایدر در حافظه نشت نکند.
 */
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
                val headers = if (token.isNullOrBlank()) {
                    emptyMap()
                } else {
                    mapOf("Authorization" to "Bearer $token")
                }

                retriever.setDataSource(
                    url,
                    headers
                )

                val frame = retriever.getFrameAtTime(
                    1_000_000L, // حدود یک ثانیه داخل ویدیو
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
private fun AuthenticatedAsyncImage(
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
                if (!token.isNullOrBlank()) {
                    addHeader(
                        "Authorization",
                        "Bearer $token"
                    )
                }
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
private val COVER_HEIGHT = 170.dp
