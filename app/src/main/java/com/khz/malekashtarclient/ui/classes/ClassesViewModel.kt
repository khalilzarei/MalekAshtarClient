package com.khz.malekashtarclient.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.ChatRepository
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.Evaluation
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.domain.model.MyScheduleItem
import com.khz.malekashtarclient.ui.components.ListState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ClassesUiState(
    val classesState: ListState<MyClass> = ListState.Loading,
    val schedule: List<MyScheduleItem> = emptyList(),
    val evaluations: List<Evaluation> = emptyList()
)

class ClassesViewModel(
    private val clientRepository: ClientRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ClassesUiState())
    val state: StateFlow<ClassesUiState> = _state.asStateFlow()

    // برای سازگاری با GenericListScreen قدیمی
    val legacyState: StateFlow<ListState<MyClass>>
        get() = MutableStateFlow(_state.value.classesState).asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(classesState = ListState.Loading)
        viewModelScope.launch {
            val classesDeferred = async { clientRepository.myClasses() }
            val scheduleDeferred = async { clientRepository.mySchedule() }
            val evalDeferred = async { clientRepository.myEvaluations() }

            val classesResult = classesDeferred.await()
            val scheduleResult = scheduleDeferred.await()
            val evalResult = evalDeferred.await()

            val classesState = when (classesResult) {
                is NetworkResult.Success -> ListState.Success(classesResult.data)
                is NetworkResult.Error   -> ListState.Error(classesResult.message)
                else                     -> ListState.Loading
            }

            val scheduleList = (scheduleResult as? NetworkResult.Success)?.data
                    ?: emptyList()
            val evalList = (evalResult as? NetworkResult.Success)?.data
                    ?: emptyList()

            _state.value = ClassesUiState(
                classesState = classesState,
                schedule = scheduleList,
                evaluations = evalList
            )
        }
    }

    suspend fun openChatWithCoach(coachUserId: Int) = chatRepository.getOrCreatePrivateRoom(coachUserId)
}
