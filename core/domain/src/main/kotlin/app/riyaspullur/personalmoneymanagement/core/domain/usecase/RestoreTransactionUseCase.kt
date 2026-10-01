package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import javax.inject.Inject

class RestoreTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) = 
        transactionRepository.insertTransaction(transaction.copy(isDeleted = false))
}
