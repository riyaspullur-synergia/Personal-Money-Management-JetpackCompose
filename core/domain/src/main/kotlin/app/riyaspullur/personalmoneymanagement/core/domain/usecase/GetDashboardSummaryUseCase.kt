package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.DashboardSummary
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AccountRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val calculateBudgetStatusUseCase: CalculateBudgetStatusUseCase
) {
    operator fun invoke(userId: Long, cycleStartDay: Int = 1): Flow<DashboardSummary> {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (currentDay < cycleStartDay) {
            calendar.add(Calendar.MONTH, -1)
        }
        calendar.set(Calendar.DAY_OF_MONTH, cycleStartDay)
        val startOfCycle = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        val endOfCycle = calendar.timeInMillis 

        return combine(
            transactionRepository.getTotalAmountByType(userId, TransactionType.INCOME, startOfCycle, endOfCycle),
            transactionRepository.getTotalAmountByType(userId, TransactionType.EXPENSE, startOfCycle, endOfCycle),
            calculateBudgetStatusUseCase(userId),
            accountRepository.getActiveAccountsWithBalances(userId)
        ) { income, expense, budget, accounts ->
            
            var availableMoney = 0L
            var netWorth = 0L
            var cash = 0L
            var bank = 0L
            var investment = 0L
            var deposit = 0L
            var receivable = 0L

            accounts.forEach { account ->
                val amount = account.currentBalance.amount
                
                when (account.type) {
                    AccountType.CASH -> {
                        cash += amount
                        availableMoney += amount
                    }
                    AccountType.BANK, AccountType.DIGITAL_WALLET, AccountType.DEBIT_CARD -> {
                        bank += amount
                        availableMoney += amount
                    }
                    AccountType.SAVINGS -> {
                        bank += amount
                        availableMoney += amount
                    }
                    AccountType.INVESTMENT, AccountType.GOLD -> {
                        investment += amount
                    }
                    AccountType.FIXED_DEPOSIT, AccountType.RECURRING_DEPOSIT -> {
                        deposit += amount
                    }
                    AccountType.RECEIVABLE -> {
                        receivable += amount
                    }
                    else -> {}
                }
                
                netWorth += amount
            }
            
            DashboardSummary(
                totalBalance = Money(netWorth),
                availableMoney = Money(availableMoney),
                netWorth = Money(netWorth),
                cashBalance = Money(cash),
                bankBalance = Money(bank),
                investmentBalance = Money(investment),
                depositBalance = Money(deposit),
                receivableBalance = Money(receivable),
                totalIncome = Money(income),
                totalExpense = Money(expense),
                netCashFlow = Money(income - expense),
                budgetStatus = budget
            )
        }
    }
}
