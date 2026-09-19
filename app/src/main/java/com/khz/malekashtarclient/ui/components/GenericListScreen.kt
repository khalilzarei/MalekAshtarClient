package com.khz.malekashtarclient.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * صفحه‌ی لیستی عمومی با ۳ حالت (Loading / Success / Error)
 *
 * امضای تغییرناپذیر طبق پرامپت:
 *   - onItemClick داخل itemContent با Modifier.clickable
 *   - دکمه‌ی onAdd فقط اگر != null باشد، FAB نمایش داده می‌شود
 *   - همیشه روی GlassBackground سوار می‌شود
 */
@Composable
fun <T> GenericListScreen(
    title: String,
    state: ListState<T>,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        when (state) {
            is ListState.Loading -> LoadingContent()
            is ListState.Error -> ErrorContent(
                message = state.message,
                onRetry = onRefresh
            )
            is ListState.Success -> {
                if (state.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "موردی برای نمایش وجود ندارد",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 64.dp), // جا برای TopBar
                        contentPadding = PaddingValues(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items, key = { item -> itemKey(item) }) { item ->
                            itemContent(item)
                        }
                        item {
                            Spacer(Modifier.height(80.dp)) // فضای پایین FAB
                        }
                    }
                }
            }
        }

        // Top Bar
        GlassTopBar(
            title = title,
            onBack = onBack,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // دکمه‌ی افزودن
        if (onAdd != null && state is ListState.Success) {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = com.khz.malekashtarclient.ui.theme.GoldPrimary,
                contentColor = com.khz.malekashtarclient.ui.theme.GoldOn,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن")
            }
        }
    }
}

/** استخراج کلید یکتا از آیتم برای LazyColumn (پیش‌فرض: hashCode) */
@Suppress("UNCHECKED_CAST")
private fun <T> itemKey(item: T): Any = when (item) {
    is com.khz.malekashtarclient.domain.model.ChatRoom -> item.id
    is com.khz.malekashtarclient.domain.model.ChatContact -> item.userId
    is com.khz.malekashtarclient.domain.model.NewsItem -> item.id
    is com.khz.malekashtarclient.domain.model.MyClass -> item.id
    is com.khz.malekashtarclient.domain.model.MyInvoice -> item.id
    is com.khz.malekashtarclient.domain.model.MyMatch -> item.id
    is com.khz.malekashtarclient.domain.model.MyScheduleItem -> item.id
    is com.khz.malekashtarclient.domain.model.MyChild -> item.id
    else -> item.hashCode()
}
