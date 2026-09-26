package com.khz.malekclient.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khz.malekclient.core.network.NetworkResult
import com.khz.malekclient.data.repository.AuthRepository
import com.khz.malekclient.data.repository.ClientRepository
import com.khz.malekclient.domain.model.Guardian
import com.khz.malekclient.domain.model.PlayerProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    data class State(
        val loading: Boolean = true,
        val profile: PlayerProfile? = null,
        val uploading: Boolean = false,
        val saving: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
        val loggedOut: Boolean = false,

        // edit modes
        val isEditingUser: Boolean = false,
        val isEditingPlayer: Boolean = false,
        val editingGuardianId: Int? = null,

        // form fields
        val userFullName: String = "",
        val userMobile: String = "",
        val playerFirstName: String = "",
        val playerLastName: String = "",
        val playerBirthDate: String = "", // YYYY-MM-DD
        val playerGender: String = "",
        val guardianFullName: String = "",
        val guardianMobile: String = "",
        val guardianEmergency: String = "",
        val guardianRelation: String = ""
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.value = _state.value.copy(
            loading = true,
            error = null,
            successMessage = null
        )
        viewModelScope.launch {
            when (val r = clientRepository.myProfile()) {
                is NetworkResult.Success -> {
                    val profile = r.data
                    _state.value = _state.value.copy(
                        loading = false,
                        profile = profile,
                        userFullName = profile.user.fullName,
                        userMobile = profile.user.mobile
                                ?: "",
                        playerFirstName = profile.player?.fullName?.split(" ")
                            ?.firstOrNull()
                                ?: "",
                        playerLastName = profile.player?.fullName?.substringAfter(
                            " ",
                            ""
                        )
                                ?: "",
                        playerBirthDate = profile.player?.birthDate
                                ?: "",
                        playerGender = profile.player?.gender
                                ?: "",
                        error = null
                    )
                }

                is NetworkResult.Error   -> _state.value = _state.value.copy(
                    loading = false,
                    error = r.message
                )

                else                     -> _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun startEditUser() {
        val p = _state.value.profile
                ?: return
        _state.value = _state.value.copy(
            isEditingUser = true,
            userFullName = p.user.fullName,
            userMobile = p.user.mobile
                    ?: ""
        )
    }

    fun cancelEditUser() {
        _state.value = _state.value.copy(
            isEditingUser = false,
            error = null
        )
    }

    fun startEditPlayer() {
        val player = _state.value.profile?.player
                ?: return
        val parts = player.fullName.split(
            " ",
            limit = 2
        )
        _state.value = _state.value.copy(
            isEditingPlayer = true,
            playerFirstName = parts.getOrNull(0)
                    ?: "",
            playerLastName = parts.getOrNull(1)
                    ?: "",
            playerBirthDate = player.birthDate
                    ?: "",
            playerGender = player.gender
                    ?: ""
        )
    }

    fun cancelEditPlayer() {
        _state.value = _state.value.copy(
            isEditingPlayer = false,
            error = null
        )
    }

    fun startEditGuardian(g: Guardian) {
        _state.value = _state.value.copy(
            editingGuardianId = g.id,
            guardianFullName = g.fullName,
            guardianMobile = g.mobile
                    ?: "",
            guardianEmergency = g.emergencyPhone
                    ?: "",
            guardianRelation = g.relation
                    ?: ""
        )
    }

    fun cancelEditGuardian() {
        _state.value = _state.value.copy(
            editingGuardianId = null,
            error = null
        )
    }

    // field updaters
    fun onUserFullNameChange(v: String) {
        _state.value = _state.value.copy(userFullName = v)
    }

    fun onUserMobileChange(v: String) {
        _state.value = _state.value.copy(userMobile = v)
    }

    fun onPlayerFirstNameChange(v: String) {
        _state.value = _state.value.copy(playerFirstName = v)
    }

    fun onPlayerLastNameChange(v: String) {
        _state.value = _state.value.copy(playerLastName = v)
    }

    fun onPlayerBirthDateChange(v: String) {
        _state.value = _state.value.copy(playerBirthDate = v)
    }

    fun onPlayerGenderChange(v: String) {
        _state.value = _state.value.copy(playerGender = v)
    }

    fun onGuardianFullNameChange(v: String) {
        _state.value = _state.value.copy(guardianFullName = v)
    }

    fun onGuardianMobileChange(v: String) {
        _state.value = _state.value.copy(guardianMobile = v)
    }

    fun onGuardianEmergencyChange(v: String) {
        _state.value = _state.value.copy(guardianEmergency = v)
    }

    fun onGuardianRelationChange(v: String) {
        _state.value = _state.value.copy(guardianRelation = v)
    }

    fun saveUser() {
        val s = _state.value
        if (s.userFullName.isBlank()) {
            _state.value = s.copy(error = "نام کامل الزامی است"); return
        }
        _state.value = s.copy(
            saving = true,
            error = null,
            successMessage = null
        )
        viewModelScope.launch {
            val body = mutableMapOf<String, Any?>(
                "full_name" to s.userFullName.trim(),
                "mobile" to s.userMobile.trim()
                    .ifBlank { null })
            when (val r = clientRepository.updateProfile(body)) {
                is NetworkResult.Success -> _state.value = _state.value.copy(
                    saving = false,
                    profile = r.data,
                    isEditingUser = false,
                    successMessage = "پروفایل به‌روزرسانی شد"
                )

                is NetworkResult.Error   -> _state.value = _state.value.copy(
                    saving = false,
                    error = r.message
                )

                else                     -> _state.value = _state.value.copy(saving = false)
            }
        }
    }

    fun savePlayer() {
        val s = _state.value
        val playerId = s.profile?.player?.id
                ?: return
        if (s.playerFirstName.isBlank() || s.playerLastName.isBlank()) {
            _state.value = s.copy(error = "نام و نام خانوادگی بازیکن الزامی است"); return
        }
        _state.value = s.copy(
            saving = true,
            error = null,
            successMessage = null
        )
        viewModelScope.launch {
            val body = mutableMapOf<String, Any?>(
                "first_name" to s.playerFirstName.trim(),
                "last_name" to s.playerLastName.trim(),
                "birth_date" to s.playerBirthDate.trim()
                    .ifBlank { null },
                "gender" to s.playerGender.trim()
                    .ifBlank { null })
            when (val r = clientRepository.updateChild(
                playerId,
                body
            )) {
                is NetworkResult.Success -> _state.value = _state.value.copy(
                    saving = false,
                    profile = r.data,
                    isEditingPlayer = false,
                    successMessage = "اطلاعات بازیکن به‌روزرسانی شد"
                )

                is NetworkResult.Error   -> _state.value = _state.value.copy(
                    saving = false,
                    error = r.message
                )

                else                     -> _state.value = _state.value.copy(saving = false)
            }
        }
    }

    fun saveGuardian() {
        val s = _state.value
        val gid = s.editingGuardianId
                ?: return
        if (s.guardianFullName.isBlank()) {
            _state.value = s.copy(error = "نام سرپرست الزامی است"); return
        }
        _state.value = s.copy(
            saving = true,
            error = null,
            successMessage = null
        )
        viewModelScope.launch {
            val body = mutableMapOf<String, Any?>(
                "full_name" to s.guardianFullName.trim(),
                "mobile" to s.guardianMobile.trim()
                    .ifBlank { null },
                "emergency_phone" to s.guardianEmergency.trim()
                    .ifBlank { null },
                "relation" to s.guardianRelation.trim()
                    .ifBlank { null })
            when (val r = clientRepository.updateGuardian(
                gid,
                body
            )) {
                is NetworkResult.Success -> _state.value = _state.value.copy(
                    saving = false,
                    profile = r.data,
                    editingGuardianId = null,
                    successMessage = "اطلاعات سرپرست به‌روزرسانی شد"
                )

                is NetworkResult.Error   -> _state.value = _state.value.copy(
                    saving = false,
                    error = r.message
                )

                else                     -> _state.value = _state.value.copy(saving = false)
            }
        }
    }

    fun uploadAvatar(file: File) {
        _state.value = _state.value.copy(
            uploading = true,
            error = null
        )
        viewModelScope.launch {
            when (val r = clientRepository.uploadAvatar(file)) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(uploading = false)
                    refresh()
                }

                is NetworkResult.Error   -> _state.value = _state.value.copy(
                    uploading = false,
                    error = r.message
                )

                else                     -> _state.value = _state.value.copy(uploading = false)
            }
        }
    }

    fun deleteAvatar() {
        _state.value = _state.value.copy(
            uploading = true,
            error = null
        )
        viewModelScope.launch {
            when (val r = clientRepository.deleteAvatar()) {
                is NetworkResult.Success -> {
                    _state.value = _state.value.copy(uploading = false)
                    refresh()
                }

                is NetworkResult.Error   -> _state.value = _state.value.copy(
                    uploading = false,
                    error = r.message
                )

                else                     -> _state.value = _state.value.copy(uploading = false)
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(
            error = null,
            successMessage = null
        )
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(loggedOut = true)
        }
    }
}
