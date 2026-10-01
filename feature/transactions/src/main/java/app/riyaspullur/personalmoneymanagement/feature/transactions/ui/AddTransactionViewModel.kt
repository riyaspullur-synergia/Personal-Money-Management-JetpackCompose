package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainError
import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainResult
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddCategoryUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddTransactionUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAccountByIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveAccountsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveCategoriesUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getActiveAccountsUseCase: GetActiveAccountsUseCase,
    private val getActiveCategoriesUseCase: GetActiveCategoriesUseCase,
    private val getAccountByIdUseCase: GetAccountByIdUseCase,
    private val addCategoryUseCase: AddCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<AddTransactionEvent>()
    val event: SharedFlow<AddTransactionEvent> = _event.asSharedFlow()

    val accounts: StateFlow<List<AccountRecord>> = getAuthenticatedUserIdUseCase()
        .filterNotNull()
        .flatMapLatest { userId ->
            getActiveAccountsUseCase(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<Category>> = getAuthenticatedUserIdUseCase()
        .filterNotNull()
        .flatMapLatest { userId ->
            getActiveCategoriesUseCase(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(
        amount: Long,
        type: TransactionType,
        categoryId: Long?,
        accountId: Long,
        toAccountId: Long? = null,
        description: String?,
        paymentStatus: String,
        receiptPath: String? = null,
        transactionDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AddTransactionUiState.Loading
                val userId = getAuthenticatedUserIdUseCase().first() ?: return@launch

                val account = getAccountByIdUseCase(accountId, userId)
                if (account == null) {
                    _uiState.value = AddTransactionUiState.Error("Invalid account selected")
                    return@launch
                }

                val transaction = Transaction(
                    userId = userId,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    currency = account.currency,
                    categoryId = categoryId,
                    type = type,
                    merchant = null,
                    description = description,
                    notes = null,
                    transactionDate = transactionDate,
                    paymentStatus = paymentStatus,
                    receiptPath = receiptPath
                )
                
                when (val result = addTransactionUseCase(transaction)) {
                    is DomainResult.Success -> {
                        _event.emit(AddTransactionEvent.Success)
                    }
                    is DomainResult.Error -> {
                        val errorMessage = when (val error = result.error) {
                            is DomainError.ValidationError -> error.message
                            is DomainError.DatabaseError -> error.message
                            else -> "Failed to save transaction"
                        }
                        _uiState.value = AddTransactionUiState.Error(errorMessage)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AddTransactionUiState.Error(e.message ?: "Failed to save transaction")
            }
        }
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            val userId = getAuthenticatedUserIdUseCase().first() ?: return@launch
            addCategoryUseCase(
                Category(
                    userId = userId,
                    name = name,
                    icon = null,
                    color = 0xFF9E9E9E.toInt(), // Default gray
                    type = type
                )
            )
        }
    }
}

sealed interface AddTransactionUiState {
    data object Idle : AddTransactionUiState
    data object Loading : AddTransactionUiState
    data object Success : AddTransactionUiState
    data class Error(val message: String) : AddTransactionUiState
}

sealed interface AddTransactionEvent {
    data object Success : AddTransactionEvent
}
