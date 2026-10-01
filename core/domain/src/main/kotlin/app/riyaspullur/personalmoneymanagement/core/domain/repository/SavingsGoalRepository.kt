package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow

interface SavingsGoalRepository {
    fun getActiveGoals(userId: Long): Flow<List<SavingsGoal>>
    suspend fun getAllGoalsForBackup(userId: Long): List<SavingsGoal>
    suspend fun insertGoal(goal: SavingsGoal): Long
    suspend fun updateGoal(goal: SavingsGoal)
    suspend fun contributeToGoal(goalId: Long, amount: Long)
}
