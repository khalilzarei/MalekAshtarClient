package com.khz.malekashtarclient.ui.classes

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassButton
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GenericListScreen
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * صفحه‌ی کلاس‌ها
 *
 * - لیست کلاس‌های فعال بازیکن
 * - دکمه‌ی «گفتگو با مربی» در هر کارت → ساخت room و هدایت به ChatScreen
 * - در صورت خطا یا عدم یافتن مربی، Snackbar ۳ ثانیه
 */
@Composable
fun ClassesScreen(
    onBack: () -> Unit,
    onOpenChat: (targetUserId: Int, roomId: Int) -> Unit
) {
    val viewModel: ClassesViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    var openingChat by remember { mutableStateOf(false) }
    var chatError by remember { mutableStateOf<String?>(null) }

    // پاک‌سازی خودکار Snackbar بعد از ۳ ثانیه
    LaunchedEffect(chatError) {
        if (chatError != null) {
            delay(3000)
            chatError = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GlassBackground {
            GenericListScreen(
                title = "کلاس‌ها",
                state = state.list,
                onRefresh = { viewModel.refresh() },
                onBack = onBack
            ) { klass ->
                ClassCard(
                    klass = klass,
                    openingChat = openingChat,
                    onChatWithCoach = {
                        val uid = viewModel.findCoachUserId(klass.coachName)
                        if (uid == null) {
                            chatError = "مربی در لیست مخاطبین یافت نشد"
                        } else {
                            scope.launch {
                                openingChat = true
                                chatError = null
                                val r = viewModel.openChatWithCoach(uid)
                                openingChat = false
                                when (r) {
                                    is NetworkResult.Success -> onOpenChat(uid, r.data.id)
                                    is NetworkResult.Error -> chatError = r.message
                                    is NetworkResult.Loading -> Unit
                                }
                            }
                        }
                    }
                )
            }
        }

        // Snackbar در BoxScope بیرونی (Modifier.align معتبر)
        chatError?.let { msg ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(msg)
            }
        }
    }
}

@Composable
private fun ClassCard(
    klass: MyClass,
    openingChat: Boolean,
    onChatWithCoach: () -> Unit
) {
    GlassCard3D {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = klass.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )

            if (!klass.ageGroupTitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = klass.ageGroupTitle,
                    color = GoldPrimary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.White.copy(0.5f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${klass.enrolledCount?.toString()?.toPersianDigits() ?: "?"} / ${klass.capacity?.toString()?.toPersianDigits() ?: "?"} نفر",
                    color = Color.White.copy(0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (!klass.location.isNullOrBlank()) {
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
                        text = klass.location,
                        color = Color.White.copy(0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // برنامه‌ی هفتگی
            if (klass.schedules.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                klass.schedules.forEach { sched ->
                    Text(
                        text = "${sched.weekdayLabel} ${sched.startTime} - ${sched.endTime}",
                        color = Color.White.copy(0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // مربی + دکمه‌ی گفتگو
            if (!klass.coachName.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "مربی: ${klass.coachName}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.clickable(onClick = onChatWithCoach)) {
                    GlassButton(
                        text = if (openingChat) "در حال اتصال..." else "گفتگو با مربی",
                        onClick = onChatWithCoach,
                        enabled = !openingChat
                    )
                }
            }
        }
    }
}
