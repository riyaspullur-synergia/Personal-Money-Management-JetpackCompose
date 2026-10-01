package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.DeleteTransactionUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetTransactionsInRangeUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetTransactionsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class TransactionsViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    
    private val getAuthenticatedUserIdUseCase = GetAuthenticatedUserIdUseCase(authRepository)
    private val getTransactionsUseCase = GetTransactionsUseCase(transactionRepository)
    private val getTransactionsInRangeUseCase = GetTransactionsInRangeUseCase(transactionRepository)
    private val deleteTransactionUseCase: DeleteTransactionUseCase = mockk()
    
    private val testDispatcher = StandardTestDispatcher()

    private val tx = Transaction(
        id = 1, userId = 1, accountId = 1, amount = 100L, currency = Currency.AED,
        categoryId = null, type = TransactionType.EXPENSE, merchant = null, description = "Coffee",
        notes = null, transactionDate = 0L
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.getCurrentUserId() } returns flowOf(1L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransactionsViewModel {
        val vm = TransactionsViewModel(
            getAuthenticatedUserIdUseCase,
            getTransactionsUseCase,
            getTransactionsInRangeUseCase,
            deleteTransactionUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `uiState defaults to Success with the full unfiltered list`() = runTest {
        every { transactionRepository.getAllTransactions(1L) } returns flowOf(listOf(tx))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(TransactionsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is TransactionsUiState.Success)
            assertEquals(listOf(tx), (state as TransactionsUiState.Success).transactions)
        }
    }

    @Test
    fun `setDateRange switches to the ranged query`() = runTest {
        every { transactionRepository.getAllTransactions(1L) } returns flowOf(listOf(tx))
        every { transactionRepository.getTransactionsInRange(1L, 100L, 200L) } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(TransactionsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertTrue(awaitItem() is TransactionsUiState.Success)

            viewModel.setDateRange(100L, 200L)
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is TransactionsUiState.Success)
            assertEquals(emptyList<Transaction>(), (state as TransactionsUiState.Success).transactions)
        }
        coVerify { transactionRepository.getTransactionsInRange(1L, 100L, 200L) }
    }

    @Test
    fun `deleteTransaction soft-deletes via the usecase`() = runTest {
        every { transactionRepository.getAllTransactions(1L) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        coEvery { deleteTransactionUseCase(1L, 1L) } returns Unit

        viewModel.deleteTransaction(tx)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { deleteTransactionUseCase(1L, 1L) }
    }
}
