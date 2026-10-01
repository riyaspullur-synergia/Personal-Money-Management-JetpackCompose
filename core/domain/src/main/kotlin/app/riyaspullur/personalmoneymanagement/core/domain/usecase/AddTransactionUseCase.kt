package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainError
import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainResult
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import app.riyaspullur.personalmoneymanagement.core.domain.validation.FinanceValidator
import app.riyaspullur.personalmoneymanagement.core.domain.validation.ValidationResult
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): DomainResult<Long> {
        val validation = FinanceValidator.validateTransaction(transaction.amount, transaction.description)
        if (validation is ValidationResult.Error) {
            return DomainResult.Error(DomainError.ValidationError(validation.message))
        }
        
        return try {
            val id = transactionRepository.insertTransaction(transaction)
            DomainResult.Success(id)
        } catch (e: Exception) {
            DomainResult.Error(DomainError.DatabaseError(e.message ?: "Unknown database error"))
        }
    }
}
