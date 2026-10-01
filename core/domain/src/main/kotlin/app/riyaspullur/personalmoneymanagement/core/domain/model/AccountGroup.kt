package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class AccountGroup(
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val sortOrder: Int = 0,
    val isArchived: Boolean = false
)
