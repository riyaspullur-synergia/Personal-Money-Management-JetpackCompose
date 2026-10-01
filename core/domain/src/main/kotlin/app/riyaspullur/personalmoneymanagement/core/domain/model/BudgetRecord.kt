package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class BudgetRecord(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val totalLimit: Long,
    val currency: Currency,
    val startDate: Long,
    val endDate: Long,
    val alertThreshold: Float = 0.8f,
    val isRolloverEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@kotlinx.serialization.Serializable
data class BudgetCategoryLimit(
    val id: Long = 0,
    val budgetId: Long,
    val categoryId: Long,
    val categoryLimit: Long
)
