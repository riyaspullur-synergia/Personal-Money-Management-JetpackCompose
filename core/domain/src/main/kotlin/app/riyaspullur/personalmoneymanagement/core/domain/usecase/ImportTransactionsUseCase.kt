package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.CategoryRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionImporter
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class ImportTransactionsUseCase @Inject constructor(
    private val importer: TransactionImporter,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    suspend operator fun invoke(userId: Long, source: Any): Int {
        val rawData = importer.parseTransactions(source)
        if (rawData.isEmpty()) return 0

        val accounts = accountRepository.getActiveAccounts(userId).first()
        val categories = categoryRepository.getAllActiveCategories(userId).first()

        val transactionsToInsert = rawData.mapNotNull { raw ->
            try {
                val account = accounts.find { it.name.equals(raw.accountName, ignoreCase = true) }
                    ?: return@mapNotNull null
                
                val category = categories.find { it.name.equals(raw.categoryName, ignoreCase = true) }
                
                val type = try {
                    TransactionType.valueOf(raw.type.uppercase())
                } catch (e: Exception) {
                    TransactionType.EXPENSE
                }

                val amount = try {
                    (raw.amount.toDouble() * 100).toLong()
                } catch (e: Exception) {
                    0L
                }

                if (amount == 0L) return@mapNotNull null

                val currency = try {
                    Currency.valueOf(raw.currency.uppercase())
                } catch (e: Exception) {
                    account.currency
                }

                val date = try {
                    dateFormat.parse(raw.date)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                Transaction(
                    userId = userId,
                    accountId = account.id,
                    amount = amount,
                    currency = currency,
                    categoryId = category?.id,
                    type = type,
                    merchant = if (raw.merchant.isBlank()) null else raw.merchant,
                    description = if (raw.description.isBlank()) null else raw.description,
                    notes = null,
                    transactionDate = date,
                    paymentStatus = raw.status.uppercase()
                )
            } catch (e: Exception) {
                null
            }
        }

        if (transactionsToInsert.isNotEmpty()) {
            transactionRepository.insertTransactions(transactionsToInsert)
        }

        return transactionsToInsert.size
    }
}
