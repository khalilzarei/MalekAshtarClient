package com.khz.malekashtarclient.ui.classes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekashtarclient.core.network.NetworkResult
import com.khz.malekashtarclient.data.repository.ChatRepository
import com.khz.malekashtarclient.data.repository.ClientRepository
import com.khz.malekashtarclient.domain.model.ChatContact
import com.khz.malekashtarclient.domain.model.MyClass
import com.khz.malekashtarclient.ui.components.ListState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel صفحه‌ی کلاس‌ها
 *
 * علاوه بر me/classes، لیست مخاطبین چت (me/chat-contacts) را هم می‌گیرد
 * تا در صورت کلیک «گفتگو با مربی» بتوان coach_id را به user_id مربی نگاشت.
 */
class ClassesViewModel(
    private val clientRepository: ClientRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    data class State(
        val list: ListState<MyClass> = ListState.Loading,
        val contacts: List<ChatContact> = emptyList()
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        _state.value = _state.value.copy(list = ListState.Loading)
        viewModelScope.launch {
            val classesDeferred = async { clientRepository.myClasses() }
            val contactsDeferred = async { clientRepository.myChatContacts() }

            val classesResult = classesDeferred.await()
            val contactsResult = contactsDeferred.await()

            val list = when (classesResult) {
                is NetworkResult.Success -> ListState.Success(classesResult.data)
                is NetworkResult.Error -> ListState.Error(classesResult.message)
                else -> ListState.Loading
            }
            val contacts = (contactsResult as? NetworkResult.Success)?.data ?: emptyList()

            _state.value = State(list = list, contacts = contacts)
        }
    }

    /** پیدا کردن user_id مربی از روی coach_id با تطابق اسم */
    fun findCoachUserId(coachName: String?): Int? {
        if (coachName.isNullOrBlank()) return null
        return _state.value.contacts
            .filter { it.role == "coach" }
            .firstOrNull { it.fullName == coachName }
            ?.userId
    }

    /** ساخت/دریافت اتاق دو نفره با مربی */
    suspend fun openChatWithCoach(coachUserId: Int) =
        chatRepository.getOrCreatePrivateRoom(coachUserId)
}
