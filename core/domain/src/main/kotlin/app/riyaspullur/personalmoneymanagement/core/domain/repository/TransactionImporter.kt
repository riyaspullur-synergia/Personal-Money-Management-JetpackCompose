package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.RawTransaction

interface TransactionImporter {
    suspend fun parseTransactions(source: Any): List<RawTransaction>
}
