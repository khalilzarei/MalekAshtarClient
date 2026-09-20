package com.khz.malekashtarclient.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.local.SessionManager
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.AuthRepository
import com.khz.malekashtarclient.data.repository.ClientRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel داشبورد
 *
 * ۴ فراخوانی موازی: me/children + me/schedule + me/news + chat/rooms
 * (me/finance و me/matches در dashboard لازم نیستند؛ در صفحات جداگانه لود می‌شوند)
 */
class DashboardViewModel(
    private val clientRepository: ClientRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(
            loading = true,
            error = null
        )
        viewModelScope.launch {
            // فراخوانی موازی چهارگانه
            val userDeferred = async { authRepository.me() }
            val childrenDeferred = async { clientRepository.myChildren() }
            val scheduleDeferred = async { clientRepository.mySchedule() }
            val newsDeferred = async { clientRepository.myNews() }
            val chatsDeferred = async {
                // برای unread count ما فقط نیاز به chat/rooms داریم (در گام چت اضافه می‌شود)
                // فعلاً ۰ می‌گذاریم تا داشبورد مستقل باشد
                NetworkResult.Success(emptyList<Any>())
            }

            coroutineScope {
                val user = userDeferred.await()
                val children = childrenDeferred.await()
                val schedule = scheduleDeferred.await()
                val news = newsDeferred.await()
                chatsDeferred.await()

                val newState = _state.value.copy(loading = false)

                // user
                val u = (user as? NetworkResult.Success)?.data
                if (u != null) newState.copy(user = u) else newState

                // children → اولین مورد
                val firstChild = (children as? NetworkResult.Success)?.data?.firstOrNull()

                // schedule → فقط scheduled آینده
                val allSessions = (schedule as? NetworkResult.Success)?.data
                // DEBUG: لاگ موقت برای بررسی
                allSessions?.forEach {
                    Log.d(
                        "Dashboard",
                        "session id=${it.id} date='${it.sessionDate}' time='${it.startTime}' status='${it.status}'"
                    )
                }
                val upcoming = allSessions?.filter { it.status == "scheduled" || it.status == "makeup" || it.status == null }
                    ?.sortedWith(
                        compareBy(
                        { it.sessionDate.ifBlank { "9999" } },
                        { it.startTime.ifBlank { "99:99:99" } }))
                    ?.take(5)
                        ?: emptyList()

                // news → ۳ تای آخر
                val recent = (news as? NetworkResult.Success)?.data?.take(3)
                        ?: emptyList()

                _state.value = _state.value.copy(
                    loading = false,
                    user = (user as? NetworkResult.Success)?.data,
                    child = firstChild,
                    upcoming = upcoming,
                    recentNews = recent,
                    unreadChats = 0, // در چت list جداگانه لود می‌شود
                    error = if (user is NetworkResult.Error && children is NetworkResult.Error && schedule is NetworkResult.Error && news is NetworkResult.Error) user.message else null
                )
            }
        }
    }

    suspend fun clearSession() {
        authRepository.logout()
    }
}
