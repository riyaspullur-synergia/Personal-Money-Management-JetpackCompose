package app.riyaspullur.personalmoneymanagement.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency

@Entity(
    tableName = "savings_goals",
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
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val targetAmount: Long, // Minor units
    val currentAmount: Long = 0,
    val currency: Currency,
    val targetDate: Long?,
    val icon: String?,
    val color: Int,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
