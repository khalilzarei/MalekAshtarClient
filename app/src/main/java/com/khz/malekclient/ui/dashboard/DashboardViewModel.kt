package com.khz.malekclient.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.khz.malekclient.core.util.DateUtils
import com.khz.malekclient.core.local.SessionManager
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.data.repository.AuthRepository
import com.khz.malekclient.data.repository.ClientRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    private val gson = Gson()
    private var lastRefreshAt = 0L

    init {
        refresh()
    }

    fun refresh(force: Boolean = false) {
        // جلوگیری از فراخوانی دوبله هنگام init + LaunchedEffect هم‌زمان (800ms debounce)
        val now = System.currentTimeMillis()
        if (!force && now - lastRefreshAt < 800) return
        lastRefreshAt = now
        _state.value = _state.value.copy(
            loading = true,
            error = null
        )
        viewModelScope.launch {
            // فراخوانی موازی پنج‌گانه با finance
            val userDeferred = async { authRepository.me() }
            val childrenDeferred = async { clientRepository.myChildren() }
            val scheduleDeferred = async { clientRepository.mySchedule() }
            val newsDeferred = async { clientRepository.myNews() }
            val financeDeferred = async { clientRepository.myFinance() }
            val chatsDeferred = async {
                NetworkResult.Success(emptyList<Any>())
            }

            coroutineScope {
                val user = userDeferred.await()
                val children = childrenDeferred.await()
                val schedule = scheduleDeferred.await()
                val news = newsDeferred.await()
                val financeResult = financeDeferred.await()
                chatsDeferred.await()

                val firstChild = (children as? NetworkResult.Success)?.data?.firstOrNull()
                val allSessions = (schedule as? NetworkResult.Success)?.data
                val upcoming = allSessions?.filter {
                    isUpcoming(
                        it.sessionDate,
                        it.startTime
                    ) && (it.status == "scheduled" || it.status == "makeup" || it.status == "cancelled" || it.status == null)
                }
                    ?.sortedWith(
                        compareBy(
                            { it.sessionDate.ifBlank { "9999" } },
                            { it.startTime.ifBlank { "99:99:99" } })
                    )
                    ?.take(5)
                        ?: emptyList()

                val recent = (news as? NetworkResult.Success)?.data?.take(3)
                        ?: emptyList()
                val finance = (financeResult as? NetworkResult.Success)?.data?.firstOrNull()

                // یادآوری بدهی: فقط یک‌بار در هر نشست — فلگ در ViewModel نگه داشته می‌شود
                // (با برگشت از صفحات دیگر به داشبورد ریست نمی‌شود)
                val previous = _state.value
                val currentDebt = finance?.debt
                        ?: firstChild?.balance?.debt
                        ?: 0L
                val shouldShowDebtDialog = currentDebt > 0 && !previous.debtDialogShown

                // خطا فقط وقتی نمایش داده می‌شود که هیچ داده‌ای برای نمایش نداریم (حالت خالی)
                val hasAnyData = firstChild != null || recent.isNotEmpty() || upcoming.isNotEmpty()
                val firstError = (user as? NetworkResult.Error)?.message
                        ?: (children as? NetworkResult.Error)?.message
                        ?: (schedule as? NetworkResult.Error)?.message
                        ?: (news as? NetworkResult.Error)?.message

                if (hasAnyData) {
                    val newState = previous.copy(
                        loading = false,
                        user = (user as? NetworkResult.Success)?.data,
                        child = firstChild,
                        finance = finance,
                        upcoming = upcoming,
                        recentNews = recent,
                        unreadChats = 0,
                        isFromCache = false,
                        showDebtDialog = if (shouldShowDebtDialog) true else previous.showDebtDialog,
                        debtDialogShown = if (shouldShowDebtDialog) true else previous.debtDialogShown,
                        error = null
                    )
                    _state.value = newState
                    // ذخیره‌ی کش آفلاین (بی‌صدا)
                    try {
                        sessionManager.saveDashboardCache(gson.toJson(newState))
                    } catch (_: Exception) {
                    }
                } else {
                    // هیچ داده‌ای نداریم — سعی کن کش آفلاین را نشان بده
                    val cachedJson = try {
                        sessionManager.getDashboardCache()
                    } catch (_: Exception) {
                        null
                    }
                    if (!cachedJson.isNullOrBlank()) {
                        try {
                            val cached = gson.fromJson(
                                cachedJson,
                                DashboardUiState::class.java
                            )
                            _state.value = cached.copy(
                                loading = false,
                                isFromCache = true,
                                showDebtDialog = false,
                                error = firstError
                            )
                        } catch (_: Exception) {
                            _state.value = previous.copy(
                                loading = false,
                                user = (user as? NetworkResult.Success)?.data,
                                child = firstChild,
                                finance = finance,
                                upcoming = upcoming,
                                recentNews = recent,
                                unreadChats = 0,
                                isFromCache = false,
                                showDebtDialog = if (shouldShowDebtDialog) true else previous.showDebtDialog,
                                debtDialogShown = if (shouldShowDebtDialog) true else previous.debtDialogShown,
                                error = firstError
                            )
                        }
                    } else {
                        _state.value = previous.copy(
                            loading = false,
                            user = (user as? NetworkResult.Success)?.data,
                            child = firstChild,
                            finance = finance,
                            upcoming = upcoming,
                            recentNews = recent,
                            unreadChats = 0,
                            isFromCache = false,
                            showDebtDialog = if (shouldShowDebtDialog) true else previous.showDebtDialog,
                            debtDialogShown = if (shouldShowDebtDialog) true else previous.debtDialogShown,
                            error = firstError
                        )
                    }
                }
            }
        }
    }

    fun dismissDebtDialog() {
        _state.value = _state.value.copy(showDebtDialog = false)
    }

    private fun isUpcoming(
        sessionDate: String,
        startTime: String
    ): Boolean {
        if (sessionDate.isBlank()) return false
        val today = DateUtils.todayGregorian()
        if (sessionDate > today) return true
        if (sessionDate < today) return false

        val now = SimpleDateFormat(
            "HH:mm",
            Locale.US
        ).format(Date())
        return startTime.take(5)
            .let { it.length == 5 && it >= now }
    }

    suspend fun clearSession() {
        authRepository.logout()
    }
}
