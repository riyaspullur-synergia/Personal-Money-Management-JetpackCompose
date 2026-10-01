package app.riyaspullur.personalmoneymanagement.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.ui.theme.finance

// Shared display mapping for TransactionType so the dashboard's transaction list and the
// add-transaction type picker never drift apart (icon/label/color defined once).

@Composable
fun TransactionType.displayLabel(): String = when (this) {
    TransactionType.EXPENSE -> stringResource(R.string.common_expense)
    TransactionType.INCOME -> stringResource(R.string.common_income)
    TransactionType.TRANSFER -> stringResource(R.string.common_transfer)
    TransactionType.INVESTMENT -> stringResource(R.string.common_investment)
    TransactionType.DEPOSIT -> stringResource(R.string.common_deposit)
}

val TransactionType.icon: ImageVector
    get() = when (this) {
        TransactionType.EXPENSE -> Icons.AutoMirrored.Filled.TrendingDown
        TransactionType.INCOME -> Icons.AutoMirrored.Filled.TrendingUp
        TransactionType.TRANSFER -> Icons.Default.SwapHoriz
        TransactionType.INVESTMENT -> Icons.Default.ShowChart
        TransactionType.DEPOSIT -> Icons.Default.Savings
    }

val TransactionType.color: Color
    @Composable get() = when (this) {
        TransactionType.EXPENSE -> MaterialTheme.finance.expense
        TransactionType.INCOME -> MaterialTheme.finance.income
        TransactionType.TRANSFER -> MaterialTheme.finance.transfer
        TransactionType.INVESTMENT -> MaterialTheme.finance.chartPalette[5]
        TransactionType.DEPOSIT -> MaterialTheme.finance.savingsGoal
    }