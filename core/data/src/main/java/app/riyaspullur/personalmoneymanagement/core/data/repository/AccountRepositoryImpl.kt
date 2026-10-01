package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toEntity
import app.riyaspullur.personalmoneymanagement.core.database.dao.AccountDao
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.DebtDetails
import app.riyaspullur.personalmoneymanagement.core.domain.model.DepositDetails
import app.riyaspullur.personalmoneymanagement.core.domain.model.InvestmentDetails
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountDao: AccountDao
) : AccountRepository {
    override fun getActiveAccounts(userId: Long): Flow<List<AccountRecord>> =
        accountDao.getActiveAccounts(userId).map { entities -> entities.map { it.toDomain() } }

    override fun getActiveAccountsWithBalances(userId: Long): Flow<List<Account>> {
        return accountDao.getActiveAccounts(userId).flatMapLatest { entities ->
            val accountFlows = entities.map { entity ->
                accountDao.getAccountTransactionBalance(entity.id).map { txBalance ->
                    val baseBalance = entity.initialBalance + txBalance

                    val investedAmount = entity.investedAmount
                    val investmentDetails = if (investedAmount != null) {
                        InvestmentDetails(
                            investedAmount = Money(investedAmount, entity.currency),
                            currentValue = Money(baseBalance, entity.currency),
                            lastValuationDate = entity.lastValuationDate ?: System.currentTimeMillis()
                        )
                    } else null

                    val interestRate = entity.interestRate
                    val depositDetails = if (interestRate != null) {
                        DepositDetails(
                            principalAmount = Money(entity.initialBalance, entity.currency),
                            interestRate = interestRate,
                            startDate = entity.createdAt,
                            maturityDate = entity.maturityDate,
                            bankName = entity.bankName
                        )
                    } else null

                    val personName = entity.personName
                    val debtDetails = if (personName != null) {
                        DebtDetails(
                            personName = personName,
                            totalAmount = Money(entity.initialBalance, entity.currency),
                            remainingAmount = Money(baseBalance, entity.currency),
                            dueDate = entity.dueDate,
                            isReceivable = entity.isReceivable ?: true
                        )
                    } else null

                    // Balance logic based on type
                    val currentBalance = when (entity.type) {
                        AccountType.INVESTMENT, AccountType.GOLD -> {
                            investmentDetails?.currentValue ?: Money(baseBalance, entity.currency)
                        }
                        AccountType.RECEIVABLE, AccountType.LIABILITY -> {
                            debtDetails?.remainingAmount ?: Money(baseBalance, entity.currency)
                        }
                        else -> Money(baseBalance, entity.currency)
                    }

                    Account(
                        id = entity.id,
                        userId = entity.userId,
                        groupId = entity.groupId,
                        name = entity.name,
                        type = entity.type,
                        currency = entity.currency,
                        color = entity.color,
                        currentBalance = currentBalance,
                        investmentDetails = investmentDetails,
                        depositDetails = depositDetails,
                        debtDetails = debtDetails
                    )
                }
            }
            if (accountFlows.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                combine(accountFlows) { it.toList() }
            }
        }
    }

    override suspend fun getAccountById(accountId: Long, userId: Long): AccountRecord? =
        accountDao.getAccountById(accountId, userId)?.toDomain()

    override suspend fun getAllAccountsForBackup(userId: Long): List<AccountRecord> =
        accountDao.getAllAccounts(userId).map { it.toDomain() }

    override suspend fun insertAccount(account: AccountRecord): Long =
        accountDao.insertAccount(account.toEntity())

    override suspend fun updateAccount(account: AccountRecord) =
        accountDao.updateAccount(account.toEntity())
}
