package com.khz.malekclient.ui.splash

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khz.malekclient.MalekClientApp
import com.khz.malekclient.R
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.ui.components.GlassBackground
import com.khz.malekclient.ui.theme.GoldPrimary

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
    val container = (context.applicationContext as MalekClientApp).container
    val authRepo = container.authRepository
    val sessionManager = container.sessionManager
    // درخواست اجازه‌ی نوتیفیکیشن (فقط Android 13+) — برای پیام‌های چت
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* نتیجه را لازم نداریم — نوتیفیکیشن‌ساز خودِ manager آن را چک می‌کند */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                    "مدرسه فوتبال - مالک‌اشتر",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نسخه بازیکنان",
                    color = Color.White,
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "نسخه بازیکنان",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(28.dp))
                CircularProgressIndicator(color = GoldPrimary)
            }
        }
    }
}