package com.khz.malekclient.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.data.repository.ClientRepository
import com.khz.malekclient.domain.model.ChatContact
import com.khz.malekclient.ui.components.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * مخاطبین قابل گفتگو (ادمین‌ها + مربیان کلاس‌های فرزندان).
 *
 * منبع: GET /me/chat-contacts
 * بازیکن می‌تواند با هر یک از این مخاطبین گفتگوی خصوصی
 * شروع کند (اگر قبلاً نباشد، سرور روم را می‌سازد).
 */
class ChatContactsViewModel(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ListState<ChatContact>>(
        ListState.Loading
    )

    val state: StateFlow<ListState<ChatContact>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = ListState.Loading
        viewModelScope.launch {
            when (val result = clientRepository.chatContacts()) {
                is NetworkResult.Success -> {
                    val list = result.data
                            ?: emptyList()
                    val sorted = list.sortedWith(
                        compareBy(
                            { it.role != "admin" },
                            { it.fullName })
                    )
                    val domain = sorted.map {
                        ChatContact(
                            userId = it.userId,
                            fullName = it.fullName
                                    ?: "کاربر",
                            role = it.role
                                    ?: "player",
                            avatarUrl = it.avatarUrl,
                            classTitle = it.classTitle
                        )
                    }
                    _state.value = ListState.Success(domain)
                }

                is NetworkResult.Error   -> {
                    _state.value = ListState.Error(result.message)
                }

                else                     -> Unit
            }
        }
    }
}
