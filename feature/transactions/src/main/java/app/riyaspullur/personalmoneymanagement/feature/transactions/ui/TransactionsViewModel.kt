package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.DeleteTransactionUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetTransactionsInRangeUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionsInRangeUseCase: GetTransactionsInRangeUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _startDate = MutableStateFlow<Long?>(null)
    private val _endDate = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TransactionsUiState> = combine(
        getAuthenticatedUserIdUseCase().filterNotNull(),
        _startDate,
        _endDate
    ) { userId, start, end ->
        Triple(userId, start, end)
    }.flatMapLatest { (userId, start, end) ->
        if (start != null && end != null) {
            getTransactionsInRangeUseCase(userId, start, end)
        } else {
            getTransactionsUseCase(userId)
        }
    }.map<List<Transaction>, TransactionsUiState> { transactions ->
        TransactionsUiState.Success(transactions)
    }.catch { e ->
        emit(TransactionsUiState.Error(e.message ?: "Unknown error"))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionsUiState.Loading)

    fun setDateRange(start: Long?, end: Long?) {
        _startDate.value = start
        _endDate.value = end
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transaction.id, transaction.userId)
        }
    }
}

sealed interface TransactionsUiState {
    data object Loading : TransactionsUiState
    data class Success(val transactions: List<Transaction>) : TransactionsUiState
    data class Error(val message: String) : TransactionsUiState
}
