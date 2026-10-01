package app.riyaspullur.personalmoneymanagement.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val totalLimit: Long, // Minor units
    val currency: Currency,
    val startDate: Long,
    val endDate: Long,
    val alertThreshold: Float = 0.8f, // 80%
    val isRolloverEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
