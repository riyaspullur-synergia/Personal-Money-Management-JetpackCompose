package app.riyaspullur.personalmoneymanagement.core.domain.model

data class InvestmentDetails(
    val investedAmount: Money,
    val currentValue: Money,
    val profitLoss: Money = currentValue - investedAmount,
    val lastValuationDate: Long = System.currentTimeMillis()
)

data class DepositDetails(
    val principalAmount: Money,
    val interestRate: Double,
    val startDate: Long,
    val maturityDate: Long?,
    val bankName: String?
)

data class DebtDetails(
    val personName: String,
    val totalAmount: Money,
    val remainingAmount: Money,
    val dueDate: Long?,
    val isReceivable: Boolean // true for money lent, false for liability
)
