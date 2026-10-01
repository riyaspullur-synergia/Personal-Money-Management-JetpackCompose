package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.SavingsGoalDao
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SavingsGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SavingsGoalRepositoryImpl @Inject constructor(
    private val savingsGoalDao: SavingsGoalDao
) : SavingsGoalRepository {
    override fun getActiveGoals(userId: Long): Flow<List<SavingsGoal>> =
        savingsGoalDao.getActiveGoals(userId).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAllGoalsForBackup(userId: Long): List<SavingsGoal> =
        savingsGoalDao.getAllGoals(userId).map { it.toDomain() }

    override suspend fun insertGoal(goal: SavingsGoal): Long =
        savingsGoalDao.insertGoal(goal.toEntity())

    override suspend fun updateGoal(goal: SavingsGoal) =
        savingsGoalDao.updateGoal(goal.toEntity())

    override suspend fun contributeToGoal(goalId: Long, amount: Long) =
        savingsGoalDao.contributeToGoal(goalId, amount)
}
