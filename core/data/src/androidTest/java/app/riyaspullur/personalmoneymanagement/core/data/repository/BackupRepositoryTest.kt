package app.riyaspullur.personalmoneymanagement.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountGroupEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetCategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.SavingsGoalEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.export.BackupCodec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class BackupRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences = AppPreferencesDataSource(context)
    private lateinit var source: AppDatabase
    private lateinit var destination: AppDatabase
    private lateinit var directory: File

    @Before
    fun setup(): Unit = runBlocking {
        source = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        destination = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        directory = File(context.cacheDir, "backup-test-${System.nanoTime()}").apply { mkdirs() }
        source.userDao().insertUser(UserEntity(1, "source", "source-secret", "Source profile"))
        destination.userDao().insertUser(UserEntity(1, "other", "other-secret", "Other profile"))
        destination.userDao()
            .insertUser(UserEntity(2, "destination", "destination-secret", "Destination"))
        source.accountGroupDao().insertGroup(AccountGroupEntity(1, 1, "Archived group", 4, true))
        source.accountDao()
            .insertAccount(account(1, 1, "Wallet").copy(groupId = 1, isArchived = true))
        source.accountDao().insertAccount(account(2, 1, "Bank"))
        source.categoryDao().insertCategory(
            CategoryEntity(
                1,
                1,
                "Parent",
                null,
                42,
                TransactionType.EXPENSE,
                isArchived = true
            )
        )
        source.categoryDao().insertCategory(
            CategoryEntity(
                2,
                1,
                "Child",
                "icon",
                43,
                TransactionType.EXPENSE,
                parentCategoryId = 1
            )
        )
        val receipt =
            File(directory, "original.png").apply { writeBytes(byteArrayOf(0, 1, 2, -1, 12)) }
        source.transactionDao().insertTransaction(
            TransactionEntity(
                id = 1, userId = 1, accountId = 1, toAccountId = 2, amount = 321,
                currency = Currency.AED, categoryId = 2, type = TransactionType.TRANSFER,
                merchant = "Merchant", description = "مرحبا, \"Hello\"", notes = "Line 1\nLine 2",
                transactionDate = 12345, createdAt = 12346, updatedAt = 12347,
                paymentStatus = "PENDING", receiptPath = receipt.absolutePath, isRecurring = true
            )
        )
        source.transactionDao().insertTransaction(
            TransactionEntity(
                id = 2, userId = 1, accountId = 2, amount = 999, currency = Currency.INR,
                categoryId = null, type = TransactionType.EXPENSE, merchant = null,
                description = null, notes = null, transactionDate = 456, isDeleted = true
            )
        )
        source.budgetDao()
            .insertBudget(BudgetEntity(1, 1, "Budget", 900, Currency.AED, 1, 50000, 0.7f, true, 8))
        source.budgetDao().insertBudgetCategories(listOf(BudgetCategoryEntity(1, 1, 2, 400)))
        source.savingsGoalDao().insertGoal(
            SavingsGoalEntity(
                1,
                1,
                "Completed goal",
                700,
                700,
                Currency.AED,
                50000,
                null,
                42,
                true,
                9
            )
        )
        destination.accountDao().insertAccount(account(1, 1, "Other user account"))
        destination.accountDao().insertAccount(account(2, 2, "Old destination account"))
    }

    @After
    fun teardown() = runBlocking {
        destination.transactionDao().getAllTransactionsBackup(2).mapNotNull { it.receiptPath }
            .forEach {
                val file = File(it)
                if (file.parentFile?.name?.startsWith("restore-") == true) file.parentFile?.deleteRecursively()
            }
        preferences.setCurrentUserId(null)
        source.close()
        destination.close()
        directory.deleteRecursively()
        Unit
    }

    @Test
    fun csvRestoresAcrossDifferentUserIdsAndRepeatedRestoreDoesNotDuplicate() = runBlocking {
        verifyTransfer(BackupFormat.CSV)
    }

    @Test
    fun excelRestoresAcrossDifferentUserIdsAndRepeatedRestoreDoesNotDuplicate() = runBlocking {
        verifyTransfer(BackupFormat.XLSX)
    }

    @Test
    fun databaseFailureRollsBackExistingRecords() = runBlocking {
        val backup = createBackup(BackupFormat.CSV)
        val repository = repository(destination)
        val archive = repository.read(backup)
        preferences.setCurrentUserId(2)
        destination.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_restore BEFORE INSERT ON accounts WHEN NEW.name = 'Wallet' BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        var failed = false
        try {
            repository.restore(2, archive)
        } catch (_: Exception) {
            failed = true
        }
        assertTrue(failed)
        assertEquals(
            "Old destination account",
            destination.accountDao().getAllAccounts(2).single().name
        )
        assertEquals("Other user account", destination.accountDao().getAllAccounts(1).single().name)
        assertEquals("Destination", destination.userDao().getUserById(2).first()!!.displayName)
    }

    private suspend fun verifyTransfer(format: BackupFormat) {
        val backup = createBackup(format)
        val repository = repository(destination)
        val archive = repository.read(backup)
        preferences.setCurrentUserId(2)
        preferences.restoreAppearance("LIGHT", "en")
        repeat(2) {
            assertTrue(repository.restore(2, archive))
            val accounts = destination.accountDao().getAllAccounts(2)
            assertEquals(2, accounts.size)
            val wallet = accounts.single { it.name == "Wallet" }
            val bank = accounts.single { it.name == "Bank" }
            assertTrue(wallet.isArchived)
            assertEquals(9_007_199_254_740_993L, wallet.initialBalance)
            assertEquals(destination.accountGroupDao().getAllGroups(2).single().id, wallet.groupId)
            val categories = destination.categoryDao().getAllCategories(2)
            val child = categories.single { it.name == "Child" }
            assertEquals(categories.single { it.name == "Parent" }.id, child.parentCategoryId)
            val transactions = destination.transactionDao().getAllTransactionsBackup(2)
            assertEquals(2, transactions.size)
            assertEquals(1, transactions.count { it.isDeleted })
            val transfer = transactions.single { !it.isDeleted }
            assertEquals(wallet.id, transfer.accountId)
            assertEquals(bank.id, transfer.toAccountId)
            assertEquals(child.id, transfer.categoryId)
            assertEquals("PENDING", transfer.paymentStatus)
            assertTrue(transfer.isRecurring)
            assertEquals(12347L, transfer.updatedAt)
            assertEquals("Line 1\nLine 2", transfer.notes)
            assertArrayEquals(
                byteArrayOf(0, 1, 2, -1, 12),
                File(transfer.receiptPath!!).readBytes()
            )
            assertNotEquals(archive.data.transactions.first().receiptPath, transfer.receiptPath)
            assertEquals(
                source.accountDao().getAccountTransactionBalance(1).first(),
                destination.accountDao().getAccountTransactionBalance(wallet.id).first()
            )
            assertEquals(
                source.accountDao().getAccountTransactionBalance(2).first(),
                destination.accountDao().getAccountTransactionBalance(bank.id).first()
            )
            val limit = destination.budgetDao().getAllBudgetCategoriesBackup(2).single()
            assertEquals(destination.budgetDao().getAllBudgetsBackup(2).single().id, limit.budgetId)
            assertEquals(child.id, limit.categoryId)
            assertTrue(destination.savingsGoalDao().getAllGoals(2).single().isCompleted)
            assertEquals(
                "Other user account",
                destination.accountDao().getAllAccounts(1).single().name
            )
            assertEquals(
                "destination-secret",
                destination.userDao().getUserById(2).first()!!.passwordHash
            )
            assertEquals(
                "Source profile",
                destination.userDao().getUserById(2).first()!!.displayName
            )
            assertEquals("DARK", preferences.themeMode.first())
            assertEquals("ar", preferences.language.first())
        }
    }

    private suspend fun createBackup(format: BackupFormat): String {
        preferences.setCurrentUserId(1)
        preferences.restoreAppearance("DARK", "ar")
        val uri = Uri.fromFile(File(directory, "backup.${format.extension}")).toString()
        repository(source).export(1, uri, format)
        return uri
    }

    private fun repository(database: AppDatabase) =
        BackupRepositoryImpl(context, database, preferences, BackupCodec())

    private fun account(id: Long, userId: Long, name: String) = AccountEntity(
        id = id, userId = userId, name = name, type = AccountType.entries.first(),
        initialBalance = 9_007_199_254_740_993, currency = Currency.AED, icon = null, color = 42
    )
}
