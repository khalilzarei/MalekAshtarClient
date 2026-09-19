package com.khz.malekashtarclient.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

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
            com.khz.malekashtarclient.ui.auth.AuthViewModel::class.java ->
                com.khz.malekashtarclient.ui.auth.AuthViewModel(
                    container.authRepository,
                    container.sessionManager
                ) as T

            // Dashboard
            com.khz.malekashtarclient.ui.dashboard.DashboardViewModel::class.java ->
                com.khz.malekashtarclient.ui.dashboard.DashboardViewModel(
                    container.clientRepository,
                    container.authRepository,
                    container.sessionManager
                ) as T

            // Profile
            com.khz.malekashtarclient.ui.profile.ProfileViewModel::class.java ->
                com.khz.malekashtarclient.ui.profile.ProfileViewModel(
                    container.authRepository,
                    container.clientRepository,
                    container.sessionManager
                ) as T

            // News
            com.khz.malekashtarclient.ui.news.NewsListViewModel::class.java ->
                com.khz.malekashtarclient.ui.news.NewsListViewModel(
                    container.clientRepository
                ) as T

            // Classes
            com.khz.malekashtarclient.ui.classes.ClassesViewModel::class.java ->
                com.khz.malekashtarclient.ui.classes.ClassesViewModel(
                    container.clientRepository,
                    container.chatRepository
                ) as T

            // Finance
            com.khz.malekashtarclient.ui.finance.FinanceViewModel::class.java ->
                com.khz.malekashtarclient.ui.finance.FinanceViewModel(
                    container.clientRepository
                ) as T

            // Matches
            com.khz.malekashtarclient.ui.matches.MatchesViewModel::class.java ->
                com.khz.malekashtarclient.ui.matches.MatchesViewModel(
                    container.clientRepository
                ) as T

            // Chat Room List
            com.khz.malekashtarclient.ui.chat.ChatRoomListViewModel::class.java ->
                com.khz.malekashtarclient.ui.chat.ChatRoomListViewModel(
                    container.chatRepository
                ) as T

            // Chat Contacts
            com.khz.malekashtarclient.ui.chat.ChatContactsViewModel::class.java ->
                com.khz.malekashtarclient.ui.chat.ChatContactsViewModel(
                    container.clientRepository
                ) as T

            else -> throw IllegalArgumentException("ViewModel ناشناخته: ${modelClass.name}")
        }
    }
}
