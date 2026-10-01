package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategorySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.DailySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY transactionDate DESC")
    fun getAllTransactions(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllTransactionsBackup(userId: Long): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 1 ORDER BY transactionDate DESC")
    fun getDeletedTransactions(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND (:accountId IS NULL OR accountId = :accountId) AND transactionDate BETWEEN :startDate AND :endDate AND isDeleted = 0 ORDER BY transactionDate DESC")
    fun getTransactionsInRange(userId: Long, startDate: Long, endDate: Long, accountId: Long? = null): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1 WHERE id = :transactionId AND userId = :userId")
    suspend fun softDeleteTransaction(transactionId: Long, userId: Long)

    @Query("DELETE FROM transactions WHERE userId = :userId AND isDeleted = 1")
    suspend fun clearRecycleBin(userId: Long)

    // Aggregation queries
    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type AND (:accountId IS NULL OR accountId = :accountId) AND isDeleted = 0 AND transactionDate BETWEEN :startDate AND :endDate")
    fun getTotalAmountByType(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long? = null): Flow<Long?>

    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE userId = :userId AND type = :type AND (:accountId IS NULL OR accountId = :accountId) AND isDeleted = 0 AND transactionDate BETWEEN :startDate AND :endDate GROUP BY categoryId")
    fun getCategoryBreakdown(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long? = null): Flow<List<CategorySum>>

    @Query("SELECT transactionDate as date, SUM(amount) as total FROM transactions WHERE userId = :userId AND type = :type AND (:accountId IS NULL OR accountId = :accountId) AND isDeleted = 0 AND transactionDate BETWEEN :startDate AND :endDate GROUP BY transactionDate ORDER BY transactionDate ASC")
    fun getDailyTrendByType(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long? = null): Flow<List<DailySum>>

    @Transaction
    suspend fun executeTransfer(
        outTransaction: TransactionEntity,
        inTransaction: TransactionEntity
    ) {
        insertTransaction(outTransaction)
        insertTransaction(inTransaction)
    }
}
