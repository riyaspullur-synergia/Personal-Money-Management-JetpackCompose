package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategoryReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategorySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.DailySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.DailyTrendItem
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetMonthlyReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    @Suppress("UNCHECKED_CAST")
    operator fun invoke(
        userId: Long,
        startDate: Long,
        endDate: Long,
        accountId: Long? = null
    ): Flow<MonthlyReport> {
        return combine(
            transactionRepository.getTotalAmountByType(userId, TransactionType.INCOME, startDate, endDate, accountId),
            transactionRepository.getTotalAmountByType(userId, TransactionType.EXPENSE, startDate, endDate, accountId),
            transactionRepository.getCategoryBreakdown(userId, TransactionType.EXPENSE, startDate, endDate, accountId),
            transactionRepository.getDailyTrendByType(userId, TransactionType.EXPENSE, startDate, endDate, accountId),
            transactionRepository.getDailyTrendByType(userId, TransactionType.INCOME, startDate, endDate, accountId),
            categoryRepository.getAllActiveCategories(userId),
            transactionRepository.getTransactionsInRange(userId, startDate, endDate, accountId),
            transactionRepository.getTotalAmountByType(userId, TransactionType.DEPOSIT, startDate, endDate, accountId),
            transactionRepository.getTotalAmountByType(userId, TransactionType.INVESTMENT, startDate, endDate, accountId)
        ) { flows ->
            val income = flows[0] as Long
            val expense = flows[1] as Long
            val breakdown = flows[2] as List<CategorySum>
            val expenseTrend = flows[3] as List<DailySum>
            val incomeTrend = flows[4] as List<DailySum>
            val categories = flows[5] as List<Category>
            val txs = flows[6] as List<Transaction>
            val deposit = flows[7] as Long
            val investment = flows[8] as Long

            val totalExpense = expense
            val categoryReports = breakdown.map { sum ->
                val category = categories.find { it.id == sum.categoryId }
                CategoryReport(
                    category = category,
                    amount = Money(sum.total),
                    percentage = if (totalExpense > 0) sum.total.toFloat() / totalExpense else 0f
                )
            }.sortedByDescending { it.amount.amount }

            MonthlyReport(
                totalIncome = Money(income),
                totalExpense = Money(totalExpense),
                totalDeposit = Money(deposit),
                totalInvestment = Money(investment),
                netBalance = Money(income - totalExpense - deposit - investment), // Net balance considering outflows
                categoryBreakdown = categoryReports,
                dailyExpenses = expenseTrend.map { DailyTrendItem(it.date, Money(it.total)) },
                dailyIncomes = incomeTrend.map { DailyTrendItem(it.date, Money(it.total)) },
                transactions = txs
            )
        }
    }
}
