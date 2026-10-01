package app.riyaspullur.personalmoneymanagement.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency

@Entity(
    tableName = "accounts",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId"), Index("groupId")]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val groupId: Long? = null,
    val name: String,
    val type: AccountType,
    val initialBalance: Long, // In minor units
    val currency: Currency,
    val icon: String?,
    val color: Int,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),

    // Investment / Asset fields
    val investedAmount: Long? = null,
    val lastValuationDate: Long? = null,

    // Deposit fields
    val interestRate: Double? = null,
    val maturityDate: Long? = null,
    val bankName: String? = null,

    // Debt / Receivable fields
    val personName: String? = null,
    val dueDate: Long? = null,
    val isReceivable: Boolean? = null
)
