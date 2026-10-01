package app.riyaspullur.personalmoneymanagement.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAllUsersUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetCurrentUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetUserByUsernameUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.HasUsersUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.LoginUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.LogoutUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.RegisterUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SeedDefaultDataUseCase
import app.riyaspullur.personalmoneymanagement.core.security.PasswordHasher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val hasUsersUseCase: HasUsersUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByUsernameUseCase: GetUserByUsernameUseCase,
    private val getAllUsersUseCase: GetAllUsersUseCase,
    private val seedDefaultDataUseCase: SeedDefaultDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val allUsers: StateFlow<List<User>> = getAllUsersUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val hasUsers = hasUsersUseCase()
            if (!hasUsers) {
                _uiState.value = AuthUiState.RegistrationRequired
            } else {
                val currentUser = getCurrentUserUseCase().first()
                if (currentUser != null) {
                    _uiState.value = AuthUiState.Authenticated(currentUser.id)
                } else {
                    _uiState.value = AuthUiState.LoginRequired
                }
            }
        }
    }

    fun register(username: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val hash = PasswordHasher.hashPassword(password)
                val userId = registerUserUseCase(username, hash, displayName)
                seedDefaultDataUseCase(userId)
                loginUseCase(userId)
                _uiState.value = AuthUiState.Authenticated(userId)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val user = getUserByUsernameUseCase(username)
            if (user != null && PasswordHasher.verifyPassword(password, user.passwordHash)) {
                loginUseCase(user.id)
                _uiState.value = AuthUiState.Authenticated(user.id)
            } else {
                _uiState.value = AuthUiState.Error("Invalid username or password")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = AuthUiState.LoginRequired
        }
    }
}

sealed interface AuthUiState {
    data object Initial : AuthUiState
    data object Loading : AuthUiState
    data object RegistrationRequired : AuthUiState
    data object LoginRequired : AuthUiState
    data class Authenticated(val userId: Long) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
