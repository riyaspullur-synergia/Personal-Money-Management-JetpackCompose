package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddAccountGroupUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddAccountUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAccountGroupsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAccountsWithBalancesUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val getAccountsWithBalancesUseCase: GetAccountsWithBalancesUseCase,
    private val getAccountGroupsUseCase: GetAccountGroupsUseCase,
    private val addAccountUseCase: AddAccountUseCase,
    private val addAccountGroupUseCase: AddAccountGroupUseCase
) : ViewModel() {

    val uiState: StateFlow<AccountsUiState> = getAuthenticatedUserIdUseCase()
        .filterNotNull()
        .flatMapLatest { userId ->
            combine(
                getAccountsWithBalancesUseCase(userId),
                getAccountGroupsUseCase(userId)
            ) { accounts, groups ->
                AccountsUiState.Success(accounts, groups)
            }
        }
        .map { it as AccountsUiState }
        .catch { e -> emit(AccountsUiState.Error(e.message ?: "Unknown error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccountsUiState.Loading)

    fun addAccount(account: AccountRecord) {
        viewModelScope.launch {
            addAccountUseCase(account)
        }
    }

    fun addGroup(name: String) {
        viewModelScope.launch {
            val userId = getAuthenticatedUserIdUseCase().first() ?: return@launch
            val group = AccountGroup(
                userId = userId,
                name = name
            )
            addAccountGroupUseCase(group)
        }
    }
}

sealed interface AccountsUiState {
    data object Loading : AccountsUiState
    data class Success(
        val accounts: List<Account>,
        val groups: List<AccountGroup>
    ) : AccountsUiState
    data class Error(val message: String) : AccountsUiState
}
