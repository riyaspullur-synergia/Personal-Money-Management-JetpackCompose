package app.riyaspullur.personalmoneymanagement.core.di

import app.riyaspullur.personalmoneymanagement.core.data.repository.AccountGroupRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.data.repository.AccountRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.data.repository.AuthRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.data.repository.BudgetRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.data.repository.CategoryRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.data.repository.SavingsGoalRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.data.repository.TransactionRepositoryImpl
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountGroupRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BudgetRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SavingsGoalRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: app.riyaspullur.personalmoneymanagement.core.data.repository.BackupRepositoryImpl
    ): app.riyaspullur.personalmoneymanagement.core.domain.repository.BackupRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindAccountGroupRepository(impl: AccountGroupRepositoryImpl): AccountGroupRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindSavingsGoalRepository(impl: SavingsGoalRepositoryImpl): SavingsGoalRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
