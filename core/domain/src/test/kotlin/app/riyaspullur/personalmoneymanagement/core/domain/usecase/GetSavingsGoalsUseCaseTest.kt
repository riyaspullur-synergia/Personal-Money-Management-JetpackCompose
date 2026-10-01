package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SavingsGoalRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSavingsGoalsUseCaseTest {

    private val savingsGoalRepository: SavingsGoalRepository = mockk()
    private val useCase = GetSavingsGoalsUseCase(savingsGoalRepository)

    @Test
    fun `delegates to the repository for the given user`() = runTest {
        val goals = listOf(
            SavingsGoal(id = 1, userId = 1, name = "Car", targetAmount = 100000L, currency = Currency.AED, targetDate = null, icon = null, color = 0)
        )
        every { savingsGoalRepository.getActiveGoals(1L) } returns flowOf(goals)

        useCase(1L).test {
            assertEquals(goals, awaitItem())
            awaitComplete()
        }
    }
}
