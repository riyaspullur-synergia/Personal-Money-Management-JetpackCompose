package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsInRangeUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(userId: Long, startDate: Long, endDate: Long): Flow<List<Transaction>> = 
        transactionRepository.getTransactionsInRange(userId, startDate, endDate)
}
