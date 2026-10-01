package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveAccountsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.TransferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransferViewModel @Inject constructor(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val getActiveAccountsUseCase: GetActiveAccountsUseCase,
    private val transferUseCase: TransferUseCase
) : ViewModel() {

    val accounts: StateFlow<List<AccountRecord>> = getAuthenticatedUserIdUseCase()
        .filterNotNull()
        .flatMapLatest { userId ->
            getActiveAccountsUseCase(userId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _success = MutableSharedFlow<Unit>()
    val success: SharedFlow<Unit> = _success.asSharedFlow()

    fun transfer(
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        currency: Currency,
        note: String?
    ) {
        viewModelScope.launch {
            val userId = getAuthenticatedUserIdUseCase().first() ?: return@launch
            transferUseCase(
                userId = userId,
                fromAccountId = fromAccountId,
                toAccountId = toAccountId,
                amount = amount,
                currency = currency,
                date = System.currentTimeMillis(),
                note = note
            )
            _success.emit(Unit)
        }
    }
}
