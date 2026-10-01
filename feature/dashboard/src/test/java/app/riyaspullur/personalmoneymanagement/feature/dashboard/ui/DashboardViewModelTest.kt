package app.riyaspullur.personalmoneymanagement.feature.dashboard.ui

import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DashboardSummary
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetDashboardSummaryUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetSavingsGoalsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetTransactionsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.ImportTransactionsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase = mockk()
    private val getTransactionsUseCase: GetTransactionsUseCase = mockk()
    private val getDashboardSummaryUseCase: GetDashboardSummaryUseCase = mockk()
    private val getSavingsGoalsUseCase: GetSavingsGoalsUseCase = mockk()
    private val importTransactionsUseCase: ImportTransactionsUseCase = mockk()
    private val preferencesDataSource: AppPreferencesDataSource = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private val summary = DashboardSummary(
        totalBalance = Money.zero(), availableMoney = Money.zero(), netWorth = Money.zero(),
        cashBalance = Money.zero(), bankBalance = Money.zero(), investmentBalance = Money.zero(),
        depositBalance = Money.zero(), receivableBalance = Money.zero(),
        totalIncome = Money(1000L, Currency.AED), totalExpense = Money(500L, Currency.AED),
        netCashFlow = Money(500L, Currency.AED), budgetStatus = null
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getAuthenticatedUserIdUseCase() } returns flowOf(1L)
        every { preferencesDataSource.expenseLimitStartDay } returns flowOf("1")
        every { preferencesDataSource.maxExpenseLimit } returns flowOf(0L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DashboardViewModel {
        val vm = DashboardViewModel(
            getAuthenticatedUserIdUseCase,
            getTransactionsUseCase,
            getDashboardSummaryUseCase,
            getSavingsGoalsUseCase,
            importTransactionsUseCase,
            preferencesDataSource
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    private fun sixTransactions() = (1..6).map { i ->
        Transaction(id = i.toLong(), userId = 1, accountId = 1, amount = 100L, currency = Currency.AED, categoryId = null, type = TransactionType.EXPENSE, merchant = null, description = "tx$i", notes = null, transactionDate = i.toLong(), paymentStatus = "CLEARED", receiptPath = null)
    }

    @Test
    fun `uiState is Success combining summary, goals and recent transactions`() = runTest {
        val goals = listOf(SavingsGoal(id = 1, userId = 1, name = "Car", targetAmount = 1000L, currency = Currency.AED, targetDate = null, icon = null, color = 0))
        every { getDashboardSummaryUseCase(1L, any()) } returns flowOf(summary)
        every { getSavingsGoalsUseCase(1L) } returns flowOf(goals)
        every { getTransactionsUseCase(1L) } returns flowOf(sixTransactions())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertTrue(state is DashboardUiState.Success)
        state as DashboardUiState.Success
        assertEquals(summary, state.summary)
        assertEquals(goals, state.savingsGoals)
        assertEquals(5, state.recentTransactions.size) // capped to the 5 most recent
    }

    @Test
    fun `uiState is Error when a source flow throws`() = runTest {
        every { getDashboardSummaryUseCase(1L, any()) } returns flow { throw RuntimeException("boom") }
        every { getSavingsGoalsUseCase(1L) } returns flowOf(emptyList())
        every { getTransactionsUseCase(1L) } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertEquals(DashboardUiState.Error("boom"), viewModel.uiState.value)
    }
}
