package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.CategoryDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.CategoryEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryRepositoryImplTest {

    private val dao: CategoryDao = mockk()
    private val repository = CategoryRepositoryImpl(dao)

    @Test
    fun `getActiveCategoriesByType delegates to dao`() = runTest {
        val categories = listOf(CategoryEntity(id = 1, userId = 1, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE))
        every { dao.getActiveCategoriesByType(1L, TransactionType.EXPENSE) } returns flowOf(categories)

        repository.getActiveCategoriesByType(1L, TransactionType.EXPENSE).test {
            assertEquals(categories.map { it.toDomain() }, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllActiveCategories delegates to dao`() = runTest {
        val categories = listOf(CategoryEntity(id = 1, userId = 1, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE))
        every { dao.getAllActiveCategories(1L) } returns flowOf(categories)

        repository.getAllActiveCategories(1L).test {
            assertEquals(categories.map { it.toDomain() }, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertCategory delegates to dao and returns the new id`() = runTest {
        val category = CategoryEntity(userId = 1, name = "Food", icon = null, color = 0, type = TransactionType.EXPENSE)
        coEvery { dao.insertCategory(category) } returns 3L

        val id = repository.insertCategory(category.toDomain())

        assertEquals(3L, id)
        coVerify { dao.insertCategory(category) }
    }
}
