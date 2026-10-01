package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetStatus
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BudgetRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalculateBudgetStatusUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(userId: Long): Flow<BudgetStatus?> {
        return budgetRepository.getActiveBudget(userId).flatMapLatest { budget ->
            if (budget == null) return@flatMapLatest flowOf(null)

            transactionRepository.getTotalAmountByType(
                userId,
                TransactionType.EXPENSE,
                budget.startDate,
                budget.endDate
            ).map { spentAmount ->
                val now = System.currentTimeMillis()
                val daysRemaining = if (budget.endDate > now) {
                    ((budget.endDate - now) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                } else 1
                
                val totalLimitAmount = budget.totalLimit.amount
                val remainingAmount = totalLimitAmount - spentAmount
                val dailyAllowance = if (daysRemaining > 0) {
                    Money(remainingAmount / daysRemaining, budget.totalLimit.currency)
                } else Money(0, budget.totalLimit.currency)

                BudgetStatus(
                    budget = budget,
                    spent = Money(spentAmount, budget.totalLimit.currency),
                    remaining = Money(remainingAmount, budget.totalLimit.currency),
                    progress = if (totalLimitAmount > 0) spentAmount.toFloat() / totalLimitAmount else 0f,
                    daysRemaining = daysRemaining,
                    dailyAllowance = dailyAllowance
                )
            }
        }
    }
}
