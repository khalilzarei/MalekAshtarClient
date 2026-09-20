package com.khz.malekashtarclient.ui.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.khz.malekashtarclient.core.util.DateUtils
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.core.util.toPersianDigits
import com.khz.malekashtarclient.domain.model.NewsItem
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTopBar
import com.khz.malekashtarclient.ui.components.GenericListScreen
import com.khz.malekashtarclient.ui.theme.GoldPrimary


@Composable
fun NewsListScreen(onBack: () -> Unit) {
    val viewModel: NewsListViewModel = appViewModel()
    val state by viewModel.state.collectAsState()
    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }

    GlassBackground {
        GenericListScreen(
            title = "اخبار",
            state = state,
            onRefresh = { viewModel.refresh() },
            onBack = onBack
        ) { news ->
            NewsRow(news) { selectedNews = news }
        }
    }

    selectedNews?.let { news ->
        NewsDetailDialog(news = news, onDismiss = { selectedNews = null })
    }
}

@Composable
private fun NewsRow(news: NewsItem, onClick: () -> Unit) {
    GlassCard3D(modifier = Modifier.clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = news.title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 2
            )
            if (!news.body.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = news.body,
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = DateUtils.toJalaliReadable(news.displayDate?.take(10)),
                color = GoldPrimary,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun NewsDetailDialog(news: NewsItem, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("بستن", color = GoldPrimary)
            }
        },
        title = {
            Text(
                text = news.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                if (!news.body.isNullOrBlank()) {
                    Text(
                        text = news.body,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = DateUtils.toJalaliReadable(news.displayDate?.take(10)),
                    color = GoldPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        containerColor = Color(0xFF1A0533),
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

