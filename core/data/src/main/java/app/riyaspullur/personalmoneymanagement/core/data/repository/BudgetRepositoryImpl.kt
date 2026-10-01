package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.BudgetDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.Budget
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetCategoryLimit
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao
) : BudgetRepository {
    override fun getAllBudgets(userId: Long): Flow<List<Budget>> =
        budgetDao.getAllBudgets(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun getAllBudgetsForBackup(userId: Long): List<BudgetRecord> =
        budgetDao.getAllBudgetsBackup(userId).map { it.toDomain() }

    override suspend fun getAllBudgetCategoriesForBackup(userId: Long): List<BudgetCategoryLimit> =
        budgetDao.getAllBudgetCategoriesBackup(userId).map { it.toDomain() }

    override fun getActiveBudget(userId: Long, date: Long): Flow<Budget?> =
        budgetDao.getActiveBudget(userId, date).map { it?.toDomainModel() }

    override suspend fun insertBudget(budget: BudgetRecord, categories: List<BudgetCategoryLimit>): Long {
        val budgetId = budgetDao.insertBudget(budget.toEntity())
        val categoriesWithId = categories.map { it.copy(budgetId = budgetId).toEntity() }
        budgetDao.insertBudgetCategories(categoriesWithId)
        return budgetId
    }

    override fun getBudgetCategories(budgetId: Long): Flow<List<BudgetCategoryLimit>> =
        budgetDao.getBudgetCategories(budgetId).map { entities -> entities.map { it.toDomain() } }
}

fun BudgetEntity.toDomainModel(): Budget {
    return Budget(
        id = id,
        userId = userId,
        name = name,
        totalLimit = Money(totalLimit, currency),
        startDate = startDate,
        endDate = endDate,
        alertThreshold = alertThreshold,
        isRolloverEnabled = isRolloverEnabled
    )
}
