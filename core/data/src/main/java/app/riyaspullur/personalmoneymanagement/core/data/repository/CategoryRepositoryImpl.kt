package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.CategoryDao
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override fun getActiveCategoriesByType(userId: Long, type: TransactionType): Flow<List<Category>> =
        categoryDao.getActiveCategoriesByType(userId, type).map { entities -> entities.map { it.toDomain() } }

    override fun getAllActiveCategories(userId: Long): Flow<List<Category>> =
        categoryDao.getAllActiveCategories(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAllCategoriesForBackup(userId: Long): List<Category> =
        categoryDao.getAllCategories(userId).map { it.toDomain() }

    override suspend fun insertCategory(category: Category): Long =
        categoryDao.insertCategory(category.toEntity())
}
