package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountGroupDao {
    @Query("SELECT * FROM account_groups WHERE userId = :userId AND isArchived = 0 ORDER BY sortOrder ASC")
    fun getActiveGroups(userId: Long): Flow<List<AccountGroupEntity>>

    @Query("SELECT * FROM account_groups WHERE userId = :userId")
    suspend fun getAllGroups(userId: Long): List<AccountGroupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: AccountGroupEntity): Long

    @Update
    suspend fun updateGroup(group: AccountGroupEntity)

    @Delete
    suspend fun deleteGroup(group: AccountGroupEntity)
}
