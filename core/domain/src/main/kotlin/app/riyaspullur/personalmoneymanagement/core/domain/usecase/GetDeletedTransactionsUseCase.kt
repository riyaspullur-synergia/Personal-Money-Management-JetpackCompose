package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDeletedTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(userId: Long): Flow<List<Transaction>> = 
        transactionRepository.getDeletedTransactions(userId)
}
