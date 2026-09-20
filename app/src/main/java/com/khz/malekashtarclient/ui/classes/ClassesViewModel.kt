package com.khz.malekashtarclient.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.ChatRepository
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.ui.components.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClassesViewModel(
    private val clientRepository: ClientRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow<ListState<MyClass>>(
            ListState.Loading
        )

    val state: StateFlow<ListState<MyClass>> =
        _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {

        _state.value = ListState.Loading

        viewModelScope.launch {

            when (
                val result = clientRepository.myClasses()
            ) {

                is NetworkResult.Success -> {
                    _state.value =
                        ListState.Success(result.data)
                }

                is NetworkResult.Error -> {
                    _state.value =
                        ListState.Error(result.message)
                }

                else -> Unit
            }
        }
    }

    /**
     * ساخت یا دریافت گفتگوی خصوصی با مربی.
     *
     * coachUserId باید ID کاربر مربی باشد،
     * نه coach_id دیتابیس کلاس و نه player_id.
     */
    suspend fun openChatWithCoach(
        coachUserId: Int
    ) = chatRepository.getOrCreatePrivateRoom(
        coachUserId
    )
}