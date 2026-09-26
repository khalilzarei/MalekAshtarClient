package com.khz.malekclient.ui.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.khz.malekclient.core.util.DateUtils
import com.khz.malekclient.core.util.appViewModel
import com.khz.malekclient.core.util.toPersianDigits
import com.khz.malekclient.domain.model.MyMatch
import com.khz.malekclient.ui.components.ErrorContent
import com.khz.malekclient.ui.components.GlassBackground
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.components.GlassTopBar
import com.khz.malekclient.ui.components.LoadingContent
import com.khz.malekclient.ui.theme.GlassBorder
import com.khz.malekclient.ui.theme.GoldPrimary
import com.khz.malekclient.ui.theme.GreenOnline
import com.khz.malekclient.ui.theme.PurplePrimary
import com.khz.malekclient.ui.theme.RedError

private enum class MatchTab(val label: String) {
    Upcoming("پیش‌رو"),
    Past("برگزارشده"),
    Invited("دعوت شده‌ام")
}

@Composable
fun MatchesScreen(onBack: () -> Unit) {
    val viewModel: MatchesViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    var tab by remember { mutableStateOf(MatchTab.Upcoming) }
    var selectedMatch by remember { mutableStateOf<MyMatch?>(null) }

    if (selectedMatch != null) {
        MatchDetailDialog(
            match = selectedMatch!!,
            onDismiss = { selectedMatch = null })
    }

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(
                title = "مسابقات",
                onBack = onBack
            )

            Column(modifier = Modifier.padding(top = 56.dp)) {
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
                    TabChip(
                        text = MatchTab.Invited.label,
                        selected = tab == MatchTab.Invited,
                        onClick = { tab = MatchTab.Invited },
                        modifier = Modifier.weight(1f)
                    )
                }

                when (state) {
                    is com.khz.malekclient.ui.components.ListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize()) { LoadingContent() }
                    }

                    is com.khz.malekclient.ui.components.ListState.Error   -> {
                        ErrorContent(
                            message = (state as com.khz.malekclient.ui.components.ListState.Error).message,
                            onRetry = { viewModel.refresh() })
                    }

                    is com.khz.malekclient.ui.components.ListState.Success -> {
                        val all = (state as com.khz.malekclient.ui.components.ListState.Success<MyMatch>).items
                        val filtered = when (tab) {
                            MatchTab.Upcoming -> all.filter { it.status == "planned" || it.status == "scheduled" || it.status == null }
                            MatchTab.Past     -> all.filter { it.status == "completed" || it.status == "cancelled" }
                            MatchTab.Invited  -> all.filter { it.isInvited }
                        }
                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.SportsSoccer,
                                        null,
                                        tint = Color.White.copy(0.3f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = when (tab) {
                                            MatchTab.Invited -> "هنوز به مسابقه‌ای دعوت نشده‌اید"
                                            else             -> "موردی برای نمایش وجود ندارد"
                                        },
                                        color = Color.White.copy(0.6f),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    filtered,
                                    key = { it.id }) { m ->
                                    MatchCard(m) { selectedMatch = m }
                                }
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
private fun TabChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) GoldPrimary.copy(0.3f) else Color.White.copy(0.08f))
            .border(
                0.5.dp,
                if (selected) GoldPrimary.copy(0.4f) else GlassBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) GoldPrimary else Color.White.copy(0.7f),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        )
    }
}

