package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.ClearRecycleBinUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetDeletedTransactionsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.RestoreTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val getDeletedTransactionsUseCase: GetDeletedTransactionsUseCase,
    private val restoreTransactionUseCase: RestoreTransactionUseCase,
    private val clearRecycleBinUseCase: ClearRecycleBinUseCase
) : ViewModel() {

    val deletedTransactions: StateFlow<List<Transaction>> = getAuthenticatedUserIdUseCase()
        .filterNotNull()
        .flatMapLatest { userId ->
            getDeletedTransactionsUseCase(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreTransaction(tx: Transaction) {
        viewModelScope.launch {
            restoreTransactionUseCase(tx)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            val userId = getAuthenticatedUserIdUseCase().first() ?: return@launch
            clearRecycleBinUseCase(userId)
        }
    }
}
