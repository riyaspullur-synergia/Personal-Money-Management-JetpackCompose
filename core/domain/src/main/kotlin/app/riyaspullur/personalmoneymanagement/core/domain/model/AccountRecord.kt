package app.riyaspullur.personalmoneymanagement.core.domain.model

@kotlinx.serialization.Serializable
data class AccountRecord(
    val id: Long = 0,
    val userId: Long,
    val groupId: Long? = null,
    val name: String,
    val type: AccountType,
    val initialBalance: Long,
    val currency: Currency,
    val icon: String?,
    val color: Int,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val investedAmount: Long? = null,
    val lastValuationDate: Long? = null,
    val interestRate: Double? = null,
    val maturityDate: Long? = null,
    val bankName: String? = null,
    val personName: String? = null,
    val dueDate: Long? = null,
    val isReceivable: Boolean? = null
)
