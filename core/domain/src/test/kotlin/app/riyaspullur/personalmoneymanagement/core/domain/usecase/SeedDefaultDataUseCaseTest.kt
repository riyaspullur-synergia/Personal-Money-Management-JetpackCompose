package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedDefaultDataUseCaseTest {

    private val categoryRepository: CategoryRepository = mockk()
    private val useCase = SeedDefaultDataUseCase(categoryRepository)

    @Test
    fun `inserts five default categories for the user`() = runTest {
        val inserted = mutableListOf<Category>()
        coEvery { categoryRepository.insertCategory(capture(inserted)) } returns 1L

        useCase(userId = 7L)

        assertEquals(5, inserted.size)
        assertTrue(inserted.all { it.userId == 7L })
        assertTrue(inserted.any { it.name == "Salary" && it.type == TransactionType.INCOME })
        assertEquals(4, inserted.count { it.type == TransactionType.EXPENSE })
    }
}
