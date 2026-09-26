package com.khz.malekclient.ui.auth

import com.khz.malekclient.domain.model.User

/**
 * وضعیت‌های صفحه‌ی ورود
 */
sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class Success(val user: User) : LoginUiState()

    /** اگر نقش بازیکن نباشد → این حالت نمایش داده می‌شود */
    data class WrongRole(val role: String?) : LoginUiState()
}

/**
 * وضعیت‌های صفحه‌ی تغییر رمز
 */
sealed class ChangePasswordUiState {
    data object Idle : ChangePasswordUiState()
    data object Loading : ChangePasswordUiState()
    data class Error(val message: String) : ChangePasswordUiState()
    data object Success : ChangePasswordUiState()
}
