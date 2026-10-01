package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategorySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetMonthlyReportUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val useCase = GetMonthlyReportUseCase(transactionRepository, categoryRepository)

    private val foodCategory = Category(id = 1, userId = 1, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE)
    private val transportCategory = Category(id = 2, userId = 1, name = "Transport", icon = null, color = 0, type = TransactionType.EXPENSE)

    @Test
    fun `builds a monthly report sorted by amount descending with correct percentages`() = runTest {
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.INCOME, 0L, 1000L, null) } returns flowOf(500000L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(150000L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.DEPOSIT, 0L, 1000L, null) } returns flowOf(20000L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.INVESTMENT, 0L, 1000L, null) } returns flowOf(30000L)
        every { transactionRepository.getCategoryBreakdown(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(
            listOf(CategorySum(categoryId = 1, total = 50000L), CategorySum(categoryId = 2, total = 100000L))
        )
        every { transactionRepository.getDailyTrendByType(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(emptyList())
        every { transactionRepository.getDailyTrendByType(1L, TransactionType.INCOME, 0L, 1000L, null) } returns flowOf(emptyList())
        every { categoryRepository.getAllActiveCategories(1L) } returns flowOf(listOf(foodCategory, transportCategory))
        every { transactionRepository.getTransactionsInRange(1L, 0L, 1000L, null) } returns flowOf(emptyList())

        useCase(1L, 0L, 1000L).test {
            val report = awaitItem()
            assertEquals(500000L, report.totalIncome.amount)
            assertEquals(150000L, report.totalExpense.amount)
            assertEquals(20000L, report.totalDeposit.amount)
            assertEquals(30000L, report.totalInvestment.amount)
            assertEquals(300000L, report.netBalance.amount) // 500k - 150k - 20k - 30k
            assertEquals(2, report.categoryBreakdown.size)
            assertEquals("Transport", report.categoryBreakdown[0].category?.name)
            assertEquals(100000L, report.categoryBreakdown[0].amount.amount)
            assertEquals(2f / 3f, report.categoryBreakdown[0].percentage, 0.001f)
            assertEquals("Food", report.categoryBreakdown[1].category?.name)
            awaitComplete()
        }
    }

    @Test
    fun `unmatched category id falls back to a null category rendered as Other`() = runTest {
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.INCOME, 0L, 1000L, null) } returns flowOf(0L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(10000L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.DEPOSIT, 0L, 1000L, null) } returns flowOf(0L)
        every { transactionRepository.getTotalAmountByType(1L, TransactionType.INVESTMENT, 0L, 1000L, null) } returns flowOf(0L)
        every { transactionRepository.getCategoryBreakdown(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(
            listOf(CategorySum(categoryId = 999L, total = 10000L))
        )
        every { transactionRepository.getDailyTrendByType(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(emptyList())
        every { transactionRepository.getDailyTrendByType(1L, TransactionType.INCOME, 0L, 1000L, null) } returns flowOf(emptyList())
        every { categoryRepository.getAllActiveCategories(1L) } returns flowOf(emptyList())
        every { transactionRepository.getTransactionsInRange(1L, 0L, 1000L, null) } returns flowOf(emptyList())

        useCase(1L, 0L, 1000L).test {
            val report = awaitItem()
            assertNull(report.categoryBreakdown.single().category)
            awaitComplete()
        }
    }
}
