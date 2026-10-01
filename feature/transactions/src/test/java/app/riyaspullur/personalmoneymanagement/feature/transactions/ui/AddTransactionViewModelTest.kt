package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainError
import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainResult
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddCategoryUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddTransactionUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAccountByIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveAccountsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveCategoriesUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddTransactionViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    
    private val getAuthenticatedUserIdUseCase = GetAuthenticatedUserIdUseCase(authRepository)
    private val addTransactionUseCase: AddTransactionUseCase = mockk()
    private val getActiveAccountsUseCase = GetActiveAccountsUseCase(accountRepository)
    private val getActiveCategoriesUseCase = GetActiveCategoriesUseCase(categoryRepository)
    private val getAccountByIdUseCase = GetAccountByIdUseCase(accountRepository)
    private val addCategoryUseCase = AddCategoryUseCase(categoryRepository)
    
    private val testDispatcher = StandardTestDispatcher()

    private val account = AccountRecord(id = 1, userId = 1, name = "Cash", type = AccountType.CASH, initialBalance = 0, currency = Currency.AED, icon = null, color = 0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.getCurrentUserId() } returns flowOf(1L)
        every { accountRepository.getActiveAccounts(1L) } returns flowOf(listOf(account))
        every { categoryRepository.getAllActiveCategories(1L) } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AddTransactionViewModel {
        val vm = AddTransactionViewModel(
            getAuthenticatedUserIdUseCase,
            addTransactionUseCase,
            getActiveAccountsUseCase,
            getActiveCategoriesUseCase,
            getAccountByIdUseCase,
            addCategoryUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `addTransaction surfaces error when use case returns validation error`() = runTest {
        val viewModel = createViewModel()
        coEvery { accountRepository.getAccountById(1L, 1L) } returns account
        coEvery { addTransactionUseCase(any()) } returns DomainResult.Error(DomainError.ValidationError("Invalid amount"))

        viewModel.addTransaction(0L, TransactionType.EXPENSE, null, 1L, null, "Coffee", "CLEARED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddTransactionUiState.Error("Invalid amount"), viewModel.uiState.value)
    }

    @Test
    fun `addTransaction persists the transaction and emits Success for a valid account`() = runTest {
        val viewModel = createViewModel()
        coEvery { accountRepository.getAccountById(1L, 1L) } returns account
        val captured = slot<Transaction>()
        coEvery { addTransactionUseCase(capture(captured)) } returns DomainResult.Success(1L)

        viewModel.event.test {
            viewModel.addTransaction(1500L, TransactionType.EXPENSE, 2L, 1L, null, "Coffee", "CLEARED")
            assertEquals(AddTransactionEvent.Success, awaitItem())
        }
        assertEquals(1500L, captured.captured.amount)
        assertEquals(Currency.AED, captured.captured.currency)
        assertEquals(1L, captured.captured.userId)
        assertEquals(2L, captured.captured.categoryId)
    }

    @Test
    fun `addTransaction with an unknown account surfaces an error state`() = runTest {
        val viewModel = createViewModel()
        coEvery { accountRepository.getAccountById(99L, 1L) } returns null

        viewModel.addTransaction(1000L, TransactionType.EXPENSE, null, 99L, null, "Coffee", "CLEARED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddTransactionUiState.Error("Invalid account selected"), viewModel.uiState.value)
        coVerify(exactly = 0) { addTransactionUseCase(any()) }
    }

    @Test
    fun `addTransaction surfaces an error state when the use case throws`() = runTest {
        val viewModel = createViewModel()
        coEvery { accountRepository.getAccountById(1L, 1L) } returns account
        coEvery { addTransactionUseCase(any()) } throws RuntimeException("disk full")

        viewModel.addTransaction(1000L, TransactionType.EXPENSE, null, 1L, null, "Coffee", "CLEARED")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AddTransactionUiState.Error("disk full"), viewModel.uiState.value)
    }

    @Test
    fun `addCategory inserts a category with a default gray color`() = runTest {
        val viewModel = createViewModel()
        val captured = slot<Category>()
        coEvery { categoryRepository.insertCategory(capture(captured)) } returns 1L

        viewModel.addCategory("Gifts", TransactionType.EXPENSE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Gifts", captured.captured.name)
        assertEquals(TransactionType.EXPENSE, captured.captured.type)
        assertEquals(1L, captured.captured.userId)
    }

    @Test
    fun `accounts and categories stream from their repositories`() = runTest {
        val category = Category(id = 1, userId = 1, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE)
        every { categoryRepository.getAllActiveCategories(1L) } returns flowOf(listOf(category))
        val viewModel = createViewModel()

        viewModel.accounts.test {
            assertEquals(emptyList<AccountRecord>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(account), awaitItem())
        }
        viewModel.categories.test {
            assertEquals(emptyList<Category>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(category), awaitItem())
        }
    }
}
