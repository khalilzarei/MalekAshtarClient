package com.khz.malekclient.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.data.repository.ChatRepository
import com.khz.malekclient.domain.model.ChatRoom
import com.khz.malekclient.ui.components.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatRoomListViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ListState<ChatRoom>>(ListState.Loading)
    val state: StateFlow<ListState<ChatRoom>> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = ListState.Loading
        viewModelScope.launch {
            when (val r = chatRepository.rooms()) {
                is NetworkResult.Success -> _state.value = ListState.Success(r.data)
                is NetworkResult.Error -> _state.value = ListState.Error(r.message)
                else -> {}
            }
        }
    }

    /** مجموع unread برای badge داشبورد */
    fun unreadTotal(): Int = when (val s = _state.value) {
        is ListState.Success -> s.items.sumOf { it.unreadCount }
        else -> 0
    }
}
