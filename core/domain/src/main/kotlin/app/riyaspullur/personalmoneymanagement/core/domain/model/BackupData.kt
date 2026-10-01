package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class BackupData(
    val accounts: List<AccountRecord> = emptyList(),
    val categories: List<Category> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: List<BudgetRecord> = emptyList(),
    val budgetCategoryLimits: List<BudgetCategoryLimit> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val accountGroups: List<AccountGroup> = emptyList()
)
