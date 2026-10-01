package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getActiveCategoriesByType(userId: Long, type: TransactionType): Flow<List<Category>>
    fun getAllActiveCategories(userId: Long): Flow<List<Category>>
    suspend fun getAllCategoriesForBackup(userId: Long): List<Category>
    suspend fun insertCategory(category: Category): Long
}
