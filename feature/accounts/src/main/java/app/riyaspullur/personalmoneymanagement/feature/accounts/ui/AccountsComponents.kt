package app.riyaspullur.personalmoneymanagement.feature.accounts.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.Account
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.ui.components.MoneyText
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel

@Composable
fun AccountItem(account: Account) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val accountColor = Color(account.color)
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = accountColor.copy(alpha = 0.16f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = accountTypeIcon(account.type),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = accountColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.name, style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.type.displayLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Extra Details Breakdown
                    account.investmentDetails?.let {
                        val profit = it.currentValue.amount - it.investedAmount.amount
                        val color = if (profit >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        Text(text = " • ", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = if (profit >= 0) "+${Money(profit, it.currentValue.currency).format()}" else Money(profit, it.currentValue.currency).format(),
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }

                    account.depositDetails?.let {
                        Text(text = " • ", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "${it.interestRate}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    account.debtDetails?.let {
                        Text(text = " • ", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = it.personName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            MoneyText(
                money = account.currentBalance,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

fun accountTypeIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CASH -> Icons.Default.Payments
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
    AccountType.DEBIT_CARD -> Icons.Default.CreditCard
    AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
    AccountType.SAVINGS -> Icons.Default.Savings
    AccountType.INVESTMENT -> Icons.Default.ShowChart
    AccountType.FIXED_DEPOSIT -> Icons.Default.Savings
    AccountType.RECURRING_DEPOSIT -> Icons.Default.Savings
    AccountType.GOLD -> Icons.Default.Savings
    AccountType.ASSET -> Icons.Default.Category
    AccountType.RECEIVABLE -> Icons.Default.Payments
    AccountType.LIABILITY -> Icons.Default.CreditCard
    AccountType.CUSTOM -> Icons.Default.Category
}
