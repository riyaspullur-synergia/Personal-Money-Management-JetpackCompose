package app.riyaspullur.personalmoneymanagement.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetCategoryEntity
import app.riyaspullur.personalmoneymanagement.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY startDate DESC")
    fun getAllBudgets(userId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    suspend fun getAllBudgetsBackup(userId: Long): List<BudgetEntity>

    @Query("SELECT * FROM budget_categories WHERE budgetId IN (SELECT id FROM budgets WHERE userId = :userId)")
    suspend fun getAllBudgetCategoriesBackup(userId: Long): List<BudgetCategoryEntity>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND :date BETWEEN startDate AND endDate LIMIT 1")
    fun getActiveBudget(userId: Long, date: Long = System.currentTimeMillis()): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetCategories(categories: List<BudgetCategoryEntity>)

    @Query("SELECT * FROM budget_categories WHERE budgetId = :budgetId")
    fun getBudgetCategories(budgetId: Long): Flow<List<BudgetCategoryEntity>>
}
