package com.khz.malekashtarclient.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.ui.components.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatContactsViewModel(
    private val clientRepository: ClientRepository
) : ViewModel() {

    data class CoachItem(
        val userId: Int,
        val fullName: String,
        val classTitle: String,
        val avatarUrl: String?
    )

    private val _state = MutableStateFlow<ListState<CoachItem>>(
        ListState.Loading
    )

    val state: StateFlow<ListState<CoachItem>> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {

        _state.value = ListState.Loading

        viewModelScope.launch {

            when (val result = clientRepository.myClasses()) {

                is NetworkResult.Success -> {

                    val coaches = result.data.mapNotNull { it.toCoachItem() }
                        .distinctBy { it.userId }

                    _state.value = ListState.Success(coaches)
                }

                is NetworkResult.Error   -> {
                    _state.value = ListState.Error(result.message)
                }

                else                     -> Unit
            }
        }
    }

    private fun MyClass.toCoachItem(): CoachItem? {

        val userId = coachUserId
                ?: return null

        val name = coachName?.takeIf { it.isNotBlank() }
                ?: return null

        return CoachItem(
            userId = userId,
            fullName = name,
            classTitle = title,
            avatarUrl = coachAvatarUrl
        )
    }
}
