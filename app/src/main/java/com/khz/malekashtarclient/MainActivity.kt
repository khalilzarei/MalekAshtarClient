package com.khz.malekashtarclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.navigation.RootNavGraph
import com.khz.malekashtarclient.ui.theme.MalekAshtarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MalekAshtarTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.ui.graphics.Color.Transparent
                ) {
                    GlassBackground {
                        // NavGraph اصلی (ساخته خواهد شد در گام ۱۲)
                        // فعلاً یک placeholder می‌گذاریم تا پروژه کامپایل شود
                        RootNavGraph()
                    }
                }
            }
        }
    }
}
