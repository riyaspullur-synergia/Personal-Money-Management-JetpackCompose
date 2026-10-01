package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DomainResult
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class AddTransactionUseCaseTest {

    private val transactionRepository: TransactionRepository = mockk()
    private val useCase = AddTransactionUseCase(transactionRepository)

    @Test
    fun `invoke delegates to repository insertTransaction`() = runTest {
        val transaction = Transaction(
            userId = 1, accountId = 1, amount = 1000L, currency = Currency.AED,
            categoryId = null, type = TransactionType.EXPENSE, merchant = null,
            description = "Test", notes = null, transactionDate = 0L
        )
        coEvery { transactionRepository.insertTransaction(transaction) } returns 1L

        val result = useCase(transaction)

        assertTrue(result is DomainResult.Success)
        coVerify { transactionRepository.insertTransaction(transaction) }
    }
}
