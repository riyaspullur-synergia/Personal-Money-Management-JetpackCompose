package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class Transaction(
    val id: Long = 0,
    val userId: Long,
    val accountId: Long,
    val toAccountId: Long? = null, // Used for transfers
    val amount: Long,
    val currency: Currency,
    val categoryId: Long?,
    val type: TransactionType,
    val merchant: String?,
    val description: String?,
    val notes: String?,
    val transactionDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val paymentStatus: String = "CLEARED", // PENDING, CLEARED
    val receiptPath: String? = null,
    val isRecurring: Boolean = false,
    val isDeleted: Boolean = false
)
