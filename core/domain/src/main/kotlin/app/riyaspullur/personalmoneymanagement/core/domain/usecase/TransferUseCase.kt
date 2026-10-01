package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import javax.inject.Inject

class TransferUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        userId: Long,
        fromAccountId: Long,
        toAccountId: Long,
        amount: Long,
        currency: Currency,
        date: Long = System.currentTimeMillis(),
        note: String? = null
    ) {
        transactionRepository.transfer(
            userId = userId,
            fromAccountId = fromAccountId,
            toAccountId = toAccountId,
            amount = amount,
            currency = currency,
            date = date,
            note = note
        )
    }
}
