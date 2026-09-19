package com.khz.malekashtarclient.ui.matches

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.MyMatch
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.GreenOnline
import com.khz.malekashtarclient.ui.theme.RedError

private enum class MatchTab(val label: String) {
    Upcoming("پیش‌رو"),
    Past("برگزارشده")
}

@Composable
fun MatchesScreen(onBack: () -> Unit) {
    val viewModel: MatchesViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    var tab by remember { mutableStateOf(MatchTab.Upcoming) }

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(title = "مسابقات", onBack = onBack)

            Column(modifier = Modifier.padding(top = 56.dp)) {
                // تب‌ها
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabChip(
                        text = MatchTab.Upcoming.label,
                        selected = tab == MatchTab.Upcoming,
                        onClick = { tab = MatchTab.Upcoming },
                        modifier = Modifier.weight(1f)
                    )
                    TabChip(
                        text = MatchTab.Past.label,
                        selected = tab == MatchTab.Past,
                        onClick = { tab = MatchTab.Past },
                        modifier = Modifier.weight(1f)
                    )
                }

                when (state) {
                    is com.khz.malekashtarclient.ui.components.ListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize()) { LoadingContent() }
                    }
                    is com.khz.malekashtarclient.ui.components.ListState.Error -> {
                        ErrorContent(
                            message = (state as com.khz.malekashtarclient.ui.components.ListState.Error).message,
                            onRetry = { viewModel.refresh() }
                        )
                    }
                    is com.khz.malekashtarclient.ui.components.ListState.Success -> {
                        val all = (state as com.khz.malekashtarclient.ui.components.ListState.Success<MyMatch>).items
                        val filtered = when (tab) {
                            MatchTab.Upcoming -> all.filter { it.status == "planned" || it.status == null }
                            MatchTab.Past -> all.filter { it.status == "completed" || it.status == "cancelled" }
                        }
                        if (filtered.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "موردی برای نمایش وجود ندارد",
                                    color = Color.White.copy(0.6f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filtered, key = { it.id }) { m -> MatchCard(m) }
                                item { Spacer(Modifier.height(40.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) GoldPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) GoldPrimary else Color.White.copy(0.7f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}

@Composable
private fun MatchCard(m: MyMatch) {
    GlassCard3D {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = m.title?.toPersianDigits() ?: "مسابقه #${m.id.toPersianDigits()}",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(m.status)
            }

            if (!m.opponentTeam.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "حریف: ${m.opponentTeam}",
                    color = Color.White.copy(0.85f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White.copy(0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = DateUtils.toJalaliReadable(m.matchDate),
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = m.matchTime.take(5).toPersianDigits(),
                    color = GoldPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (!m.location.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = m.location,
                        color = Color.White.copy(0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (!m.classTitle.isNullOrBlank() || !m.ageGroupTitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(m.classTitle, m.ageGroupTitle).joinToString(" - "),
                    color = Color.White.copy(0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // نتیجه
            if (m.hasResult) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenOnline.copy(alpha = 0.25f))
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "نتیجه: ${m.resultText}",
                        color = GreenOnline,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (text, color) = when (status) {
        "planned" -> "برنامه‌ریزی" to GoldPrimary
        "completed" -> "برگزارشده" to GreenOnline
        "cancelled" -> "لغوشده" to RedError
        else -> (status ?: "نامشخص") to Color.White.copy(0.6f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.25f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}
