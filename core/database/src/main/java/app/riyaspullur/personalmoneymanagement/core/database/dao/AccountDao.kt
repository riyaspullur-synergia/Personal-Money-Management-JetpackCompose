package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE userId = :userId AND isArchived = 0")
    fun getActiveAccounts(userId: Long): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId AND userId = :userId")
    suspend fun getAccountById(accountId: Long, userId: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId")
    suspend fun getAllAccounts(userId: Long): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Delete
    suspend fun deleteAccount(account: AccountEntity)

    @Query("SELECT SUM(initialBalance) FROM accounts WHERE userId = :userId AND isArchived = 0")
    fun getTotalInitialBalance(userId: Long): Flow<Long?>

    @Query("""
        SELECT 
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE accountId = :accountId AND type = 'INCOME' AND isDeleted = 0) -
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE accountId = :accountId AND type = 'EXPENSE' AND isDeleted = 0) +
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE toAccountId = :accountId AND type IN ('TRANSFER', 'INVESTMENT', 'DEPOSIT') AND isDeleted = 0) -
            (SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE accountId = :accountId AND type IN ('TRANSFER', 'INVESTMENT', 'DEPOSIT') AND isDeleted = 0)
    """)
    fun getAccountTransactionBalance(accountId: Long): Flow<Long>
}
