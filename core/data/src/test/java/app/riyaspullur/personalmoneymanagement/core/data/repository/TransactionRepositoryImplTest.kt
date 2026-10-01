package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.TransactionDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.TransactionEntity
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionRepositoryImplTest {

    private val dao: TransactionDao = mockk()
    private val repository = TransactionRepositoryImpl(dao)

    @Test
    fun `getAllTransactions delegates to dao`() = runTest {
        every { dao.getAllTransactions(1L) } returns flowOf(emptyList())

        repository.getAllTransactions(1L).test {
            assertEquals(emptyList<Transaction>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getTotalAmountByType maps a null dao result to zero`() = runTest {
        every { dao.getTotalAmountByType(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(null)

        repository.getTotalAmountByType(1L, TransactionType.EXPENSE, 0L, 1000L).test {
            assertEquals(0L, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getTotalAmountByType passes through a non-null dao result`() = runTest {
        every { dao.getTotalAmountByType(1L, TransactionType.EXPENSE, 0L, 1000L, null) } returns flowOf(75000L)

        repository.getTotalAmountByType(1L, TransactionType.EXPENSE, 0L, 1000L).test {
            assertEquals(75000L, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `insertTransaction delegates to dao and returns the new id`() = runTest {
        val tx = Transaction(userId = 1, accountId = 1, amount = 100L, currency = Currency.AED, categoryId = null, type = TransactionType.EXPENSE, merchant = null, description = null, notes = null, transactionDate = 0L)
        coEvery { dao.insertTransaction(tx.toEntity()) } returns 11L

        val id = repository.insertTransaction(tx)

        assertEquals(11L, id)
        coVerify { dao.insertTransaction(tx.toEntity()) }
    }

    @Test
    fun `softDeleteTransaction delegates to dao`() = runTest {
        coEvery { dao.softDeleteTransaction(1L, 2L) } returns Unit

        repository.softDeleteTransaction(1L, 2L)

        coVerify { dao.softDeleteTransaction(1L, 2L) }
    }

    @Test
    fun `clearRecycleBin delegates to dao`() = runTest {
        coEvery { dao.clearRecycleBin(1L) } returns Unit

        repository.clearRecycleBin(1L)

        coVerify { dao.clearRecycleBin(1L) }
    }

    @Test
    fun `transfer builds a TRANSFER transaction from the source account`() = runTest {
        val captured = slot<TransactionEntity>()
        coEvery { dao.insertTransaction(capture(captured)) } returns 1L

        repository.transfer(
            userId = 1L, fromAccountId = 10L, toAccountId = 20L,
            amount = 5000L, currency = Currency.AED, date = 123456L, note = "Rent"
        )

        val tx = captured.captured
        assertEquals(1L, tx.userId)
        assertEquals(10L, tx.accountId)
        assertEquals(20L, tx.toAccountId)
        assertEquals(5000L, tx.amount)
        assertEquals(TransactionType.TRANSFER, tx.type)
        assertEquals("Rent", tx.description)
        assertEquals(123456L, tx.transactionDate)
    }

    @Test
    fun `transfer falls back to a default description when no note is given`() = runTest {
        val captured = slot<TransactionEntity>()
        coEvery { dao.insertTransaction(capture(captured)) } returns 1L

        repository.transfer(
            userId = 1L, fromAccountId = 10L, toAccountId = 20L,
            amount = 5000L, currency = Currency.AED, date = 123456L, note = null
        )

        assertEquals("Transfer between accounts", captured.captured.description)
    }
}
