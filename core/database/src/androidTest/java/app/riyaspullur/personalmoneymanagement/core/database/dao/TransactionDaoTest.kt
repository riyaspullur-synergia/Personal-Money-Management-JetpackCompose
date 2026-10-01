package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented: exercises Room against a real SQLite engine, which the plain JVM unit-test
 * classpath cannot provide. Run on a connected device/emulator via `connectedAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class TransactionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TransactionDao
    private var userId: Long = 0
    private var accountId: Long = 0
    private var otherAccountId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transactionDao()
        userId = db.userDao().insertUser(UserEntity(username = "riyas", passwordHash = "hash", displayName = "Riyas"))
        accountId = db.accountDao().insertAccount(
            AccountEntity(
                userId = userId, name = "Cash", type = AccountType.CASH, initialBalance = 0L,
                currency = Currency.AED, icon = null, color = 0
            )
        )
        otherAccountId = db.accountDao().insertAccount(
            AccountEntity(
                userId = userId, name = "Bank", type = AccountType.BANK, initialBalance = 0L,
                currency = Currency.AED, icon = null, color = 0
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun tx(
        type: TransactionType = TransactionType.EXPENSE,
        amount: Long = 1000L,
        categoryId: Long? = null,
        transactionDate: Long = 100L,
        accId: Long = accountId,
        toAccountId: Long? = null,
        isDeleted: Boolean = false
    ) = TransactionEntity(
        userId = userId, accountId = accId, toAccountId = toAccountId, amount = amount,
        currency = Currency.AED, categoryId = categoryId, type = type, merchant = null,
        description = null, notes = null, transactionDate = transactionDate, isDeleted = isDeleted
    )

    @Test
    fun getAllTransactionsExcludesDeletedAndOrdersByDateDescending() = runBlocking {
        dao.insertTransaction(tx(transactionDate = 100L))
        dao.insertTransaction(tx(transactionDate = 300L))
        dao.insertTransaction(tx(transactionDate = 200L, isDeleted = true))

        val transactions = dao.getAllTransactions(userId).first()

        assertEquals(listOf(300L, 100L), transactions.map { it.transactionDate })
    }

    @Test
    fun getDeletedTransactionsReturnsOnlySoftDeletedRows() = runBlocking {
        dao.insertTransaction(tx(transactionDate = 100L))
        dao.insertTransaction(tx(transactionDate = 200L, isDeleted = true))

        val deleted = dao.getDeletedTransactions(userId).first()

        assertEquals(1, deleted.size)
        assertEquals(200L, deleted.single().transactionDate)
    }

    @Test
    fun getTransactionsInRangeFiltersByDateAndAccount() = runBlocking {
        dao.insertTransaction(tx(transactionDate = 50L)) // before range
        val inRangeId = dao.insertTransaction(tx(transactionDate = 150L))
        dao.insertTransaction(tx(transactionDate = 150L, accId = otherAccountId))
        dao.insertTransaction(tx(transactionDate = 500L)) // after range

        val allInRange = dao.getTransactionsInRange(userId, 100L, 300L).first()
        assertEquals(2, allInRange.size)

        val filteredByAccount = dao.getTransactionsInRange(userId, 100L, 300L, accountId).first()
        assertEquals(listOf(inRangeId), filteredByAccount.map { it.id })
    }

    @Test
    fun updateTransactionPersistsChanges() = runBlocking {
        val id = dao.insertTransaction(tx(amount = 1000L))
        val stored = dao.getAllTransactions(userId).first().single()

        dao.updateTransaction(stored.copy(amount = 2000L))

        assertEquals(2000L, dao.getAllTransactions(userId).first().single { it.id == id }.amount)
    }

    @Test
    fun softDeleteTransactionMovesItToRecycleBinForTheOwningUserOnly() = runBlocking {
        val id = dao.insertTransaction(tx())
        val otherUserId = db.userDao().insertUser(UserEntity(username = "other", passwordHash = "hash", displayName = "Other"))

        dao.softDeleteTransaction(id, otherUserId)
        assertEquals(1, dao.getAllTransactions(userId).first().size)

        dao.softDeleteTransaction(id, userId)

        assertTrue(dao.getAllTransactions(userId).first().isEmpty())
        assertEquals(1, dao.getDeletedTransactions(userId).first().size)
    }

    @Test
    fun clearRecycleBinRemovesOnlyDeletedTransactions() = runBlocking {
        val activeId = dao.insertTransaction(tx(transactionDate = 100L))
        val deletedId = dao.insertTransaction(tx(transactionDate = 200L, isDeleted = true))

        dao.clearRecycleBin(userId)

        assertEquals(listOf(activeId), dao.getAllTransactions(userId).first().map { it.id })
        assertTrue(dao.getDeletedTransactions(userId).first().none { it.id == deletedId })
    }

    @Test
    fun getTotalAmountByTypeSumsOnlyMatchingTypeWithinRange() = runBlocking {
        dao.insertTransaction(tx(type = TransactionType.EXPENSE, amount = 1000L, transactionDate = 100L))
        dao.insertTransaction(tx(type = TransactionType.EXPENSE, amount = 500L, transactionDate = 150L))
        dao.insertTransaction(tx(type = TransactionType.INCOME, amount = 5000L, transactionDate = 100L))
        dao.insertTransaction(tx(type = TransactionType.EXPENSE, amount = 999L, transactionDate = 500L)) // out of range

        val total = dao.getTotalAmountByType(userId, TransactionType.EXPENSE, 0L, 200L).first()

        assertEquals(1500L, total)
    }

    @Test
    fun getTotalAmountByTypeReturnsNullWhenNoMatches() = runBlocking {
        val total = dao.getTotalAmountByType(userId, TransactionType.EXPENSE, 0L, 200L).first()

        assertNull(total)
    }

    @Test
    fun getCategoryBreakdownGroupsSpendByCategory() = runBlocking {
        val foodId = db.categoryDao().insertCategory(
            CategoryEntity(userId = userId, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE)
        )
        val transportId = db.categoryDao().insertCategory(
            CategoryEntity(userId = userId, name = "Transport", icon = null, color = 0, type = TransactionType.EXPENSE)
        )
        dao.insertTransaction(tx(amount = 1000L, categoryId = foodId, transactionDate = 100L))
        dao.insertTransaction(tx(amount = 500L, categoryId = foodId, transactionDate = 150L))
        dao.insertTransaction(tx(amount = 300L, categoryId = transportId, transactionDate = 100L))

        val breakdown = dao.getCategoryBreakdown(userId, TransactionType.EXPENSE, 0L, 200L).first()
            .associateBy({ it.categoryId }, { it.total })

        assertEquals(1500L, breakdown[foodId])
        assertEquals(300L, breakdown[transportId])
    }

    @Test
    fun getDailyTrendByTypeGroupsAndOrdersByDate() = runBlocking {
        dao.insertTransaction(tx(amount = 1000L, transactionDate = 200L))
        dao.insertTransaction(tx(amount = 500L, transactionDate = 200L))
        dao.insertTransaction(tx(amount = 300L, transactionDate = 100L))

        val trend = dao.getDailyTrendByType(userId, TransactionType.EXPENSE, 0L, 300L).first()

        assertEquals(listOf(100L, 200L), trend.map { it.date })
        assertEquals(300L, trend.first { it.date == 100L }.total)
        assertEquals(1500L, trend.first { it.date == 200L }.total)
    }

    @Test
    fun executeTransferInsertsBothLegsAtomically() = runBlocking {
        val outTx = tx(type = TransactionType.TRANSFER, amount = 1000L, accId = accountId, toAccountId = otherAccountId)
        val inTx = tx(type = TransactionType.TRANSFER, amount = 1000L, accId = otherAccountId)

        dao.executeTransfer(outTx, inTx)

        val transactions = dao.getAllTransactions(userId).first()
        assertEquals(2, transactions.size)
        assertTrue(transactions.all { it.type == TransactionType.TRANSFER })
    }
}
