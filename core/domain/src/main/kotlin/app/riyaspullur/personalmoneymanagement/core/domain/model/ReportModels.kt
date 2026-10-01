package app.riyaspullur.personalmoneymanagement.core.domain.model

data class MonthlyReport(
    val totalIncome: Money,
    val totalExpense: Money,
    val totalDeposit: Money = Money.zero(),
    val totalInvestment: Money = Money.zero(),
    val netBalance: Money,
    val categoryBreakdown: List<CategoryReport>,
    val dailyExpenses: List<DailyTrendItem>,
    val dailyIncomes: List<DailyTrendItem> = emptyList(),
    val transactions: List<Transaction> = emptyList()
)

data class DailyTrendItem(
    val date: Long,
    val amount: Money
)

data class CategoryReport(
    val category: Category?,
    val amount: Money,
    val percentage: Float
)
