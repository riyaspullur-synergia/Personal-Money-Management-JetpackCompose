package app.riyaspullur.personalmoneymanagement.core.domain.model

data class Budget(
    val id: Long,
    val userId: Long,
    val name: String,
    val totalLimit: Money,
    val startDate: Long,
    val endDate: Long,
    val alertThreshold: Float,
    val isRolloverEnabled: Boolean
)
