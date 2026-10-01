package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveAccountsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.TransferUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransferViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    
    private val getAuthenticatedUserIdUseCase = GetAuthenticatedUserIdUseCase(authRepository)
    private val getActiveAccountsUseCase = GetActiveAccountsUseCase(accountRepository)
    private val transferUseCase: TransferUseCase = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val account = AccountRecord(id = 1, userId = 1, name = "Cash", type = AccountType.CASH, initialBalance = 0, currency = Currency.AED, icon = null, color = 0)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TransferViewModel {
        val vm = TransferViewModel(getAuthenticatedUserIdUseCase, getActiveAccountsUseCase, transferUseCase)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `accounts streams the active accounts for the current user`() = runTest {
        every { authRepository.getCurrentUserId() } returns flowOf(1L)
        every { accountRepository.getActiveAccounts(1L) } returns flowOf(listOf(account))
        val viewModel = createViewModel()

        viewModel.accounts.test {
            assertEquals(emptyList<AccountRecord>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(account), awaitItem())
        }
    }

    @Test
    fun `transfer moves money between accounts and signals success`() = runTest {
        every { authRepository.getCurrentUserId() } returns flowOf(1L)
        every { accountRepository.getActiveAccounts(1L) } returns flowOf(emptyList())
        val viewModel = createViewModel()
        coEvery {
            transferUseCase(1L, 10L, 20L, 5000L, Currency.AED, any(), "Rent")
        } returns Unit

        viewModel.success.test {
            viewModel.transfer(10L, 20L, 5000L, Currency.AED, "Rent")
            awaitItem()
        }
        coVerify { transferUseCase(1L, 10L, 20L, 5000L, Currency.AED, any(), "Rent") }
    }

    @Test
    fun `transfer does nothing when there is no signed-in user`() = runTest {
        every { authRepository.getCurrentUserId() } returns flowOf(null)
        every { accountRepository.getActiveAccounts(any()) } returns flowOf(emptyList())
        val viewModel = createViewModel()

        viewModel.transfer(10L, 20L, 5000L, Currency.AED, "Rent")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { transferUseCase(any(), any(), any(), any(), any(), any(), any()) }
    }
}
