package app.riyaspullur.personalmoneymanagement.feature.dashboard.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import app.riyaspullur.personalmoneymanagement.core.domain.model.DashboardSummary
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetDashboardSummaryUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetSavingsGoalsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetTransactionsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.ImportTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase,
    private val getSavingsGoalsUseCase: GetSavingsGoalsUseCase,
    private val importTransactionsUseCase: ImportTransactionsUseCase,
    private val preferencesDataSource: AppPreferencesDataSource
) : ViewModel() {

    val maxExpenseLimit = preferencesDataSource.maxExpenseLimit
    val expenseLimitStartDay = preferencesDataSource.expenseLimitStartDay

    fun setExpenseLimit(limit: Long, startDay: String) {
        viewModelScope.launch {
            preferencesDataSource.setMaxExpenseLimit(limit)
            preferencesDataSource.setExpenseLimitStartDay(startDay)
        }
    }

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<DashboardEvent>()
    val event = _event.asSharedFlow()

    init {
        loadDashboard()
    }

    private fun loadDashboard() {
        getAuthenticatedUserIdUseCase()
            .filterNotNull()
            .flatMapLatest { userId ->
                combine(
                    expenseLimitStartDay,
                    getSavingsGoalsUseCase(userId),
                    getTransactionsUseCase(userId).map { it.take(5) }
                ) { startDay, goals, txs ->
                    val day = startDay.toIntOrNull() ?: 1
                    getDashboardSummaryUseCase(userId, day).map { summary ->
                        DashboardUiState.Success(summary, goals, txs)
                    }
                }.flatMapLatest { it }
            }
            .onEach { state ->
                _uiState.value = state
            }
            .catch { e ->
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }

    fun importTransactions(uri: Uri) {
        viewModelScope.launch {
            val userId = getAuthenticatedUserIdUseCase().filterNotNull().first()
            try {
                val count = importTransactionsUseCase(userId, uri)
                _event.emit(DashboardEvent.ImportResult(count))
            } catch (e: Exception) {
                _event.emit(DashboardEvent.Error(e.message ?: "Import failed"))
            }
        }
    }
}

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Success(
        val summary: DashboardSummary,
        val savingsGoals: List<SavingsGoal>,
        val recentTransactions: List<Transaction>
    ) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

sealed interface DashboardEvent {
    data class ImportResult(val count: Int) : DashboardEvent
    data class Error(val message: String) : DashboardEvent
}
