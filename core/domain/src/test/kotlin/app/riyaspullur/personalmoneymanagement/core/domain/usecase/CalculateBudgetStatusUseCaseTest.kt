package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Budget
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BudgetRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateBudgetStatusUseCaseTest {

    private val budgetRepository: BudgetRepository = mockk()
    private val transactionRepository: TransactionRepository = mockk()
    private val useCase = CalculateBudgetStatusUseCase(budgetRepository, transactionRepository)

    @Test
    fun `emits null when there is no active budget`() = runTest {
        every { budgetRepository.getActiveBudget(any(), any()) } returns flowOf(null)

        useCase(userId = 1L).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `computes spent, remaining and progress from the active budget`() = runTest {
        val now = System.currentTimeMillis()
        val budget = Budget(
            id = 1, userId = 1, name = "Monthly",
            totalLimit = Money(100000L, Currency.AED),
            startDate = now - 1000L,
            endDate = now + 10L * 24 * 60 * 60 * 1000,
            alertThreshold = 0.8f,
            isRolloverEnabled = false
        )
        every { budgetRepository.getActiveBudget(any(), any()) } returns flowOf(budget)
        every {
            transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, budget.startDate, budget.endDate)
        } returns flowOf(40000L)

        useCase(userId = 1L).test {
            val status = awaitItem()
            assertEquals(40000L, status?.spent?.amount)
            assertEquals(60000L, status?.remaining?.amount)
            assertEquals(0.4f, status?.progress)
            awaitComplete()
        }
    }

    @Test
    fun `daysRemaining is at least 1 when the budget period has already ended`() = runTest {
        val now = System.currentTimeMillis()
        val budget = Budget(
            id = 1, userId = 1, name = "Expired",
            totalLimit = Money(100000L, Currency.AED),
            startDate = now - 100000L,
            endDate = now - 1000L,
            alertThreshold = 0.8f,
            isRolloverEnabled = false
        )
        every { budgetRepository.getActiveBudget(any(), any()) } returns flowOf(budget)
        every {
            transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, budget.startDate, budget.endDate)
        } returns flowOf(0L)

        useCase(userId = 1L).test {
            val status = awaitItem()
            assertEquals(1, status?.daysRemaining)
            awaitComplete()
        }
    }

    @Test
    fun `progress is zero when the budget limit is zero`() = runTest {
        val now = System.currentTimeMillis()
        val budget = Budget(
            id = 1, userId = 1, name = "Zero",
            totalLimit = Money(0L, Currency.AED),
            startDate = now,
            endDate = now + 100000L,
            alertThreshold = 0.8f,
            isRolloverEnabled = false
        )
        every { budgetRepository.getActiveBudget(any(), any()) } returns flowOf(budget)
        every {
            transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, budget.startDate, budget.endDate)
        } returns flowOf(0L)

        useCase(userId = 1L).test {
            val status = awaitItem()
            assertEquals(0f, status?.progress)
            awaitComplete()
        }
    }
}
