package com.khz.malekclient.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.khz.malekclient.ui.components.AvatarView
import com.khz.malekclient.ui.theme.GoldPrimary


@Composable
internal fun TelegramChatBackdrop(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF211044),
                        Color(0xFF10283A),
                        Color(0xFF16062D)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val step = 56.dp.toPx()
                    val radius = 1.35.dp.toPx()
                    var row = 0
                    var y = step / 2f
                    while (y < size.height) {
                        var x = if (row % 2 == 0) step / 2f else step
                        while (x < size.width) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.025f),
                                radius = radius,
                                center = Offset(
                                    x,
                                    y
                                )
                            )
                            x += step
                        }
                        y += step
                        row++
                    }
                })
        content()
    }
}

/** نوار بالای چت با ساختار دو کپسولی شبیه تلگرام */
@Composable
internal fun TelegramChatTopBar(
    title: String,
    subtitle: String,
    avatarUrl: String?,
    onBack: () -> Unit,
    showCall: Boolean,
    callEnabled: Boolean,
    onCall: () -> Unit,
    showLock: Boolean,
    onSearch: () -> Unit = {},
    onMore: () -> Unit = {}
) {
    val profileShape = RoundedCornerShape(28.dp)
    val actionsShape = RoundedCornerShape(26.dp)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp)
                    .shadow(
                        8.dp,
                        actionsShape,
                        clip = false
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xC91B2731),
                                Color(0xA61A2731)
                            )
                        ),
                        shape = actionsShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = actionsShape
                    )
                    .padding(
                        horizontal = 2.dp,
                        vertical = 2.dp
                    )
            ) {
                IconButton(
                    onClick = onBack,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "بازگشت",
                        tint = Color.White.copy(alpha = 0.90f),
                        modifier = Modifier.size(21.dp)
                    )
                }

            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .shadow(
                        8.dp,
                        profileShape,
                        clip = false
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xC91B2731),
                                Color(0xA61A2731)
                            )
                        ),
                        shape = profileShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = profileShape
                    )
                    .padding(
                        horizontal = 4.dp,
                        vertical = 4.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    AvatarView(
                        name = title,
                        avatarUrl = avatarUrl,
                        size = 38.dp,
                        accentColor = GoldPrimary
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (showLock) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "قفل شده",
                                    tint = Color(0xFFFFA6A0),
                                    modifier = Modifier
                                        .padding(start = 5.dp)
                                        .size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = subtitle,
                            color = Color.White.copy(alpha = 0.62f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .width(52.dp)
                    .height(52.dp)
                    .shadow(
                        8.dp,
                        actionsShape,
                        clip = false
                    )
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xC91B2731),
                                Color(0xA61A2731)
                            )
                        ),
                        shape = actionsShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.10f),
                        shape = actionsShape
                    )
                    .padding(
                        horizontal = 2.dp,
                        vertical = 2.dp
                    )
            ) {
                IconButton(onClick = onCall) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "تماس با $title",
                        tint = if (callEnabled) {
                            Color.White.copy(alpha = 0.92f)
                        } else {
                            Color.White.copy(alpha = 0.32f)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

            }
        }
    }
}