@Composable
private fun MatchCard(
    m: MyMatch,
    onClick: () -> Unit
) {
    GlassCard3D(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldPrimary.copy(0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SportsSoccer,
                            null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = m.title?.toPersianDigits()
                                    ?: "مسابقه #${m.id.toPersianDigits()}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                        if (!m.opponentTeam.isNullOrBlank()) {
                            Text(
                                text = "حریف: ${m.opponentTeam}",
                                color = Color.White.copy(0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                StatusBadge(m.status)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        null,
                        tint = Color.White.copy(0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = DateUtils.toJalaliReadable(m.matchDate)
                            .toPersianDigits(),
                        color = Color.White.copy(0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    text = m.matchTime.take(5)
                        .toPersianDigits(),
                    color = GoldPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (!m.location.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
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
                Text(
                    text = listOfNotNull(
                        m.classTitle,
                        m.ageGroupTitle
                    ).joinToString(" - "),
                    color = Color.White.copy(0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // وضعیت دعوت بازیکن
            if (m.isInvited) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val inviteText = when (m.invitationStatus) {
                        "invited" -> "دعوت شده‌اید"
                        "accepted" -> "پذیرفته شده"
                        "declined" -> "رد شده"
                        else -> m.invitationStatus
                                ?: "دعوت"
                    }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldPrimary.copy(0.15f))
                            .border(
                                0.5.dp,
                                GoldPrimary.copy(0.3f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 3.dp
                            )
                    ) {
                        Text(
                            inviteText,
                            color = GoldPrimary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    m.position?.takeIf { it.isNotBlank() }
                        ?.let {
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(0.08f))
                                    .padding(
                                        horizontal = 6.dp,
                                        vertical = 3.dp
                                    )
                            ) {
                                Text(
                                    "پست: $it",
                                    color = Color.White.copy(0.7f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    m.jerseyNumber?.let {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(0.08f))
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 3.dp
                                )
                        ) {
                            Text(
                                "شماره: ${
                                    it.toString()
                                        .toPersianDigits()
                                }",
                                color = Color.White.copy(0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                if (m.goals != null && m.goals > 0 || m.assists != null && m.assists > 0 || m.minutesPlayed != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        m.goals?.let {
                            if (it > 0) Text(
                                "گل: ${
                                    it.toString()
                                        .toPersianDigits()
                                }",
                                color = Color(0xFF81C784),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        m.assists?.let {
                            if (it > 0) Text(
                                "پاس گل: ${
                                    it.toString()
                                        .toPersianDigits()
                                }",
                                color = GoldPrimary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        m.minutesPlayed?.let {
                            Text(
                                "دقیقه: ${
                                    it.toString()
                                        .toPersianDigits()
                                }",
                                color = Color.White.copy(0.6f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            if (m.hasResult) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenOnline.copy(alpha = 0.15f))
                        .border(
                            0.5.dp,
                            GreenOnline.copy(0.25f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "نتیجه: ${m.resultText?.toPersianDigits() ?: ""}",
                        color = GreenOnline,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Text(
                "برای جزئیات لمس کنید",
                color = Color.White.copy(0.35f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun MatchDetailDialog(
    match: MyMatch,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    match.title?.toPersianDigits()
                            ?: "مسابقه",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                match.opponentTeam?.let {
                    Text(
                        "حریف: $it",
                        color = GoldPrimary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                null,
                                tint = Color.White.copy(0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${
                                    DateUtils.toJalaliReadable(match.matchDate)
                                        .toPersianDigits()
                                } - ${
                                    match.matchTime.take(5)
                                        .toPersianDigits()
                                }",
                                color = Color.White.copy(0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        match.location?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    null,
                                    tint = Color.White.copy(0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    it,
                                    color = Color.White.copy(0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        if (!match.classTitle.isNullOrBlank() || !match.ageGroupTitle.isNullOrBlank()) {
                            Text(
                                listOfNotNull(
                                    match.classTitle,
                                    match.ageGroupTitle
                                ).joinToString(" - "),
                                color = Color.White.copy(0.5f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        StatusBadge(match.status)
                    }
                }
                if (match.hasResult) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GreenOnline.copy(0.15f))
                                .border(
                                    0.5.dp,
                                    GreenOnline.copy(0.25f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "نتیجه نهایی: ${match.resultText?.toPersianDigits()}",
                                color = GreenOnline,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                if (match.isInvited) {
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GoldPrimary.copy(0.08f))
                                .border(
                                    0.5.dp,
                                    GoldPrimary.copy(0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "وضعیت شما در این مسابقه:",
                                color = GoldPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            DetailRow(
                                "دعوت",
                                when (match.invitationStatus) {
                                    "invited" -> "دعوت شده‌اید"; "accepted" -> "پذیرفته‌اید"; "declined" -> "رد کرده‌اید"; else -> match.invitationStatus
                                        ?: "نامشخص"
                                }
                            )
                            match.position?.let {
                                DetailRow(
                                    "پست",
                                    it
                                )
                            }
                            match.jerseyNumber?.let {
                                DetailRow(
                                    "شماره پیراهن",
                                    it.toString()
                                        .toPersianDigits()
                                )
                            }
                            match.attendanceStatus?.let {
                                DetailRow(
                                    "حضور",
                                    it
                                )
                            }
                            match.minutesPlayed?.let {
                                DetailRow(
                                    "دقایق بازی",
                                    it.toString()
                                        .toPersianDigits()
                                )
                            }
                            match.goals?.let {
                                DetailRow(
                                    "گل",
                                    it.toString()
                                        .toPersianDigits()
                                )
                            }
                            match.assists?.let {
                                DetailRow(
                                    "پاس گل",
                                    it.toString()
                                        .toPersianDigits()
                                )
                            }
                            match.rating?.let {
                                DetailRow(
                                    "امتیاز",
                                    it.toString()
                                        .toPersianDigits()
                                )
                            }
                            if (match.yellowCards != null && match.yellowCards > 0) DetailRow(
                                "کارت زرد",
                                match.yellowCards.toString()
                                    .toPersianDigits()
                            )
                            if (match.redCards != null && match.redCards > 0) DetailRow(
                                "کارت قرمز",
                                match.redCards.toString()
                                    .toPersianDigits()
                            )
                            match.playerNotes?.takeIf { it.isNotBlank() }
                                ?.let {
                                    DetailRow(
                                        "توضیح مربی",
                                        it
                                    )
                                }
                        }
                    }
                }
                match.notes?.takeIf { it.isNotBlank() }
                    ?.let { notes ->
                        item {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(0.05f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "توضیحات مسابقه:",
                                    color = Color.White.copy(0.6f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    notes.toPersianDigits(),
                                    color = Color.White.copy(0.85f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                match.result?.takeIf { it.isNotBlank() && !match.hasResult }
                    ?.let { res ->
                        item {
                            Text(
                                "نتیجه: ${res.toPersianDigits()}",
                                color = Color.White.copy(0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "بستن",
                    color = GoldPrimary
                )
            }
        },
        containerColor = PurplePrimary.copy(alpha = 0.96f)
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            color = Color.White.copy(0.55f),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            value.toPersianDigits(),
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val (text, color) = when (status) {
        "planned", "scheduled" -> "برنامه‌ریزی" to GoldPrimary
        "completed"            -> "برگزارشده" to GreenOnline
        "cancelled"            -> "لغوشده" to RedError
        else                   -> (status
                ?: "نامشخص") to Color.White.copy(0.6f)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.25f))
            .padding(
                horizontal = 10.dp,
                vertical = 4.dp
            )
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}
