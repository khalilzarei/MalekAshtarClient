package com.khz.malekashtarclient.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.local.SessionManager
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.AuthRepository
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.MyChild
import com.khz.malekashtarclient.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel صفحه‌ی پروفایل
 *
 * دو بخش:
 *  - حساب من: از auth/me
 *  - کارت بازیکن: از me/children
 *
 * امکانات: تغییر آواتار، حذف آواتار، خروج
 */
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val clientRepository: ClientRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    data class State(
        val loading: Boolean = true,
        val user: User? = null,
        val child: MyChild? = null,
        val uploading: Boolean = false,
        val error: String? = null,
        val loggedOut: Boolean = false
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            val userResult = authRepository.me()
            val childrenResult = clientRepository.myChildren()

            _state.value = _state.value.copy(
                loading = false,
                user = (userResult as? NetworkResult.Success)?.data,
                child = (childrenResult as? NetworkResult.Success)?.data?.firstOrNull(),
                error = if (userResult is NetworkResult.Error && childrenResult is NetworkResult.Error)
                    userResult.message else null
            )
        }
    }

    fun uploadAvatar(file: File) {
        _state.value = _state.value.copy(uploading = true, error = null)
        viewModelScope.launch {
            when (val r = clientRepository.uploadAvatar(file)) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(uploading = false)
                    refresh()
                }
                is NetworkResult.Error -> _state.value = _state.value.copy(
                    uploading = false,
                    error = r.message
                )
                else -> {}
            }
        }
    }

    fun deleteAvatar() {
        _state.value = _state.value.copy(uploading = true, error = null)
        viewModelScope.launch {
            when (val r = clientRepository.deleteAvatar()) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(uploading = false)
                    refresh()
                }
                is NetworkResult.Error -> _state.value = _state.value.copy(
                    uploading = false,
                    error = r.message
                )
                else -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(loggedOut = true)
        }
    }
}
