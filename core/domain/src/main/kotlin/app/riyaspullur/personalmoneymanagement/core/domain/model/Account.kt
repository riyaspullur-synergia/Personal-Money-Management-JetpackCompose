package app.riyaspullur.personalmoneymanagement.core.domain.model

data class Account(
    val id: Long,
    val userId: Long,
    val groupId: Long?,
    val name: String,
    val type: AccountType,
    val currency: Currency,
    val color: Int,
    val currentBalance: Money,
    val investmentDetails: InvestmentDetails? = null,
    val depositDetails: DepositDetails? = null,
    val debtDetails: DebtDetails? = null
)
