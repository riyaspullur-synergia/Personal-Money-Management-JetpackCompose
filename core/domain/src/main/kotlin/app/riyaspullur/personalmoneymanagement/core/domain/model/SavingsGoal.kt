package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class SavingsGoal(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long = 0,
    val currency: Currency,
    val targetDate: Long?,
    val icon: String?,
    val color: Int,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
