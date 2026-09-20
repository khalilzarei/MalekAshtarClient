package com.khz.malekashtarclient.ui.classes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ClassesScreen(
    onBack: () -> Unit,
    onOpenChat: (targetUserId: Int, roomId: Int) -> Unit
) {
    val viewModel: ClassesViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    val scope = rememberCoroutineScope()

    var openingChat by remember {
        mutableStateOf(false)
    }

    var chatError by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(chatError) {
        if (chatError != null) {
            delay(3000)
            chatError = null
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        GlassBackground {

            GenericListScreen(
                title = "کلاس‌ها",
                state = state,
                onRefresh = {
                    viewModel.refresh()
                },
                onBack = onBack
            ) { klass ->

                ClassCard(
                    klass = klass,
                    openingChat = openingChat,
                    onClick = {},
                    onChatWithCoach = {

                        val coachUserId = klass.coachUserId

                        if (coachUserId == null || coachUserId <= 0) {

                            chatError = "شناسه کاربری مربی پیدا نشد"

                        } else {

                            scope.launch {

                                openingChat = true
                                chatError = null

                                when (val result = viewModel.openChatWithCoach(
                                    coachUserId
                                )) {

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

                                    is NetworkResult.Loading -> Unit
                                }
                            }
                        }
                    })
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

@Composable
private fun ClassCard(
    klass: MyClass,
    openingChat: Boolean,
    onClick: () -> Unit,
    onChatWithCoach: () -> Unit
) {
    GlassCard3D(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        onClick = onClick
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = klass.title,
                fontWeight = FontWeight.Bold
            )

            klass.ageGroupTitle?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(text = it)
                }

            klass.coachName?.takeIf { it.isNotBlank() }
                ?.let {
                    Text(text = "مربی: $it")
                }

            klass.location?.takeIf { it.isNotBlank() }
                ?.let {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(text = it)
                    }
                }

            if (klass.schedules.isNotEmpty()) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(6.dp)
                    )

                    Text(
                        text = klass.schedules.joinToString(" • ") {
                            "${it.weekdayLabel} ${it.startTime}-${it.endTime}"
                        })
                }
            }

            if (klass.capacity != null || klass.enrolledCount != null) {

                Text(
                    text = buildString {

                        klass.enrolledCount?.let {
                            append("ثبت‌نام: ")
                            append(
                                it.toString()
                                    .toPersianDigits()
                            )
                        }

                        klass.capacity?.let {

                            if (length > 0) {
                                append(" / ")
                            }

                            append("ظرفیت: ")
                            append(
                                it.toString()
                                    .toPersianDigits()
                            )
                        }
                    })
            }

            GlassButton(
                text = if (openingChat) {
                    "در حال اتصال..."
                } else {
                    "گفتگو با مربی"
                },
                onClick = onChatWithCoach,
                enabled = !openingChat,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
