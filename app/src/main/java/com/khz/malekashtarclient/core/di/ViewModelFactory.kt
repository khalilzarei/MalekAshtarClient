package com.khz.malekashtarclient.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.khz.malekashtarclient.ui.auth.AuthViewModel
import com.khz.malekashtarclient.ui.chat.ChatRoomListViewModel
import com.khz.malekashtarclient.ui.classes.ClassesViewModel
import com.khz.malekashtarclient.ui.dashboard.DashboardViewModel
import com.khz.malekashtarclient.ui.finance.FinanceViewModel
import com.khz.malekashtarclient.ui.matches.MatchesViewModel
import com.khz.malekashtarclient.ui.news.NewsListViewModel
import com.khz.malekashtarclient.ui.profile.ProfileViewModel

/**
 * Factory کلی برای ساخت ViewModelها از طریق ریپازیتوری‌های موجود در AppContainer.
 *
 * استفاده:
 *   val factory = ViewModelFactory(container)
 *   val vm: AuthViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            // Auth
            AuthViewModel::class.java         -> AuthViewModel(
                container.authRepository,
                container.sessionManager
            ) as T

            // Dashboard
            DashboardViewModel::class.java    -> DashboardViewModel(
                container.clientRepository,
                container.authRepository,
                container.sessionManager
            ) as T

            // Profile
            ProfileViewModel::class.java      -> ProfileViewModel(
                container.authRepository,
                container.clientRepository,
                container.sessionManager
            ) as T

            // News
            NewsListViewModel::class.java     -> NewsListViewModel(
                container.clientRepository
            ) as T

            // Classes
            ClassesViewModel::class.java      -> ClassesViewModel(
                container.clientRepository,
                container.chatRepository
            ) as T

            // Finance
            FinanceViewModel::class.java      -> FinanceViewModel(
                container.clientRepository
            ) as T

            // Matches
            MatchesViewModel::class.java      -> MatchesViewModel(
                container.clientRepository
            ) as T

            // Chat Room List
            ChatRoomListViewModel::class.java -> ChatRoomListViewModel(
                container.chatRepository
            ) as T

            else                              -> throw IllegalArgumentException("ViewModel ناشناخته: ${modelClass.name}")
        }
    }
}
