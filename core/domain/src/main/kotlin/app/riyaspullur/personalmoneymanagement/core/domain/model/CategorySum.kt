package app.riyaspullur.personalmoneymanagement.core.domain.model

data class CategorySum(
    val categoryId: Long?,
    val total: Long
)

data class DailySum(
    val date: Long,
    val total: Long
)
