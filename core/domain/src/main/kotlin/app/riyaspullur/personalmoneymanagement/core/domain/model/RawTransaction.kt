package app.riyaspullur.personalmoneymanagement.core.domain.model

data class RawTransaction(
    val date: String,
    val type: String,
    val amount: String,
    val currency: String,
    val accountName: String,
    val categoryName: String,
    val merchant: String,
    val description: String,
    val status: String
)
