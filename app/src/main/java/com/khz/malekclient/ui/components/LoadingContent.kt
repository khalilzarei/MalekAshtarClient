package com.khz.malekclient.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.khz.malekclient.ui.theme.GoldPrimary

/**
 * نمایش حالت بارگذاری
 *
 * استفاده:
 *   when (state) {
 *       is ListState.Loading -> LoadingContent()
 *       is ListState.Success -> ...
 *   }
 */
@Composable
fun LoadingContent(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = GoldPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(size)
        )
    }
}
