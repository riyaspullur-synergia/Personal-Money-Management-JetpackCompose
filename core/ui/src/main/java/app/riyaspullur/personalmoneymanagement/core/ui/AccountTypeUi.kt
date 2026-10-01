package app.riyaspullur.personalmoneymanagement.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType

@Composable
fun AccountType.displayLabel(): String = when (this) {
    AccountType.CASH -> stringResource(R.string.account_type_cash)
    AccountType.BANK -> stringResource(R.string.account_type_bank)
    AccountType.CREDIT_CARD -> stringResource(R.string.account_type_credit_card)
    AccountType.DEBIT_CARD -> stringResource(R.string.account_type_debit_card)
    AccountType.DIGITAL_WALLET -> stringResource(R.string.account_type_digital_wallet)
    AccountType.SAVINGS -> stringResource(R.string.account_type_savings)
    AccountType.INVESTMENT -> stringResource(R.string.account_type_investment)
    AccountType.FIXED_DEPOSIT -> stringResource(R.string.account_type_fixed_deposit)
    AccountType.RECURRING_DEPOSIT -> stringResource(R.string.account_type_recurring_deposit)
    AccountType.GOLD -> stringResource(R.string.account_type_gold)
    AccountType.ASSET -> stringResource(R.string.account_type_asset)
    AccountType.RECEIVABLE -> stringResource(R.string.account_type_receivable)
    AccountType.LIABILITY -> stringResource(R.string.account_type_liability)
    AccountType.CUSTOM -> stringResource(R.string.account_type_custom)
}
