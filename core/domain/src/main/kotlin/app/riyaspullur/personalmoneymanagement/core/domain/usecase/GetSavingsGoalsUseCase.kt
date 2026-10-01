package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavingsGoalsUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {
    operator fun invoke(userId: Long): Flow<List<SavingsGoal>> {
        return savingsGoalRepository.getActiveGoals(userId)
    }
}
