package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class Category(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val icon: String?,
    val color: Int,
    val type: TransactionType,
    val parentCategoryId: Long? = null,
    val isArchived: Boolean = false
)
