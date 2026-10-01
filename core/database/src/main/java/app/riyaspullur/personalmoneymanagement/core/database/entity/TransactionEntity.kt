package app.riyaspullur.personalmoneymanagement.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = AccountEntity::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = CategoryEntity::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL)
    ],
    indices = [
        Index("userId"),
        Index("accountId"),
        Index("categoryId"),
        Index("transactionDate")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
