package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.ClearRecycleBinUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetDeletedTransactionsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.RestoreTransactionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
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
class RecycleBinViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    
    private val getAuthenticatedUserIdUseCase = GetAuthenticatedUserIdUseCase(authRepository)
    private val getDeletedTransactionsUseCase = GetDeletedTransactionsUseCase(transactionRepository)
    private val restoreTransactionUseCase = RestoreTransactionUseCase(transactionRepository)
    private val clearRecycleBinUseCase = ClearRecycleBinUseCase(transactionRepository)
    
    private val testDispatcher = StandardTestDispatcher()

    private val deletedTx = Transaction(
        id = 1, userId = 1, accountId = 1, amount = 100L, currency = Currency.AED,
        categoryId = null, type = TransactionType.EXPENSE, merchant = null, description = "Coffee",
        notes = null, transactionDate = 0L, isDeleted = true
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

    private fun createViewModel(): RecycleBinViewModel {
        val vm = RecycleBinViewModel(
            getAuthenticatedUserIdUseCase,
            getDeletedTransactionsUseCase,
            restoreTransactionUseCase,
            clearRecycleBinUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `deletedTransactions streams the repository's deleted list`() = runTest {
        every { transactionRepository.getDeletedTransactions(1L) } returns flowOf(listOf(deletedTx))
        val viewModel = createViewModel()

        viewModel.deletedTransactions.test {
            assertEquals(emptyList<Transaction>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(deletedTx), awaitItem())
        }
    }

    @Test
    fun `restoreTransaction reinserts the transaction as not deleted`() = runTest {
        every { transactionRepository.getDeletedTransactions(1L) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        val captured = slot<Transaction>()
        coEvery { transactionRepository.insertTransaction(capture(captured)) } returns 1L

        viewModel.restoreTransaction(deletedTx)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(!captured.captured.isDeleted)
        assertEquals(deletedTx.id, captured.captured.id)
    }

    @Test
    fun `clearAll clears the recycle bin for the current user`() = runTest {
        every { transactionRepository.getDeletedTransactions(1L) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        coEvery { transactionRepository.clearRecycleBin(1L) } returns Unit

        viewModel.clearAll()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { transactionRepository.clearRecycleBin(1L) }
    }
}
