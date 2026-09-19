package com.khz.malekashtarclient.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khz.malekashtarclient.core.util.appViewModel
import com.khz.malekashtarclient.domain.model.User
import com.khz.malekashtarclient.ui.components.GlassButton
import com.khz.malekashtarclient.ui.components.GlassCard3D
import com.khz.malekashtarclient.ui.components.GlassTextField

/**
 * صفحه‌ی ورود
 *
 * فیلدها: کد ملی + رمز + «مرا به خاطر بسپار»
 * در صورت must_change_password → ChangePasswordScreen
 * اگر نقش غیر player → پیام «این اپ برای بازیکنان/والدین است» + خروج
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onRoleWrong: () -> Unit,
    onLogoutFromWrongRole: () -> Unit
) {
    val viewModel: AuthViewModel = appViewModel()
    val state by viewModel.loginState.collectAsState()

    var nationalCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }

    LaunchedEffect(state) {
        when (val s = state) {
            is LoginUiState.Success -> onLoginSuccess(s.user)
            is LoginUiState.WrongRole -> onRoleWrong()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .wrapContentSize(Alignment.Center)
    ) {
        GlassCard3D(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⚽", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "ورود به حساب",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "با کد ملی بازیکن وارد شوید",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                GlassTextField(
                    value = nationalCode,
                    onValueChange = { input ->
                        nationalCode = input.filter { ch -> ch.isDigit() }.take(10)
                    },
                    label = "کد ملی بازیکن",
                    placeholder = "مثلاً ۱۲۳۴۵۶۷۸۹۰",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    enabled = state !is LoginUiState.Loading
                )

                Spacer(Modifier.height(12.dp))

                GlassTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "رمز عبور",
                    placeholder = "رمز اولیه، کد ملی بازیکن است",
                    isPassword = true,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    enabled = state !is LoginUiState.Loading
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        enabled = state !is LoginUiState.Loading
                    )
                    Text(
                        text = "مرا به خاطر بسپار",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (state is LoginUiState.Error) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = (state as LoginUiState.Error).message,
                        color = Color(0xFFFF8A80),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                GlassButton(
                    text = if (state is LoginUiState.Loading) "در حال ورود..." else "ورود",
                    onClick = { viewModel.login(nationalCode, password) },
                    enabled = state !is LoginUiState.Loading
                )

                // حالت نقش نادرست: پیام بزرگ‌تر + دکمه خروج
                if (state is LoginUiState.WrongRole) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "این اپ برای بازیکنان/والدین است.\nلطفاً از اپ مدیریت استفاده کنید.",
                        color = Color(0xFFFF8A80),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    GlassButton(
                        text = "خروج",
                        onClick = onLogoutFromWrongRole
                    )
                }
            }
        }
    }
}
