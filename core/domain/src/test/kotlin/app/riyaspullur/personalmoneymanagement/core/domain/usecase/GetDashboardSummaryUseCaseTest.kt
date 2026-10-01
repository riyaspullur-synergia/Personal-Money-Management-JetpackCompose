package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetDashboardSummaryUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val accountRepository: AccountRepository = mockk()
    private val calculateBudgetStatusUseCase: CalculateBudgetStatusUseCase = mockk()
    private val useCase = GetDashboardSummaryUseCase(transactionRepository, accountRepository, calculateBudgetStatusUseCase)

    private fun account(type: AccountType, amount: Long) = Account(
        id = 1, userId = 1, groupId = null, name = type.name, type = type,
        currency = Currency.AED, color = 0, currentBalance = Money(amount, Currency.AED)
    )

    @Test
    fun `aggregates account balances into the correct buckets`() = runTest {
        val accounts = listOf(
            account(AccountType.CASH, 1000L),
            account(AccountType.BANK, 2000L),
            account(AccountType.INVESTMENT, 3000L),
            account(AccountType.FIXED_DEPOSIT, 4000L),
            account(AccountType.RECEIVABLE, 5000L)
        )
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.INCOME, any(), any()) } returns flowOf(0L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, any(), any()) } returns flowOf(0L)
        every { calculateBudgetStatusUseCase(1L) } returns flowOf(null)
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(accounts)

        useCase(1L).test {
            val summary = awaitItem()
            assertEquals(1000L, summary.cashBalance.amount)
            assertEquals(2000L, summary.bankBalance.amount)
            assertEquals(3000L, summary.investmentBalance.amount)
            assertEquals(4000L, summary.depositBalance.amount)
            assertEquals(5000L, summary.receivableBalance.amount)
            assertEquals(15000L, summary.netWorth.amount)
            assertEquals(3000L, summary.availableMoney.amount) // cash + bank only
            awaitComplete()
        }
    }

    @Test
    fun `computes net cash flow from income and expense totals`() = runTest {
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.INCOME, any(), any()) } returns flowOf(50000L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, any(), any()) } returns flowOf(30000L)
        every { calculateBudgetStatusUseCase(1L) } returns flowOf(null)
        every { accountRepository.getActiveAccountsWithBalances(1L) } returns flowOf(emptyList())

        useCase(1L).test {
            val summary = awaitItem()
            assertEquals(50000L, summary.totalIncome.amount)
            assertEquals(30000L, summary.totalExpense.amount)
            assertEquals(20000L, summary.netCashFlow.amount)
            assertNull(summary.budgetStatus)
            awaitComplete()
        }
    }
}
