package com.khz.malekashtarclient.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.ListState
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.theme.GoldPrimary

/**
 * انتخاب مربی برای شروع گفتگوی خصوصی.
 *
 * منبع اطلاعات:
 *     GET /me/classes
 *
 * دیگر از ChatContact یا /me/chat-contacts استفاده نمی‌شود.
 */
@Composable
fun ChatContactsScreen(
    onBack: () -> Unit,
    onSelect: (coachUserId: Int) -> Unit
) {
    val viewModel: ChatContactsViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            GlassTopBar(
                title = "انتخاب مربی",
                onBack = onBack
            )

            when (val currentState = state) {

                is ListState.Loading -> {
                    LoadingContent()
                }

                is ListState.Error   -> {
                    ErrorContent(
                        message = currentState.message,
                        onRetry = {
                            viewModel.refresh()
                        })
                }

                is ListState.Success -> {

                    val coaches = currentState.items

                    if (coaches.isEmpty()) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "مربی‌ای برای گفتگو یافت نشد",
                                color = Color.White.copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            items(
                                items = coaches,
                                key = { it.userId }) { coach ->

                                CoachRow(
                                    coach = coach,
                                    onClick = {
                                        onSelect(coach.userId)
                                    })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachRow(
    coach: ChatContactsViewModel.CoachItem,
    onClick: () -> Unit
) {
    GlassCard3D(
        modifier = Modifier.clickable(
            onClick = onClick
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AvatarView(
                name = coach.fullName,
                avatarUrl = coach.avatarUrl,
                size = 48.dp,
                accentColor = GoldPrimary
            )

            Spacer(
                Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = coach.fullName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                if (coach.classTitle.isNotBlank()) {

                    Text(
                        text = "مربی - ${coach.classTitle}",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {

                    Text(
                        text = "مربی",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
