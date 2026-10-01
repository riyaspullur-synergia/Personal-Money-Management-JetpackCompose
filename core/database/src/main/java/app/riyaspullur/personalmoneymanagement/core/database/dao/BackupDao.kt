package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface BackupDao {
    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteTransactions(userId: Long)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteBudgets(userId: Long)

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteAccounts(userId: Long)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteCategories(userId: Long)

    @Query("DELETE FROM savings_goals WHERE userId = :userId")
    suspend fun deleteSavingsGoals(userId: Long)

    @Query("DELETE FROM account_groups WHERE userId = :userId")
    suspend fun deleteAccountGroups(userId: Long)
}
