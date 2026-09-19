package com.khz.malekashtarclient.ui.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.RedError

/**
 * صفحه‌ی خانه (داشبورد)
 *
 * - هدر با نام بازیکن + پیام خوش‌آمد
 * - کارت بازیکن (آواتار، نام، سن، کلاس، بَج بدهی)
 * - «جلسات پیش‌رو» (۲-۳ جلسه)
 * - دسترسی سریع: گفتگوها، اخبار، کلاس‌ها، صورت حساب، مسابقات، پروفایل
 */
@Composable
fun DashboardScreen(
    onNavigateToChats: () -> Unit,
    onNavigateToNews: () -> Unit,
    onNavigateToClasses: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToMatches: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: DashboardViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(title = "خانه", onBack = null)

            when {
                state.loading -> LoadingContent()
                state.error != null -> ErrorContent(
                    message = state.error!!,
                    onRetry = { viewModel.refresh() }
                )
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // کارت بازیکن
                    item {
                        PlayerCard(
                            playerName = state.child?.fullName ?: state.user?.fullName ?: "بازیکن",
                            avatarUrl = state.child?.avatarUrl ?: state.user?.avatarUrl,
                            className = state.child?.currentClass?.title,
                            debt = state.child?.balance?.debt ?: 0L
                        )
                    }

                    // جلسات پیش‌رو
                    if (state.upcoming.isNotEmpty()) {
                        item {
                            SectionTitle("جلسات پیش‌رو")
                        }
                        items(state.upcoming, key = { it.id }) { s ->
                            SessionCard(s)
                        }
                    }

                    // اخبار اخیر
                    if (state.recentNews.isNotEmpty()) {
                        item { SectionTitle("آخرین اخبار") }
                        items(state.recentNews, key = { it.id }) { n ->
                            DashboardNewsCard(
                                title = n.title,
                                dateText = DateUtils.toJalaliReadable(n.displayDate?.take(10))
                            )
                        }
                    }

                    // دسترسی سریع
                    item { SectionTitle("دسترسی سریع") }
                    item {
                        QuickAccessGrid(
                            unreadChats = state.unreadChats,
                            onChats = onNavigateToChats,
                            onNews = onNavigateToNews,
                            onClasses = onNavigateToClasses,
                            onFinance = onNavigateToFinance,
                            onMatches = onNavigateToMatches,
                            onProfile = onNavigateToProfile
                        )
                    }

                    // خروج
                    item {
                        Spacer(Modifier.height(16.dp))
                        androidx.compose.material3.TextButton(
                            onClick = onLogout,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "خروج از حساب",
                                color = Color(0xFFFF8A80),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ═══════ کامپوننت‌های داخلی داشبورد ═══════ */

@Composable
private fun PlayerCard(
    playerName: String,
    avatarUrl: String?,
    className: String?,
    debt: Long
) {
    GlassCard3D {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(
                name = playerName,
                avatarUrl = avatarUrl,
                size = 56.dp,
                accentColor = GoldPrimary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playerName.toPersianDigits(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                if (!className.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = className,
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (debt > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RedError.copy(alpha = 0.85f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "بدهی: ${debt.toPersianDigits()} ت",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
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
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SessionCard(s: MyScheduleItem) {
    GlassCard3D {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = s.classTitle,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = DateUtils.toJalaliReadable(s.sessionDate),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = s.startTime.take(5).toPersianDigits(),
                color = GoldPrimary,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
private fun DashboardNewsCard(title: String, dateText: String) {
    GlassCard3D {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2
            )
            if (dateText.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateText,
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun QuickAccessGrid(
    unreadChats: Int,
    onChats: () -> Unit,
    onNews: () -> Unit,
    onClasses: () -> Unit,
    onFinance: () -> Unit,
    onMatches: () -> Unit,
    onProfile: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickTile(Icons.Default.Chat, "گفتگوها", unreadChats, onClick = onChats, modifier = Modifier.weight(1f))
            QuickTile(Icons.Default.Notifications, "اخبار", badge = 0, onClick = onNews, modifier = Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickTile(Icons.Default.SportsSoccer, "کلاس‌ها", badge = 0, onClick = onClasses, modifier = Modifier.weight(1f))
            QuickTile(Icons.Default.Star, "صورت حساب", badge = 0, onClick = onFinance, modifier = Modifier.weight(1f))
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickTile(Icons.Default.CalendarMonth, "مسابقات", badge = 0, onClick = onMatches, modifier = Modifier.weight(1f))
            QuickTile(Icons.Default.AccountCircle, "پروفایل", badge = 0, onClick = onProfile, modifier = Modifier.weight(1f))
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
    GlassCard3D(modifier = modifier.clickable(onClick = onClick)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
