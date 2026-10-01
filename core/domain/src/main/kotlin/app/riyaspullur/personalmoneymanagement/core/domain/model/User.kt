package app.riyaspullur.personalmoneymanagement.core.domain.model

data class User(
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val createdAt: Long = System.currentTimeMillis()
)
