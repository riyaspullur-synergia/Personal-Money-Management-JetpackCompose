package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.SavingsGoalDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.SavingsGoalEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SavingsGoalRepositoryImplTest {

    private val dao: SavingsGoalDao = mockk()
    private val repository = SavingsGoalRepositoryImpl(dao)

    private val goal = SavingsGoalEntity(
        id = 1, userId = 1, name = "Car", targetAmount = 100000L, currency = Currency.AED,
        targetDate = null, icon = null, color = 0
    )

    @Test
    fun `getActiveGoals delegates to dao`() = runTest {
        every { dao.getActiveGoals(1L) } returns flowOf(listOf(goal))

        repository.getActiveGoals(1L).test {
            assertEquals(listOf(goal.toDomain()), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertGoal delegates to dao and returns the new id`() = runTest {
        coEvery { dao.insertGoal(goal) } returns 5L

        val id = repository.insertGoal(goal.toDomain())

        assertEquals(5L, id)
        coVerify { dao.insertGoal(goal) }
    }

    @Test
    fun `updateGoal delegates to dao`() = runTest {
        coEvery { dao.updateGoal(goal) } returns Unit

        repository.updateGoal(goal.toDomain())

        coVerify { dao.updateGoal(goal) }
    }

    @Test
    fun `contributeToGoal delegates to dao`() = runTest {
        coEvery { dao.contributeToGoal(1L, 5000L) } returns Unit

        repository.contributeToGoal(1L, 5000L)

        coVerify { dao.contributeToGoal(1L, 5000L) }
    }
}
