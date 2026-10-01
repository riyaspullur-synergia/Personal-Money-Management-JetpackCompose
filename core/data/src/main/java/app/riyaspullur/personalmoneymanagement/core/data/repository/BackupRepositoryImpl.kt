package app.riyaspullur.personalmoneymanagement.core.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.room.withTransaction
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupArchive
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupData
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import app.riyaspullur.personalmoneymanagement.core.domain.model.validate
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BackupRepository
import app.riyaspullur.personalmoneymanagement.core.export.BackupCodec
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: AppDatabase,
    private val preferences: AppPreferencesDataSource,
    private val codec: BackupCodec
) : BackupRepository {
    private val mutex = Mutex()

    override suspend fun export(userId: Long, destination: String, format: BackupFormat): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            check(preferences.currentUserId.first() == userId)
            val archive = database.withTransaction {
                val user = requireNotNull(database.userDao().getUserById(userId).first())
                BackupArchive(
                    createdAt = System.currentTimeMillis(),
                    displayName = user.displayName,
                    themeMode = preferences.themeMode.first(),
                    language = preferences.language.first(),
                    data = BackupData(
                        accounts = database.accountDao().getAllAccounts(userId).map { it.toDomain() },
                        categories = database.categoryDao().getAllCategories(userId).map { it.toDomain() },
                        transactions = database.transactionDao().getAllTransactionsBackup(userId).map { it.toDomain() },
                        budgets = database.budgetDao().getAllBudgetsBackup(userId).map { it.toDomain() },
                        budgetCategoryLimits = database.budgetDao().getAllBudgetCategoriesBackup(userId).map { it.toDomain() },
                        savingsGoals = database.savingsGoalDao().getAllGoals(userId).map { it.toDomain() },
                        accountGroups = database.accountGroupDao().getAllGroups(userId).map { it.toDomain() }
                    )
                )
            }
            var receiptBytes = 0
            val receipts = archive.data.transactions.filter { it.receiptPath != null }.associate { transaction ->
                val path = requireNotNull(transaction.receiptPath)
                val uri = Uri.parse(path)
                val input = when (uri.scheme) {
                    "content" -> context.contentResolver.openInputStream(uri)
                    "file" -> File(requireNotNull(uri.path)).inputStream()
                    null -> File(path).inputStream()
                    else -> error("Unsupported receipt location")
                }
                val bytes = requireNotNull(input).use { stream ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (true) {
                        val size = stream.read(buffer)
                        if (size < 0) break
                        receiptBytes += size
                        require(receiptBytes <= 8 * 1024 * 1024) { "Receipts exceed backup size limit" }
                        output.write(buffer, 0, size)
                    }
                    output.toByteArray()
                }
                transaction.id.toString() to Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
            val complete = archive.copy(receipts = receipts)
            complete.validate()
            // Stage and verify before opening the selected document for replacement.
            val staged = File.createTempFile("backup-", ".${format.extension}", context.cacheDir)
            try {
                staged.outputStream().use { codec.write(complete, format, it) }
                staged.inputStream().use { codec.read(it).validate() }
                requireNotNull(context.contentResolver.openOutputStream(Uri.parse(destination), "wt")).use { output ->
                    staged.inputStream().use { it.copyTo(output) }
                }
            } finally { staged.delete() }
        }
    }

    override suspend fun read(source: String): BackupArchive = withContext(Dispatchers.IO) {
        requireNotNull(context.contentResolver.openInputStream(Uri.parse(source))).use { codec.read(it) }
            .also { archive ->
                archive.validate()
                // Decode now so invalid attachments fail before the user confirms replacement.
                archive.receipts.values.forEach { Base64.decode(it, Base64.NO_WRAP) }
            }
    }

    override suspend fun restore(userId: Long, archive: BackupArchive): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            archive.validate()
            check(preferences.currentUserId.first() == userId)
            val receiptDirectory = File(context.filesDir, "receipts/restore-${UUID.randomUUID()}")
            val receiptPaths = mutableMapOf<Long, String>()
            var committed = false
            try {
                if (archive.receipts.isNotEmpty()) check(receiptDirectory.mkdirs())
                archive.receipts.forEach { (id, encoded) ->
                    val file = File(receiptDirectory, "${id.toLong()}.receipt")
                    file.writeBytes(Base64.decode(encoded, Base64.NO_WRAP))
                    receiptPaths[id.toLong()] = file.absolutePath
                }
                // Once replacement starts, finish both the database commit and preference attempt even if the screen closes.
                withContext(NonCancellable) {
                    database.withTransaction {
                        check(preferences.currentUserId.first() == userId)
                        val user = requireNotNull(database.userDao().getUserById(userId).first())
                        with(database.backupDao()) {
                            deleteTransactions(userId)
                            deleteBudgets(userId)
                            deleteAccounts(userId)
                            deleteCategories(userId)
                            deleteSavingsGoals(userId)
                            deleteAccountGroups(userId)
                        }
                        with(archive.data) {
                            val groups = accountGroups.associate { group ->
                                group.id to database.accountGroupDao().insertGroup(group.copy(id = 0, userId = userId).toEntity())
                            }
                            val categoryIds = categories.associate { category ->
                                category.id to database.categoryDao().insertCategory(category.copy(id = 0, userId = userId, parentCategoryId = null).toEntity())
                            }
                            categories.filter { it.parentCategoryId != null }.forEach { category ->
                                database.categoryDao().updateCategory(category.copy(
                                    id = categoryIds.getValue(category.id), userId = userId,
                                    parentCategoryId = categoryIds.getValue(requireNotNull(category.parentCategoryId))
                                ).toEntity())
                            }
                            val accountIds = accounts.associate { account ->
                                account.id to database.accountDao().insertAccount(account.copy(
                                    id = 0, userId = userId, groupId = account.groupId?.let { groups.getValue(it) }
                                ).toEntity())
                            }
                            val budgetIds = budgets.associate { budget ->
                                budget.id to database.budgetDao().insertBudget(budget.copy(id = 0, userId = userId).toEntity())
                            }
                            database.budgetDao().insertBudgetCategories(budgetCategoryLimits.map { limit ->
                                limit.copy(id = 0, budgetId = budgetIds.getValue(limit.budgetId), categoryId = categoryIds.getValue(limit.categoryId)).toEntity()
                            })
                            savingsGoals.forEach { database.savingsGoalDao().insertGoal(it.copy(id = 0, userId = userId).toEntity()) }
                            transactions.forEach { transaction ->
                                database.transactionDao().insertTransaction(transaction.copy(
                                    id = 0, userId = userId, accountId = accountIds.getValue(transaction.accountId),
                                    toAccountId = transaction.toAccountId?.let { accountIds.getValue(it) },
                                    categoryId = transaction.categoryId?.let { categoryIds.getValue(it) },
                                    receiptPath = receiptPaths[transaction.id]
                                ).toEntity())
                            }
                        }
                        database.userDao().updateUser(user.copy(displayName = archive.displayName))
                    }
                    committed = true
                    try {
                        preferences.restoreAppearance(archive.themeMode, archive.language)
                        true
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Exception) {
                        false
                    }
                }
            } finally {
                if (!committed) receiptDirectory.deleteRecursively()
            }
        }
    }
}
