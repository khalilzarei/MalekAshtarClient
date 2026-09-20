package com.khz.malekashtarclient.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassButton
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.theme.GoldPrimary

/**
 * صفحه‌ی پروفایل
 *
 * بخش ۱: حساب من (auth/me)
 *   - نام + نقش + آواتار (با دکمه‌ی ویرایش و حذف)
 *   - دکمه‌ی خروج
 * بخش ۲: کارت بازیکن (me/children)
 *   - نام + کد ملی + تاریخ تولد شمسی + سن + کلاس فعلی + وضعیت مالی
 */
@Composable
fun ProfileScreen(
    onLoggedOut: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: ProfileViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "پروفایل من",
                onBack = onBack
            )

            when {
                state.loading -> LoadingContent()
                state.error != null -> ErrorContent(state.error!!, onRetry = { viewModel.refresh() })
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
                    // بخش ۱: حساب من
                    item {
                        SectionTitle("حساب من")
                        AccountCard(
                            fullName = state.user?.fullName ?: "—",
                            role = state.user?.role ?: "player",
                            avatarUrl = state.user?.avatarUrl,
                            uploading = state.uploading,
                            onDeleteAvatar = { viewModel.deleteAvatar() }
                        )
                    }

                    // خروج
                    item {
                        Spacer(Modifier.height(8.dp))
                        GlassButton(
                            text = "خروج از حساب",
                            onClick = { viewModel.logout() }
                        )
                    }

                    // بخش ۲: کارت بازیکن
                    state.child?.let { child ->
                        item { SectionTitle("اطلاعات بازیکن") }
                        item { PlayerInfoCard(child) }
                    }

                    item { Spacer(Modifier.height(40.dp)) }
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
private fun AccountCard(
    fullName: String,
    role: String,
    avatarUrl: String?,
    uploading: Boolean,
    onDeleteAvatar: () -> Unit
) {
    GlassCard3D {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                AvatarView(
                    name = fullName,
                    avatarUrl = avatarUrl,
                    size = 64.dp,
                    accentColor = GoldPrimary
                )
                if (uploading) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName.toPersianDigits(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = when (role) {
                        "player" -> "بازیکن"
                        "coach" -> "مربی"
                        "admin" -> "مدیر"
                        else -> role
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!avatarUrl.isNullOrBlank()) {
                IconButton(onClick = onDeleteAvatar, enabled = !uploading) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف عکس",
                        tint = Color(0xFFFF8A80)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerInfoCard(child: com.khz.malekashtarclient.domain.model.MyChild) {
    GlassCard3D {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarView(
                    name = child.fullName,
                    avatarUrl = child.avatarUrl,
                    size = 48.dp,
                    accentColor = GoldPrimary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = child.fullName.toPersianDigits(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    if (!child.nationalCode.isNullOrBlank()) {
                        Text(
                            text = "کد ملی: ${child.nationalCode.toPersianDigits()}",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            InfoRow("تاریخ تولد", DateUtils.toJalaliReadable(child.birthDate))
            InfoRow("سن", child.age?.toString()?.toPersianDigits() ?: "—")
            InfoRow("کلاس فعلی", child.currentClass?.title ?: "—")

            if (child.balance != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "وضعیت مالی",
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.height(4.dp))
                InfoRow("کل پرداخت‌شده", "${child.balance.totalPaid.toPersianDigits()} تومان")
                if (child.balance.debt > 0) {
                    InfoRow("بدهی", "${child.balance.debt.toPersianDigits()} تومان", accent = Color(0xFFFF8A80))
                }
                if (child.balance.pendingPaymentsCount > 0) {
                    InfoRow("پرداخت در انتظار", child.balance.pendingPaymentsCount.toPersianDigits())
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, accent: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value.toPersianDigits(),
            color = accent,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun HorizontalDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}
