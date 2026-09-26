package com.khz.malekclient.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
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
import com.khz.malekclient.core.util.appViewModel
import com.khz.malekclient.ui.components.GlassButton
import com.khz.malekclient.ui.components.GlassCard3D
import com.khz.malekclient.ui.components.GlassTextField
import com.khz.malekclient.ui.components.GlassTopBar

/**
 * صفحه‌ی تغییر رمز (اجباری در اولین ورود)
 */
@Composable
fun ChangePasswordScreen(
    onSuccess: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val viewModel: AuthViewModel = appViewModel()
    val state by viewModel.changePasswordState.collectAsState()

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is ChangePasswordUiState.Success) onSuccess()
    }

    Box(Modifier.fillMaxSize()) {
        if (onBack != null) {
            GlassTopBar(
                title = "تغییر رمز عبور",
                onBack = onBack
            )
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
                    Text(text = "🔒", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "تغییر رمز عبور",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "رمز اولیه، کد ملی بازیکن است.\nبرای امنیت حساب، آن را تغییر دهید.",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(20.dp))

                    GlassTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = "رمز فعلی (کد ملی)",
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                        enabled = state !is ChangePasswordUiState.Loading
                    )

                    Spacer(Modifier.height(12.dp))

                    GlassTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "رمز جدید (حداقل ۸ کاراکتر)",
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                        enabled = state !is ChangePasswordUiState.Loading
                    )

                    Spacer(Modifier.height(12.dp))

                    GlassTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "تکرار رمز جدید",
                        isPassword = true,
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        enabled = state !is ChangePasswordUiState.Loading
                    )

                    if (state is ChangePasswordUiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = (state as ChangePasswordUiState.Error).message,
                            color = Color(0xFFFF8A80),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    GlassButton(
                        text = if (state is ChangePasswordUiState.Loading) "در حال ذخیره..." else "تأیید",
                        onClick = {
                            viewModel.changePassword(oldPassword, newPassword, confirmPassword)
                        },
                        enabled = state !is ChangePasswordUiState.Loading
                    )
                }
            }
        }
    }
}
