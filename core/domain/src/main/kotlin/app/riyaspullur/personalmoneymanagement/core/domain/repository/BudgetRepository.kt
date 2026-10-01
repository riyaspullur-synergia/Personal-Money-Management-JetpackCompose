package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.Budget
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetCategoryLimit
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetRecord
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllBudgets(userId: Long): Flow<List<Budget>>
    suspend fun getAllBudgetsForBackup(userId: Long): List<BudgetRecord>
    suspend fun getAllBudgetCategoriesForBackup(userId: Long): List<BudgetCategoryLimit>
    fun getActiveBudget(userId: Long, date: Long = System.currentTimeMillis()): Flow<Budget?>
    suspend fun insertBudget(budget: BudgetRecord, categories: List<BudgetCategoryLimit>): Long
    fun getBudgetCategories(budgetId: Long): Flow<List<BudgetCategoryLimit>>
}
