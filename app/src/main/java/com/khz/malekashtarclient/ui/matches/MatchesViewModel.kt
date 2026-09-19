package com.khz.malekashtarclient.ui.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.MyMatch
import com.khz.malekashtarclient.ui.components.ListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchesViewModel(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ListState<MyMatch>>(ListState.Loading)
    val state: StateFlow<ListState<MyMatch>> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = ListState.Loading
        viewModelScope.launch {
            when (val r = clientRepository.myMatches()) {
                is NetworkResult.Success -> _state.value = ListState.Success(r.data)
                is NetworkResult.Error -> _state.value = ListState.Error(r.message)
                else -> {}
            }
        }
    }
}
