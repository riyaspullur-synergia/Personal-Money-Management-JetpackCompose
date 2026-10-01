package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.CategorySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DailySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(userId: Long): Flow<List<Transaction>>
    suspend fun getAllTransactionsForBackup(userId: Long): List<Transaction>
    fun getDeletedTransactions(userId: Long): Flow<List<Transaction>>
    fun getTransactionsInRange(userId: Long, startDate: Long, endDate: Long, accountId: Long? = null): Flow<List<Transaction>>
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun insertTransactions(transactions: List<Transaction>)
    suspend fun softDeleteTransaction(transactionId: Long, userId: Long)
    suspend fun clearRecycleBin(userId: Long)
    fun getTotalAmountByType(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long? = null): Flow<Long>
    fun getCategoryBreakdown(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long? = null): Flow<List<CategorySum>>
    fun getDailyTrendByType(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long? = null): Flow<List<DailySum>>
    suspend fun transfer(
        userId: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        currency: Currency,
        date: Long,
        note: String?
    )
}
