package app.riyaspullur.personalmoneymanagement.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val createdAt: Long = System.currentTimeMillis()
)
