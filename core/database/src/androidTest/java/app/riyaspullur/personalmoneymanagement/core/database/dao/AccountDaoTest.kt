package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented: exercises Room against a real SQLite engine, which the plain JVM unit-test
 * classpath cannot provide. Run on a connected device/emulator via `connectedAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class AccountDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var accountDao: AccountDao
    private lateinit var userDao: UserDao
    private lateinit var transactionDao: TransactionDao
    private var userId: Long = 0

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        accountDao = db.accountDao()
        userDao = db.userDao()
        transactionDao = db.transactionDao()
        userId = userDao.insertUser(
            UserEntity(
                username = "riyas",
                passwordHash = "hash",
                displayName = "Riyas"
            )
        )
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun account(name: String = "Cash", isArchived: Boolean = false) = AccountEntity(
        userId = userId, name = name, type = AccountType.CASH, initialBalance = 10000L,
        currency = Currency.AED, icon = null, color = 0, isArchived = isArchived
    )

    @Test
    fun insertAndRetrieveAccountById() = runBlocking {
        val id = accountDao.insertAccount(account())

        val fetched = accountDao.getAccountById(id, userId)

        assertEquals("Cash", fetched?.name)
    }

    @Test
    fun getAccountByIdReturnsNullForAnotherUser() = runBlocking {
        val id = accountDao.insertAccount(account())
        val otherUserId = userDao.insertUser(
            UserEntity(
                username = "other",
                passwordHash = "hash",
                displayName = "Other"
            )
        )

        assertNull(accountDao.getAccountById(id, otherUserId))
    }

    @Test
    fun getActiveAccountsExcludesArchivedAccounts() = runBlocking {
        accountDao.insertAccount(account(name = "Active"))
        accountDao.insertAccount(account(name = "Archived", isArchived = true))

        val active = accountDao.getActiveAccounts(userId).first()

        assertEquals(1, active.size)
        assertEquals("Active", active.single().name)
    }

    @Test
    fun getActiveAccountsExcludesOtherUsers() = runBlocking {
        val otherUserId = userDao.insertUser(
            UserEntity(
                username = "other",
                passwordHash = "hash",
                displayName = "Other"
            )
        )
        accountDao.insertAccount(account(name = "Mine"))
        accountDao.insertAccount(account(name = "Theirs").copy(userId = otherUserId))

        val active = accountDao.getActiveAccounts(userId).first()

        assertEquals(1, active.size)
        assertEquals("Mine", active.single().name)
    }

    @Test
    fun updateAccountPersistsChanges() = runBlocking {
        val id = accountDao.insertAccount(account())
        val stored = accountDao.getAccountById(id, userId)!!

        accountDao.updateAccount(stored.copy(name = "Renamed"))

        assertEquals("Renamed", accountDao.getAccountById(id, userId)?.name)
    }

    @Test
    fun deleteAccountRemovesIt() = runBlocking {
        val id = accountDao.insertAccount(account())
        val stored = accountDao.getAccountById(id, userId)!!

        accountDao.deleteAccount(stored)

        assertNull(accountDao.getAccountById(id, userId))
    }

    @Test
    fun getTotalInitialBalanceSumsOnlyActiveAccounts() = runBlocking {
        accountDao.insertAccount(account(name = "A")) // 10000
        accountDao.insertAccount(account(name = "B")) // 10000
        accountDao.insertAccount(account(name = "Archived", isArchived = true)) // excluded

        assertEquals(20000L, accountDao.getTotalInitialBalance(userId).first())
    }

    @Test
    fun accountTransactionBalanceCombinesIncomeExpenseAndTransfers() = runBlocking {
        val cashId = accountDao.insertAccount(account(name = "Cash"))
        val bankId = accountDao.insertAccount(account(name = "Bank"))

        fun tx(type: TransactionType, amount: Long, accountId: Long, toAccountId: Long? = null) =
            TransactionEntity(
                userId = userId, accountId = accountId, toAccountId = toAccountId, amount = amount,
                currency = Currency.AED, categoryId = null, type = type, merchant = null,
                description = null, notes = null, transactionDate = 0L
            )

        transactionDao.insertTransaction(tx(TransactionType.INCOME, 5000L, cashId))
        transactionDao.insertTransaction(tx(TransactionType.EXPENSE, 2000L, cashId))
        // Transfer 1000 from cash to bank: reduces cash, increases bank (via toAccountId)
        transactionDao.insertTransaction(
            tx(
                TransactionType.TRANSFER,
                1000L,
                cashId,
                toAccountId = bankId
            )
        )

        // cash: +5000 (income) - 2000 (expense) - 1000 (outgoing transfer) = 2000
        assertEquals(2000L, accountDao.getAccountTransactionBalance(cashId).first())
        // bank: +1000 (incoming transfer)
        assertEquals(1000L, accountDao.getAccountTransactionBalance(bankId).first())
    }
}
