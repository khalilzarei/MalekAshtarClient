package com.khz.malekashtarclient.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khz.malekashtarclient.FootballSchoolApp
import com.khz.malekashtarclient.R
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.ui.components.GlassBackground
import com.khz.malekashtarclient.ui.theme.GoldPrimary
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * صفحه‌ی Splash — بررسی token و هدایت
 *
 * فراخوانی auth/me برای:
 *  - اعتبارسنجی توکن
 *  - تشخیص must_change_password
 *  - تشخیص نقش (player باشد)
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToChangePassword: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as FootballSchoolApp).container
    val authRepo = container.authRepository
    val sessionManager = container.sessionManager

    LaunchedEffect(Unit) {
//        delay(2000.milliseconds)
        val token = sessionManager.getTokenSync()

        if (token.isNullOrBlank()) {
            onNavigateToLogin()
            return@LaunchedEffect
        }

        when (val r = authRepo.me()) {
            is NetworkResult.Success -> {
                val user = r.data
                when {
                    user.role != "player"   -> {
                        authRepo.logout()
                        onNavigateToLogin()
                    }

                    user.mustChangePassword -> onNavigateToChangePassword()
                    else                    -> onNavigateToDashboard()
                }
            }

            is NetworkResult.Error   -> {
                authRepo.logout()
                onNavigateToLogin()
            }

            is NetworkResult.Loading -> Unit
        }
    }

    GlassBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "لوگو",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "مدرسه فوتبال - ملک‌اشتر",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نسخه بازیکنان",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(color = GoldPrimary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    // فقط بخش UI رو بدون LaunchedEffect و container نشون بده
    GlassBackground {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "لوگو",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(32.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "مدرسه فوتبال - مالک‌اشتر",
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(color = GoldPrimary)
            }
        }
    }
}