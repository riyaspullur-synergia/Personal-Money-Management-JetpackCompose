package app.riyaspullur.personalmoneymanagement.core.di

import android.content.Context
import androidx.room.Room
import app.riyaspullur.personalmoneymanagement.core.database.AppDatabase
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountGroupDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.BudgetDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.CategoryDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.SavingsGoalDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.TransactionDao
import app.riyaspullur.personalmoneymanagement.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finance_db"
        ).build()
    }

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideAccountGroupDao(db: AppDatabase): AccountGroupDao = db.accountGroupDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideSavingsGoalDao(db: AppDatabase): SavingsGoalDao = db.savingsGoalDao()
}
