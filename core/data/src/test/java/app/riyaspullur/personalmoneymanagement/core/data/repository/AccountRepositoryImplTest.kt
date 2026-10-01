package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRepositoryImplTest {

    private val dao: AccountDao = mockk()
    private val repository = AccountRepositoryImpl(dao)

    private fun entity(
        id: Long,
        type: AccountType = AccountType.CASH,
        initialBalance: Long = 1000L,
        investedAmount: Long? = null,
        interestRate: Double? = null,
        personName: String? = null,
        isReceivable: Boolean? = null
    ) = AccountEntity(
        id = id, userId = 1, name = "Account $id", type = type, initialBalance = initialBalance,
        currency = Currency.AED, icon = null, color = 0,
        investedAmount = investedAmount, interestRate = interestRate,
        personName = personName, isReceivable = isReceivable
    )

    @Test
    fun `getActiveAccounts delegates to dao`() = runTest {
        val entities = listOf(entity(1))
        every { dao.getActiveAccounts(1L) } returns flowOf(entities)

        repository.getActiveAccounts(1L).test {
            assertEquals(entities.map { it.toDomain() }, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getActiveAccountsWithBalances emits an empty list when there are no accounts`() = runTest {
        every { dao.getActiveAccounts(1L) } returns flowOf(emptyList())

        repository.getActiveAccountsWithBalances(1L).test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `plain cash account balance is initial balance plus transaction balance`() = runTest {
        val cash = entity(id = 1, type = AccountType.CASH, initialBalance = 1000L)
        every { dao.getActiveAccounts(1L) } returns flowOf(listOf(cash))
        every { dao.getAccountTransactionBalance(1L) } returns flowOf(500L)

        repository.getActiveAccountsWithBalances(1L).test {
            val accounts = awaitItem()
            assertEquals(1500L, accounts.single().currentBalance.amount)
            assertNull(accounts.single().investmentDetails)
            assertNull(accounts.single().depositDetails)
            assertNull(accounts.single().debtDetails)
            awaitComplete()
        }
    }

    @Test
    fun `investment account current balance uses the mark-to-market value`() = runTest {
        val investment = entity(
            id = 2,
            type = AccountType.INVESTMENT,
            initialBalance = 5000L,
            investedAmount = 4000L
        )
        every { dao.getActiveAccounts(1L) } returns flowOf(listOf(investment))
        every { dao.getAccountTransactionBalance(2L) } returns flowOf(1000L)

        repository.getActiveAccountsWithBalances(1L).test {
            val account = awaitItem().single()
            assertEquals(6000L, account.currentBalance.amount)
            assertEquals(4000L, account.investmentDetails?.investedAmount?.amount)
            assertEquals(6000L, account.investmentDetails?.currentValue?.amount)
            assertEquals(2000L, account.investmentDetails?.profitLoss?.amount)
            awaitComplete()
        }
    }

    @Test
    fun `deposit account exposes deposit details without changing the balance branch`() = runTest {
        val deposit = entity(
            id = 3,
            type = AccountType.FIXED_DEPOSIT,
            initialBalance = 10000L,
            interestRate = 5.5
        )
        every { dao.getActiveAccounts(1L) } returns flowOf(listOf(deposit))
        every { dao.getAccountTransactionBalance(3L) } returns flowOf(0L)

        repository.getActiveAccountsWithBalances(1L).test {
            val account = awaitItem().single()
            assertEquals(10000L, account.currentBalance.amount)
            assertEquals(10000L, account.depositDetails?.principalAmount?.amount)
            assertEquals(5.5, account.depositDetails?.interestRate)
            awaitComplete()
        }
    }

    @Test
    fun `receivable account current balance is the remaining debt amount`() = runTest {
        val receivable = entity(
            id = 4,
            type = AccountType.RECEIVABLE,
            initialBalance = 2000L,
            personName = "John",
            isReceivable = true
        )
        every { dao.getActiveAccounts(1L) } returns flowOf(listOf(receivable))
        every { dao.getAccountTransactionBalance(4L) } returns flowOf(-500L)

        repository.getActiveAccountsWithBalances(1L).test {
            val account = awaitItem().single()
            assertEquals(1500L, account.currentBalance.amount)
            assertEquals("John", account.debtDetails?.personName)
            assertEquals(2000L, account.debtDetails?.totalAmount?.amount)
            assertEquals(1500L, account.debtDetails?.remainingAmount?.amount)
            assertTrue(account.debtDetails?.isReceivable == true)
            awaitComplete()
        }
    }

    @Test
    fun `combines multiple accounts preserving their order`() = runTest {
        val first = entity(id = 1, initialBalance = 100L)
        val second = entity(id = 2, initialBalance = 200L)
        every { dao.getActiveAccounts(1L) } returns flowOf(listOf(first, second))
        every { dao.getAccountTransactionBalance(1L) } returns flowOf(0L)
        every { dao.getAccountTransactionBalance(2L) } returns flowOf(0L)

        repository.getActiveAccountsWithBalances(1L).test {
            val accounts = awaitItem()
            assertEquals(listOf(1L, 2L), accounts.map { it.id })
            assertEquals(listOf(100L, 200L), accounts.map { it.currentBalance.amount })
            awaitComplete()
        }
    }

    @Test
    fun `getAccountById delegates to dao`() = runTest {
        val cash = entity(1)
        coEvery { dao.getAccountById(1L, 1L) } returns cash

        assertEquals(cash.toDomain(), repository.getAccountById(1L, 1L))
    }

    @Test
    fun `insertAccount delegates to dao and returns the new id`() = runTest {
        val cash = entity(1)
        coEvery { dao.insertAccount(cash) } returns 9L

        assertEquals(9L, repository.insertAccount(cash.toDomain()))
    }

    @Test
    fun `updateAccount delegates to dao`() = runTest {
        val cash = entity(1)
        coEvery { dao.updateAccount(cash) } returns Unit

        repository.updateAccount(cash.toDomain())

        coVerify { dao.updateAccount(cash) }
    }
}
