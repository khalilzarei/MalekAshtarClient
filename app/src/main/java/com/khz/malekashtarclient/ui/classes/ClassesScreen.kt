package com.khz.malekashtarclient.ui.classes

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.Evaluation
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassButton
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.ListState
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import com.khz.malekashtarclient.ui.theme.PurplePrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ClassesScreen(
    onBack: () -> Unit,
    onOpenChat: (targetUserId: Int, roomId: Int) -> Unit
) {
    val viewModel: ClassesViewModel = appViewModel()
    val uiState by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var openingChat by remember { mutableStateOf(false) }
    var chatError by remember { mutableStateOf<String?>(null) }
    var selectedEval by remember { mutableStateOf<Evaluation?>(null) }

    LaunchedEffect(chatError) {
        if (chatError != null) {
            delay(3000)
            chatError = null
        }
    }

    // دیالوگ ارزیابی
    if (selectedEval != null) {
        EvaluationDetailDialog(
            evaluation = selectedEval!!,
            onDismiss = { selectedEval = null })
    }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "کلاس‌ها",
                    onBack = onBack
                )
            }) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                when (val s = uiState.classesState) {
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
                                    s.message,
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
                        if (s.items.isEmpty()) {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "کلاسی یافت نشد",
                                    color = Color.White.copy(0.6f)
                                )
                            }
                        } else {
                            val scheduleByClass = uiState.schedule.groupBy { it.classId }
                            val evalBySession = uiState.evaluations.groupBy { it.sessionId }

                            LazyColumn(
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 12.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    s.items,
                                    key = { it.id }) { klass ->
                                    val classSchedule = scheduleByClass[klass.id]
                                            ?: emptyList()
                                    ClassCardWithSchedule(
                                        klass = klass,
                                        upcomingSessions = classSchedule.sortedBy { it.sessionDate }
                                            .take(5),
                                        evalBySession = evalBySession,
                                        openingChat = openingChat,
                                        onEvaluationClick = { ev -> selectedEval = ev },
                                        onChatWithCoach = {
                                            val coachUserId = klass.coachUserId
                                            if (coachUserId == null || coachUserId <= 0) {
                                                chatError = "شناسه کاربری مربی پیدا نشد"
                                            } else {
                                                scope.launch {
                                                    openingChat = true
                                                    chatError = null
                                                    when (val result = viewModel.openChatWithCoach(coachUserId)) {
                                                        is NetworkResult.Success -> {
                                                            openingChat = false
                                                            onOpenChat(
                                                                coachUserId,
                                                                result.data.id
                                                            )
                                                        }

                                                        is NetworkResult.Error   -> {
                                                            openingChat = false
                                                            chatError = result.message
                                                        }

                                                        else                     -> Unit
                                                    }
                                                }
                                            }
                                        })
                                }
                                item { Spacer(Modifier.height(80.dp)) }
                            }
                        }
                    }
                }

                chatError?.let { message ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text(text = message)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassCardWithSchedule(
    klass: MyClass,
    upcomingSessions: List<MyScheduleItem>,
    evalBySession: Map<Int?, List<Evaluation>>,
    openingChat: Boolean,
    onEvaluationClick: (Evaluation) -> Unit,
    onChatWithCoach: () -> Unit
) {
    GlassCard3D(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = klass.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    klass.ageGroupTitle?.takeIf { it.isNotBlank() }
                        ?.let {
                            Text(
                                text = it,
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                }
            }

            klass.coachName?.takeIf { it.isNotBlank() }
                ?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            null,
                            tint = GoldPrimary.copy(0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "مربی: $it",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            klass.location?.takeIf { it.isNotBlank() }
                ?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            null,
                            tint = GoldPrimary.copy(0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = it,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

            if (klass.schedules.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "برنامه هفتگی:",
                        color = GoldPrimary.copy(0.9f),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    klass.schedules.forEach { sch ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                sch.weekdayLabel,
                                color = Color.White.copy(0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "${sch.startTime.take(5)} - ${sch.endTime.take(5)}".toPersianDigits(),
                                color = GoldPrimary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (upcomingSessions.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "جلسات پیش‌رو:",
                            color = GoldPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    upcomingSessions.forEach { session ->
                        val evals = evalBySession[session.id]
                                ?: emptyList()
                        val hasEval = evals.isNotEmpty()
                        val eval = evals.firstOrNull()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (hasEval) GoldPrimary.copy(0.08f) else Color.White.copy(0.06f))
                                .border(
                                    0.5.dp,
                                    if (hasEval) GoldPrimary.copy(0.25f) else Color.White.copy(0.08f),
                                    RoundedCornerShape(10.dp)
                                )
                                .then(if (hasEval && eval != null) Modifier.clickable { onEvaluationClick(eval) } else Modifier)
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 10.dp
                                ),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        DateUtils.toJalaliReadable(session.sessionDate)
                                            .toPersianDigits(),
                                        color = Color.White.copy(0.85f),
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    if (hasEval) {
                                        Box(
                                            Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldPrimary.copy(0.2f))
                                                .padding(
                                                    horizontal = 6.dp,
                                                    vertical = 2.dp
                                                )
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    null,
                                                    tint = GoldPrimary,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                                Text(
                                                    "ارزیابی",
                                                    color = GoldPrimary,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = androidx.compose.ui.unit.TextUnit.Unspecified,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    session.topic?.takeIf { it.isNotBlank() }
                                        ?.let {
                                            Text(
                                                it,
                                                color = Color.White.copy(0.5f),
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        }
                                    session.location?.takeIf { it.isNotBlank() }
                                        ?.let {
                                            Text(
                                                "· $it",
                                                color = Color.White.copy(0.4f),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                }
                                if (hasEval && eval != null && eval.overallScore != null) {
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "نمره: ${
                                            eval.overallScore.toString()
                                                .toPersianDigits()
                                        } - برای جزئیات لمس کنید",
                                        color = GoldPrimary.copy(0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    session.startTime.take(5)
                                        .toPersianDigits(),
                                    color = GoldPrimary,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                session.status?.let {
                                    val statusText = when (it) {
                                        "scheduled" -> "برنامه‌ریزی شده"
                                        "completed" -> "برگزار شده"
                                        "makeup"    -> "جبرانی"
                                        else        -> it
                                    }
                                    Text(
                                        statusText,
                                        color = Color.White.copy(0.5f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    "جلسه‌ای در ۱۴ روز آینده ثبت نشده",
                    color = Color.White.copy(0.4f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (klass.capacity != null || klass.enrolledCount != null) {
                Text(
                    text = buildString {
                        klass.enrolledCount?.let {
                            append("ثبت‌نام: ").append(
                                it.toString()
                                    .toPersianDigits()
                            )
                        }
                        klass.capacity?.let {
                            if (isNotEmpty()) append(" / ")
                            append("ظرفیت: ").append(
                                it.toString()
                                    .toPersianDigits()
                            )
                        }
                    },
                    color = Color.White.copy(0.5f),
                    style = MaterialTheme.typography.labelSmall
                )
            }

            GlassButton(
                text = if (openingChat) "در حال اتصال..." else "گفتگو با مربی",
                onClick = onChatWithCoach,
                enabled = !openingChat,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EvaluationDetailDialog(
    evaluation: Evaluation,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    "ارزیابی مربی",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                evaluation.sessionDate?.let {
                    Text(
                        DateUtils.toJalaliReadable(it)
                            .toPersianDigits(),
                        color = Color.White.copy(0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                evaluation.coachName?.let {
                    Text(
                        "مربی: $it",
                        color = GoldPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    if (evaluation.overallScore != null || evaluation.technicalScore != null) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "نمرات:",
                                color = GoldPrimary,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                evaluation.overallScore?.let {
                                    ScoreChip(
                                        "کل",
                                        it
                                    )
                                }
                                evaluation.technicalScore?.let {
                                    ScoreChip(
                                        "فنی",
                                        it
                                    )
                                }
                                evaluation.disciplineScore?.let {
                                    ScoreChip(
                                        "انضباط",
                                        it
                                    )
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                evaluation.physicalScore?.let {
                                    ScoreChip(
                                        "بدنی",
                                        it
                                    )
                                }
                                evaluation.teamworkScore?.let {
                                    ScoreChip(
                                        "تیمی",
                                        it
                                    )
                                }
                            }
                        }
                    }
                }
                evaluation.strengths?.takeIf { it.isNotBlank() }
                    ?.let { strengths ->
                        item {
                            GlassInfoBox(
                                title = "نقاط قوت",
                                content = strengths,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                evaluation.weaknesses?.takeIf { it.isNotBlank() }
                    ?.let { weaknesses ->
                        item {
                            GlassInfoBox(
                                title = "نقاط ضعف",
                                content = weaknesses,
                                color = Color(0xFFFF9800)
                            )
                        }
                    }
                evaluation.notes?.takeIf { it.isNotBlank() }
                    ?.let { notes ->
                        item {
                            GlassInfoBox(
                                title = "توضیح مربی",
                                content = notes,
                                color = GoldPrimary
                            )
                        }
                    }
                if (evaluation.strengths.isNullOrBlank() && evaluation.weaknesses.isNullOrBlank() && evaluation.notes.isNullOrBlank() && evaluation.overallScore == null) {
                    item {
                        Text(
                            "مربی هنوز توضیحی ثبت نکرده",
                            color = Color.White.copy(0.5f),
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
        containerColor = PurplePrimary.copy(alpha = 0.95f)
    )
}

@Composable
private fun ScoreChip(
    label: String,
    score: Int
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GoldPrimary.copy(0.15f))
            .border(
                0.5.dp,
                GoldPrimary.copy(0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(
                horizontal = 10.dp,
                vertical = 6.dp
            )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                color = Color.White.copy(0.6f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                score.toString()
                    .toPersianDigits(),
                color = GoldPrimary,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun GlassInfoBox(
    title: String,
    content: String,
    color: Color
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.08f))
            .border(
                0.5.dp,
                color.copy(0.2f),
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            title,
            color = color,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            content.toPersianDigits(),
            color = Color.White.copy(0.85f),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
