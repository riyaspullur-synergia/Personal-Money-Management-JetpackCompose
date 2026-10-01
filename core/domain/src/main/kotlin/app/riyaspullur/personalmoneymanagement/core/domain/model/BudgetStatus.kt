package app.riyaspullur.personalmoneymanagement.core.domain.model

data class BudgetStatus(
    val budget: Budget,
    val spent: Money,
    val remaining: Money,
    val progress: Float,
    val daysRemaining: Int,
    val dailyAllowance: Money
)
