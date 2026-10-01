package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getActiveAccounts(userId: Long): Flow<List<AccountRecord>>
    fun getActiveAccountsWithBalances(userId: Long): Flow<List<Account>>
    suspend fun getAccountById(accountId: Long, userId: Long): AccountRecord?
    suspend fun getAllAccountsForBackup(userId: Long): List<AccountRecord>
    suspend fun insertAccount(account: AccountRecord): Long
    suspend fun updateAccount(account: AccountRecord)
}
