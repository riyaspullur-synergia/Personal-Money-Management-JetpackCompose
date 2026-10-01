package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountGroup
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountGroupRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddAccountGroupUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AddAccountUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAccountGroupsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAccountsWithBalancesUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountsViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val accountGroupRepository: AccountGroupRepository = mockk()
    
    private val getAuthenticatedUserIdUseCase = GetAuthenticatedUserIdUseCase(authRepository)
    private val getAccountsWithBalancesUseCase = GetAccountsWithBalancesUseCase(accountRepository)
    private val getAccountGroupsUseCase = GetAccountGroupsUseCase(accountGroupRepository)
    private val addAccountUseCase: AddAccountUseCase = mockk()
    private val addAccountGroupUseCase: AddAccountGroupUseCase = mockk()
    
    private val testDispatcher = StandardTestDispatcher()

    private val account = Account(
        id = 1, userId = 1, groupId = null, name = "Cash", type = AccountType.CASH,
        currency = Currency.AED, color = 0, currentBalance = Money(1000L, Currency.AED)
    )
    private val group = AccountGroup(id = 1, userId = 1, name = "Banks")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { authRepository.getCurrentUserId() } returns flowOf(1L)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): AccountsViewModel {
        val vm = AccountsViewModel(
            getAuthenticatedUserIdUseCase,
            getAccountsWithBalancesUseCase,
            getAccountGroupsUseCase,
            addAccountUseCase,
            addAccountGroupUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `uiState is Success with accounts and groups`() = runTest {
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(listOf(account))
        every { accountGroupRepository.getActiveGroups(1L) } returns flowOf(listOf(group))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AccountsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AccountsUiState.Success)
            assertEquals(listOf(account), (state as AccountsUiState.Success).accounts)
            assertEquals(listOf(group), state.groups)
        }
    }

    @Test
    fun `uiState is Error when the repository flow throws`() = runTest {
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(emptyList())
        every { accountGroupRepository.getActiveGroups(1L) } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException(
                "db error"
            )
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AccountsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is AccountsUiState.Error)
            assertEquals("db error", (state as AccountsUiState.Error).message)
        }
    }

    @Test
    fun `addAccount inserts the account via the usecase`() = runTest {
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(emptyList())
        every { accountGroupRepository.getActiveGroups(1L) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        val entity = AccountRecord(
            userId = 1,
            name = "Cash",
            type = AccountType.CASH,
            initialBalance = 0,
            currency = Currency.AED,
            icon = null,
            color = 0
        )
        coEvery { addAccountUseCase(entity) } returns Unit

        viewModel.addAccount(entity)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { addAccountUseCase(entity) }
    }

    @Test
    fun `addGroup inserts a group for the current user via the usecase`() = runTest {
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(emptyList())
        every { accountGroupRepository.getActiveGroups(1L) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        val captured = slot<AccountGroup>()
        coEvery { addAccountGroupUseCase(capture(captured)) } returns Unit

        viewModel.addGroup("Investments")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1L, captured.captured.userId)
        assertEquals("Investments", captured.captured.name)
    }

    @Test
    fun `accounts and categories stream from their repositories`() = runTest {
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(listOf(account))
        every { accountGroupRepository.getActiveGroups(1L) } returns flowOf(listOf(group))
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(AccountsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem() as AccountsUiState.Success
            assertEquals(listOf(account), state.accounts)
            assertEquals(listOf(group), state.groups)
        }
    }
}
