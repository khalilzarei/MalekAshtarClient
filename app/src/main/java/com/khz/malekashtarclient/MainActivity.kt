package com.khz.malekashtarclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.navigation.RootNavGraph
import com.khz.malekashtarclient.ui.theme.MalekAshtarTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

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
}
