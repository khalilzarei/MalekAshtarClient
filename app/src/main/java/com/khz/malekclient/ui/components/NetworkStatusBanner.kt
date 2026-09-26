package com.khz.malekclient.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.khz.malekclient.core.network.ConnectivityMonitor
import com.khz.malekclient.core.network.NetState
import com.khz.malekclient.core.util.LocalAppContainer

/**
 * بنر وضعیت شبکه — بالای همه‌ی صفحات نمایش داده می‌شود:
 *
 * - قرمز: اینترنت وصل نیست
 * - کهربایی: فیلترشکن/VPN روشن است
 *
 * در حالت ONLINE هیچ چیزی نمایش داده نمی‌شود.
 */
@Composable
fun NetworkStatusBanner(modifier: Modifier = Modifier) {
    val container = LocalAppContainer
    val netState by container.connectivity.state.collectAsState()

    val message = ConnectivityMonitor.messageFor(netState)
            ?: return

    val isOffline = netState == NetState.OFFLINE
    val bg = if (isOffline) Color(0xFFC62828) else Color(0xFFE65100)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (isOffline) "📡" else "🛡️",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
