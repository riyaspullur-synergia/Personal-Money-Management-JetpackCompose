package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import javax.inject.Inject

class ClearRecycleBinUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(userId: Long) = 
        transactionRepository.clearRecycleBin(userId)
}
