package com.khz.malekashtarclient.ui.dashboard

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.LocalAppContainer
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.domain.model.NewsItem
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.news.AuthenticatedImage
import com.khz.malekashtarclient.ui.news.NewsSlider
import com.khz.malekashtarclient.ui.theme.GlassBorder
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.PurplePrimary
import com.khz.malekashtarclient.ui.theme.RedError
import com.khz.malekashtarclient.ui.theme.WhiteTransparent15
import com.khz.malekashtarclient.ui.theme.BlueAccent

@Composable
fun DashboardScreen(
    onNavigateToChats: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToNewsDetail: (Int) -> Unit = {},
    onNavigateToClasses: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: DashboardViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    val container = LocalAppContainer
    val token by produceState<String?>(initialValue = null) {
        value = container.sessionManager.getTokenSync()
    }

    val showLogoutDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    // هر بار که داشبورد نمایش داده می‌شود (بعد از برگشت از پروفایل) رفرش کن تا آواتار جدید دیده شود
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    if (showLogoutDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog.value = false },
            title = {
                Text(
                    "خروج از حساب",
                    color = Color.White
                )
            },
            text = {
                Text(
                    "آیا مطمئن هستید که می‌خواهید خارج شوید؟",
                    color = Color.White.copy(0.8f)
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showLogoutDialog.value = false
                    onLogout()
                }) {
                    Text(
                        "خروج",
                        color = RedError
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLogoutDialog.value = false }) {
                    Text(
                        "انصراف",
                        color = Color.White
                    )
                }
            },
            containerColor = PurplePrimary.copy(alpha = 0.95f)
        )
    }

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "خانه",
                onBack = null
            )

            when {
                state.loading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.refresh() })

                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // اسلایدر - بدون هدر، بک‌گراند شیشه‌ای روی عکس
                    if (state.recentNews.isNotEmpty()) {
                        item {
                            NewsSlider(
                                news = state.recentNews,
                                onSeeAll = onNavigateToNews,
                                onNewsClick = onNavigateToNewsDetail,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // کارت بازیکن - گروه سنی + ویرایش پروفایل + بدهی از finance
                    item {
                        PlayerCard(
                            playerName = state.child?.fullName
                                    ?: state.user?.fullName
                                    ?: "بازیکن",
                            avatarUrl = state.child?.avatarUrl
                                    ?: state.user?.avatarUrl,
                            ageGroupTitle = state.child?.currentClass?.ageGroupTitle
                                    ?: state.child?.currentClass?.title,
                            debt = state.finance?.debt
                                    ?: state.child?.balance?.debt
                                    ?: 0L,
                            isDebtor = state.finance?.isDebtor
                                    ?: ((state.finance?.debt
                                            ?: 0L) > 0 || (state.child?.balance?.debt
                                            ?: 0L) > 0),
                            classFees = state.finance?.classFees
                                    ?: emptyList(),
                            onClick = onNavigateToProfile
                        )
                    }

                    // اخبار افقی با عکس - زیر اسلایدر - کوچکتر + شیشه‌ای
                    if (state.recentNews.isNotEmpty()) {
                        item {
                            SectionHeaderWithAction(
                                title = "اخبار",
                                actionText = "همه",
                                onAction = onNavigateToNews
                            )
                            Spacer(Modifier.height(10.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(end = 8.dp)
                            ) {
                                items(
                                    state.recentNews,
                                    key = { it.id }) { news ->
                                    NewsHorizontalCardSmall(
                                        news = news,
                                        token = token,
                                        onClick = { onNavigateToNewsDetail(news.id) })
                                }
                            }
                        }
                    }

                    // کلاس پیش‌رو - فقط یکی + دکمه همه طلایی
                    if (state.upcoming.isNotEmpty()) {
                        item {
                            SectionHeaderWithAction(
                                title = "کلاس پیش‌رو",
                                actionText = "همه کلاس‌ها",
                                onAction = onNavigateToClasses,
                                actionColor = GoldPrimary
                            )
                            Spacer(Modifier.height(10.dp))
                            SessionCardSingle(
                                s = state.upcoming.first(),
                                onClick = onNavigateToClasses
                            )
                        }
                    }

                    // دسترسی سریع - فقط گفتگوها، صورت حساب، مسابقات (حذف اخبار/کلاس/پروفایل)
                    item {
                        SectionTitle("دسترسی سریع")
                        Spacer(Modifier.height(10.dp))
                        QuickAccessGrid(
                            unreadChats = state.unreadChats,
                            onChats = onNavigateToChats,
                            onFinance = onNavigateToFinance,
                            onMatches = onNavigateToMatches
                        )
                    }

                    // خروج - با دیالوگ تایید
                    item {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.TextButton(
                            onClick = { showLogoutDialog.value = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "خروج از حساب",
                                color = Color(0xFFFF8A80),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderWithAction(
    title: String,
    actionText: String,
    onAction: () -> Unit,
    actionColor: Color = GoldPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = GoldPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = actionText,
            color = actionColor,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAction() }
                .background(actionColor.copy(alpha = 0.12f))
                .border(
                    0.5.dp,
                    actionColor.copy(alpha = 0.25f),
                    RoundedCornerShape(8.dp)
                )
                .padding(
                    horizontal = 14.dp,
                    vertical = 6.dp
                ))
    }
}

@Composable
private fun NewsHorizontalCardSmall(
    news: NewsItem,
    token: String?,
    onClick: () -> Unit
) {
    // کوچکتر: 190dp عرض، 135dp ارتفاع کل
    GlassCard3D(
        modifier = Modifier
            .width(190.dp)
            .height(135.dp)
            .clickable(onClick = onClick),
        contentPadding = 0.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val cover = news.coverMedia
            if (cover != null) {
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
                        Modifier
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
                        Icon(
                            Icons.Default.Image,
                            null,
                            tint = Color.White.copy(0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    PurplePrimary.copy(0.8f),
                                    BlueAccent.copy(0.7f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Image,
                        null,
                        tint = Color.White.copy(0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // گرادیان برای خوانایی
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(0.2f),
                                Color.Black.copy(0.75f)
                            )
                        )
                    )
            )

            // آیکون ویدیو
            if (cover?.isVideo == true) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(0.55f))
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
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
                                PurplePrimary.copy(alpha = 0.4f)
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
                    .padding(
                        horizontal = 10.dp,
                        vertical = 8.dp
                    )
            ) {
                Column {
                    Text(
                        text = news.title,
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = DateUtils.toJalaliReadable(news.displayDate?.take(10))
                            .toPersianDigits(),
                        color = Color.White.copy(alpha = 0.65f),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
                    )
                }
            }

            // بَج تعداد رسانه بالا
            if (news.media.size > 1) {
                Box(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(0.55f))
                        .padding(
                            horizontal = 5.dp,
                            vertical = 2.dp
                        )
                ) {
                    Text(
                        "${news.media.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(
    playerName: String,
    avatarUrl: String?,
    ageGroupTitle: String?,
    debt: Long,
    isDebtor: Boolean,
    classFees: List<com.khz.malekashtarclient.domain.model.ClassFee>,
    onClick: () -> Unit
) {
    GlassCard3D(onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarView(
                    name = playerName,
                    avatarUrl = avatarUrl,
                    size = 64.dp,
                    accentColor = GoldPrimary
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerName.toPersianDigits(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(Modifier.height(3.dp))
                    if (!ageGroupTitle.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GoldPrimary.copy(0.15f))
                                    .border(
                                        0.5.dp,
                                        GoldPrimary.copy(0.25f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 3.dp
                                    )
                            ) {
                                Text(
                                    text = ageGroupTitle,
                                    color = GoldPrimary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (isDebtor) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RedError.copy(0.15f))
                                        .border(
                                            0.5.dp,
                                            RedError.copy(0.3f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(
                                            horizontal = 6.dp,
                                            vertical = 3.dp
                                        )
                                ) {
                                    Text(
                                        text = "بدهکار",
                                        color = RedError,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "بازیکن آکادمی",
                            color = Color.White.copy(0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (debt > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "بدهی کل: ${debt.toPersianDigits()} تومان",
                            color = RedError.copy(0.9f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
                // آخر ردیف - ویرایش پروفایل
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ویرایش پروفایل",
                            color = GoldPrimary.copy(0.9f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.AccountCircle,
                            null,
                            tint = GoldPrimary.copy(0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        tint = Color.White.copy(0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            // نمایش شهریه هر کلاس ثبت‌نام شده (خلاصه)
            if (classFees.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(0.04f))
                        .padding(10.dp)
                ) {
                    Text(
                        "شهریه کلاس‌ها:",
                        color = GoldPrimary.copy(0.8f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    classFees.take(3)
                        .forEach { fee ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    fee.classTitle.toPersianDigits(),
                                    color = Color.White.copy(0.7f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.weight(1f)
                                )
                                if (fee.debt > 0) {
                                    Text(
                                        "${fee.debt.toPersianDigits()} ت بدهی",
                                        color = RedError,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                } else {
                                    Text(
                                        "تسویه",
                                        color = Color(0xFF81C784),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    if (classFees.size > 3) {
                        Text(
                            "${
                                (classFees.size - 3).toString()
                                    .toPersianDigits()
                            } کلاس دیگر...",
                            color = Color.White.copy(0.4f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = GoldPrimary,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(
            top = 4.dp,
            start = 4.dp
        )
    )
}

@Composable
private fun SessionCardSingle(
    s: MyScheduleItem,
    onClick: () -> Unit
) {
    GlassCard3D(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GoldPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.classTitle,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = DateUtils.toJalaliReadable(s.sessionDate)
                        .toPersianDigits(),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                s.topic?.takeIf { it.isNotBlank() }
                    ?.let {
                        Text(
                            text = it,
                            color = Color.White.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = s.startTime.take(5)
                        .toPersianDigits(),
                    color = GoldPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                s.location?.takeIf { it.isNotBlank() }
                    ?.let {
                        Text(
                            text = it.take(12),
                            color = Color.White.copy(0.5f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    }
            }
        }
    }
}

@Composable
private fun QuickAccessGrid(
    unreadChats: Int,
    onChats: () -> Unit,
    onFinance: () -> Unit,
    onMatches: () -> Unit
) {
    // تک ستون، هر آیتم یک ردیف با آیکن + تایتل
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        QuickTileRow(
            Icons.Default.Chat,
            "گفتگوها",
            badge = unreadChats,
            onClick = onChats
        )
        QuickTileRow(
            Icons.Default.Star,
            "صورت حساب",
            badge = 0,
            onClick = onFinance
        )
        QuickTileRow(
            Icons.Default.CalendarMonth,
            "مسابقات",
            badge = 0,
            onClick = onMatches
        )
    }
}

@Composable
private fun QuickTileRow(
    icon: ImageVector,
    label: String,
    badge: Int,
    onClick: () -> Unit
) {
    GlassCard3D(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GoldPrimary.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) {
                BadgedBox(badge = {
                    if (badge > 0) {
                        Badge(containerColor = RedError) {
                            Text(
                                text = badge.toPersianDigits(),
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }) {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = GoldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.PlayArrow,
                null,
                tint = Color.White.copy(0.25f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun QuickTile(
    icon: ImageVector,
    label: String,
    badge: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // نگه داشته شده برای سازگاری قدیمی (استفاده نمی‌شود)
    GlassCard3D(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BadgedBox(badge = {
                if (badge > 0) {
                    Badge(containerColor = RedError) {
                        Text(
                            text = badge.toPersianDigits(),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = GoldPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
