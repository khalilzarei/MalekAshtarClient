package com.khz.malekclient.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.data.repository.ClientRepository
import com.khz.malekclient.domain.model.NewsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NewsDetailState {
    data object Loading : NewsDetailState()
    data class Success(val news: NewsItem) : NewsDetailState()
    data class Error(val message: String) : NewsDetailState()
}

class NewsDetailViewModel(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _state = MutableStateFlow<NewsDetailState>(NewsDetailState.Loading)
    val state: StateFlow<NewsDetailState> = _state.asStateFlow()

    private var currentId: Int? = null

    fun load(newsId: Int) {
        if (currentId == newsId && _state.value is NewsDetailState.Success) return
        currentId = newsId
        _state.value = NewsDetailState.Loading
        viewModelScope.launch {
            when (val r = clientRepository.myNewsDetail(newsId)) {
                is NetworkResult.Success -> _state.value = NewsDetailState.Success(r.data)
                is NetworkResult.Error   -> _state.value = NewsDetailState.Error(r.message)
                else                     -> {}
            }
        }
    }

    fun refresh() {
        currentId?.let { load(it) }
    }
}
