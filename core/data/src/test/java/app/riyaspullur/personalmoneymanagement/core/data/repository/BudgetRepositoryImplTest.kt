package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.BudgetDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetCategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetRepositoryImplTest {

    private val dao: BudgetDao = mockk()
    private val repository = BudgetRepositoryImpl(dao)

    private val budgetEntity = BudgetEntity(
        id = 1, userId = 1, name = "Monthly", totalLimit = 500000L, currency = Currency.AED,
        startDate = 0L, endDate = 1000L, alertThreshold = 0.8f, isRolloverEnabled = false
    )

    @Test
    fun `getAllBudgets maps entities to domain Budget with Money totalLimit`() = runTest {
        every { dao.getAllBudgets(1L) } returns flowOf(listOf(budgetEntity))

        repository.getAllBudgets(1L).test {
            val budgets = awaitItem()
            assertEquals(1, budgets.size)
            assertEquals("Monthly", budgets[0].name)
            assertEquals(500000L, budgets[0].totalLimit.amount)
            assertEquals(Currency.AED, budgets[0].totalLimit.currency)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveBudget maps a present entity to a domain Budget`() = runTest {
        every { dao.getActiveBudget(1L, any()) } returns flowOf(budgetEntity)

        repository.getActiveBudget(1L).test {
            assertEquals(budgetEntity.id, awaitItem()?.id)
            awaitComplete()
        }
    }

    @Test
    fun `getActiveBudget maps a missing entity to null`() = runTest {
        every { dao.getActiveBudget(1L, any()) } returns flowOf(null)

        repository.getActiveBudget(1L).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertBudget stamps generated budgetId onto each category before inserting`() = runTest {
        val categories = listOf(
            BudgetCategoryEntity(budgetId = 0, categoryId = 1, categoryLimit = 10000L),
            BudgetCategoryEntity(budgetId = 0, categoryId = 2, categoryLimit = 20000L)
        )
        val capturedCategories = slot<List<BudgetCategoryEntity>>()
        coEvery { dao.insertBudget(budgetEntity) } returns 42L
        coEvery { dao.insertBudgetCategories(capture(capturedCategories)) } returns Unit

        val id = repository.insertBudget(budgetEntity.toDomain(), categories.map { it.toDomain() })

        assertEquals(42L, id)
        assertEquals(listOf(42L, 42L), capturedCategories.captured.map { it.budgetId })
    }

    @Test
    fun `getBudgetCategories delegates to dao`() = runTest {
        val categories = listOf(BudgetCategoryEntity(id = 1, budgetId = 1, categoryId = 1, categoryLimit = 10000L))
        every { dao.getBudgetCategories(1L) } returns flowOf(categories)

        repository.getBudgetCategories(1L).test {
            assertEquals(categories.map { it.toDomain() }, awaitItem())
            awaitComplete()
        }
    }
}
