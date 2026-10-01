package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.TransactionDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategorySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DailySum
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactions(userId: Long): Flow<List<Transaction>> =
        transactionDao.getAllTransactions(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAllTransactionsForBackup(userId: Long): List<Transaction> =
        transactionDao.getAllTransactionsBackup(userId).map { it.toDomain() }

    override fun getDeletedTransactions(userId: Long): Flow<List<Transaction>> =
        transactionDao.getDeletedTransactions(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getTransactionsInRange(userId: Long, startDate: Long, endDate: Long, accountId: Long?): Flow<List<Transaction>> =
        transactionDao.getTransactionsInRange(userId, startDate, endDate, accountId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun insertTransaction(transaction: Transaction): Long =
        transactionDao.insertTransaction(transaction.toEntity())

    override suspend fun insertTransactions(transactions: List<Transaction>) =
        transactionDao.insertTransactions(transactions.map { it.toEntity() })

    override suspend fun softDeleteTransaction(transactionId: Long, userId: Long) =
        transactionDao.softDeleteTransaction(transactionId, userId)

    override suspend fun clearRecycleBin(userId: Long) =
        transactionDao.clearRecycleBin(userId)

    override fun getTotalAmountByType(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long?): Flow<Long> =
        transactionDao.getTotalAmountByType(userId, type, startDate, endDate, accountId).map { it ?: 0L }

    override fun getCategoryBreakdown(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long?): Flow<List<CategorySum>> =
        transactionDao.getCategoryBreakdown(userId, type, startDate, endDate, accountId)

    override fun getDailyTrendByType(userId: Long, type: TransactionType, startDate: Long, endDate: Long, accountId: Long?): Flow<List<DailySum>> =
        transactionDao.getDailyTrendByType(userId, type, startDate, endDate, accountId)

    override suspend fun transfer(
        userId: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        currency: Currency,
        date: Long,
        note: String?
    ) {
        val transaction = TransactionEntity(
            userId = userId,
            accountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            currency = currency,
            categoryId = null,
            type = TransactionType.TRANSFER,
            merchant = "Transfer",
            description = note ?: "Transfer between accounts",
            notes = note,
            transactionDate = date
        )
        transactionDao.insertTransaction(transaction)
    }
}
