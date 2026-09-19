package com.khz.malekashtarclient.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.ChatContact
import com.khz.malekashtarclient.ui.components.AvatarView
import com.khz.malekashtarclient.ui.components.ErrorContent
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.LoadingContent
import com.khz.malekashtarclient.ui.theme.GoldPrimary

/**
 * صفحه‌ی انتخاب مخاطب برای گفتگوی جدید
 */
@Composable
fun ChatContactsScreen(
    onBack: () -> Unit,
    onSelect: (contact: ChatContact) -> Unit
) {
    val viewModel: ChatContactsViewModel = appViewModel()
    val state by viewModel.state.collectAsState()

    GlassBackground {
        Box(Modifier.fillMaxSize()) {
            GlassTopBar(title = "انتخاب مخاطب", onBack = onBack)

            when (state) {
                is com.khz.malekashtarclient.ui.components.ListState.Loading -> {
                    LoadingContent()
                }
                is com.khz.malekashtarclient.ui.components.ListState.Error -> {
                    ErrorContent(
                        message = (state as com.khz.malekashtarclient.ui.components.ListState.Error).message,
                        onRetry = { viewModel.refresh() }
                    )
                }
                is com.khz.malekashtarclient.ui.components.ListState.Success -> {
                    val contacts = (state as com.khz.malekashtarclient.ui.components.ListState.Success<ChatContact>).items
                    if (contacts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(top = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "مخاطبی برای گفتگو یافت نشد",
                                color = Color.White.copy(0.6f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 64.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(contacts, key = { it.userId }) { contact ->
                                ContactRow(contact) { onSelect(contact) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(contact: ChatContact, onClick: () -> Unit) {
    GlassCard3D(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarView(
                name = contact.fullName,
                avatarUrl = contact.avatarUrl,
                size = 48.dp,
                accentColor = GoldPrimary
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.fullName.toPersianDigits(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = when (contact.role) {
                        "coach" -> "مربی${if (!contact.classTitle.isNullOrBlank()) " - ${contact.classTitle}" else ""}"
                        "admin" -> "مدیر مدرسه"
                        else -> contact.role
                    },
                    color = Color.White.copy(0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
