package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: Long, userId: Long) {
        transactionRepository.softDeleteTransaction(transactionId, userId)
    }
}
