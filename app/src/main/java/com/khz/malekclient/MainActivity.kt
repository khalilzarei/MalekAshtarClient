package com.khz.malekclient

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.khz.malekclient.core.notifications.ChatDeepLink
import com.khz.malekclient.core.notifications.ChatNotificationManager
import com.khz.malekclient.ui.components.GlassBackground
import com.khz.malekclient.ui.navigation.RootNavGraph
import com.khz.malekclient.ui.theme.MalekAshtarTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleChatDeepLink(intent)

        setContent {
            MalekAshtarTheme(darkTheme = true) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GlassBackground {
                        RootNavGraph()
                    }
                }
            }
        }
    }

    /** لمس نوتیفیکیشن وقتی اپ از قبل باز است (task فعلی) */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleChatDeepLink(intent)
    }

    /** برگشت اپ به فرانت‌گراند — چک فوری پیام‌های جدید */
    override fun onResume() {
        super.onResume()
        (application as? MalekClientApp)?.let { ChatNotificationManager.checkOnForeground(it) }
    }

    /**
     * اگر اینتنت حاوی شناسه‌ی اتاق چت است، آن را برای ناوبری
     * (بعد از Splash/لاگین) در ChatDeepLink نگه می‌داریم.
     */
    private fun handleChatDeepLink(intent: Intent?) {
        val roomId = intent?.getIntExtra(
            ChatNotificationManager.EXTRA_CHAT_ROOM_ID,
            0
        )
                ?: 0
        if (roomId > 0) {
            ChatDeepLink.pendingRoomId = roomId
            intent?.removeExtra(ChatNotificationManager.EXTRA_CHAT_ROOM_ID)
        }
    }
}