package app.riyaspullur.personalmoneymanagement.core.domain.model

data class DashboardSummary(
    val totalBalance: Money,
    val availableMoney: Money,
    val netWorth: Money,
    val cashBalance: Money,
    val bankBalance: Money,
    val investmentBalance: Money,
    val depositBalance: Money,
    val receivableBalance: Money,
    val totalIncome: Money,
    val totalExpense: Money,
    val netCashFlow: Money,
    val budgetStatus: BudgetStatus?
)
