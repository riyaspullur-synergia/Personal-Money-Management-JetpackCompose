package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetCurrentUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.UpdatePasswordUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.UpdateProfileUseCase
import app.riyaspullur.personalmoneymanagement.core.security.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<ProfileEvent>()
    val event: SharedFlow<ProfileEvent> = _event.asSharedFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        getCurrentUserUseCase()
            .onEach { user ->
                if (user != null) {
                    _uiState.value = ProfileUiState.Success(user)
                } else {
                    _uiState.value = ProfileUiState.Error("User not found")
                }
            }
            .catch { e ->
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }

    fun updateProfile(displayName: String) {
        viewModelScope.launch {
            try {
                updateProfileUseCase(displayName)
            } catch (e: Exception) {
                _event.emit(ProfileEvent.Error(e.message ?: "Failed to update profile"))
            }
        }
    }

    fun verifyAndChangePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val user = getCurrentUserUseCase().first()
                if (user == null) {
                    _event.emit(ProfileEvent.Error("User not found"))
                    return@launch
                }

                if (PasswordHasher.verifyPassword(oldPassword, user.passwordHash)) {
                    val newHash = PasswordHasher.hashPassword(newPassword)
                    updatePasswordUseCase(newHash)
                    _event.emit(ProfileEvent.PasswordChanged)
                } else {
                    _event.emit(ProfileEvent.Error("Incorrect current password"))
                }
            } catch (e: Exception) {
                _event.emit(ProfileEvent.Error(e.message ?: "Failed to change password"))
            }
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

sealed interface ProfileEvent {
    data object PasswordChanged : ProfileEvent
    data class Error(val message: String) : ProfileEvent
}
