package app.riyaspullur.personalmoneymanagement.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountGroupDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.BudgetDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.CategoryDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.SavingsGoalDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.TransactionDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.UserDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.AccountGroupEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetCategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.SavingsGoalEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        AccountEntity::class,
        AccountGroupEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        SavingsGoalEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun backupDao(): app.riyaspullur.personalmoneymanagement.core.database.dao.BackupDao
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao
    abstract fun accountGroupDao(): AccountGroupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
}
