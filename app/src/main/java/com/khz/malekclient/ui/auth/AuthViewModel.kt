package com.khz.malekclient.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekclient.core.local.SessionManager
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.core.util.Constants
import com.khz.malekclient.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel مشترک برای Login و ChangePassword
 *
 * هر دو متد login/changePassword در اپ این نقش، فقط با player سروکار دارند.
 * اگر نقش در پاسخ server متفاوت بود → LoginUiState.WrongRole.
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    @Suppress("unused") private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _changePasswordState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.Idle)
    val changePasswordState: StateFlow<ChangePasswordUiState> = _changePasswordState.asStateFlow()

    /** تلاش برای ورود */
    fun login(identifier: String, password: String) {
        if (identifier.isBlank() || password.isBlank()) {
            _loginState.value = LoginUiState.Error("کد ملی و رمز عبور را وارد کنید")
            return
        }
        _loginState.value = LoginUiState.Loading
        viewModelScope.launch {
            when (val r = authRepository.login(identifier.trim(), password)) {
                is NetworkResult.Success -> {
                    val u = r.data.user
                    if (u.role != Constants.ROLE_PLAYER) {
                        // نقش غیرمجاز: پاک‌سازی session و پیام
                        authRepository.logout()
                        _loginState.value = LoginUiState.WrongRole(u.role)
                    } else {
                        _loginState.value = LoginUiState.Success(u)
                    }
                }
                is NetworkResult.Error -> _loginState.value = LoginUiState.Error(r.message)
                is NetworkResult.Loading -> { /* never happens here */ }
            }
        }
    }

    /** تغییر رمز عبور */
    fun changePassword(oldPassword: String, newPassword: String, confirmation: String) {
        when {
            oldPassword.isBlank() -> {
                _changePasswordState.value = ChangePasswordUiState.Error("رمز فعلی را وارد کنید")
                return
            }
            newPassword.length < 8 -> {
                _changePasswordState.value = ChangePasswordUiState.Error("رمز جدید باید حداقل ۸ کاراکتر باشد")
                return
            }
            newPassword != confirmation -> {
                _changePasswordState.value = ChangePasswordUiState.Error("تکرار رمز با رمز جدید یکسان نیست")
                return
            }
            newPassword == oldPassword -> {
                _changePasswordState.value = ChangePasswordUiState.Error("رمز جدید باید با رمز فعلی فرق کند")
                return
            }
        }

        _changePasswordState.value = ChangePasswordUiState.Loading
        viewModelScope.launch {
            when (val r = authRepository.changePassword(oldPassword, newPassword, confirmation)) {
                is NetworkResult.Success -> _changePasswordState.value = ChangePasswordUiState.Success
                is NetworkResult.Error -> _changePasswordState.value = ChangePasswordUiState.Error(r.message)
                is NetworkResult.Loading -> { /* never */ }
            }
        }
    }

    /** ریست state به Idle (مثلاً بعد از نمایش Snackbar) */
    fun resetLoginState() { _loginState.value = LoginUiState.Idle }
    fun resetChangePasswordState() { _changePasswordState.value = ChangePasswordUiState.Idle }
}
