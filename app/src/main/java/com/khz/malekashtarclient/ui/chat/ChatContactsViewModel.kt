package com.khz.malekashtarclient.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.ChatContact
import com.khz.malekashtarclient.ui.components.ListState
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
                    if (!result.data.isNullOrEmpty()) {  /* ادمین‌ها اول، سپس مربیان */
                        val sorted = result.data.sortedWith(
                            compareBy(
                                { it.role != "admin" },
                                { it.fullName })
                        )

//                        _state.value = ListState.Success(sorted)
                    }
                }

                is NetworkResult.Error   -> {

                    _state.value = ListState.Error(result.message)
                }

                else                     -> {
                    Unit
                }
            }
        }
    }
}
