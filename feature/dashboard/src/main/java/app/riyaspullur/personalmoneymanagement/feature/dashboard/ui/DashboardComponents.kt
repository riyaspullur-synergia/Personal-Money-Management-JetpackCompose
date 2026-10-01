package app.riyaspullur.personalmoneymanagement.feature.dashboard.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetStatus
import app.riyaspullur.personalmoneymanagement.core.domain.model.DashboardSummary
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.color
import app.riyaspullur.personalmoneymanagement.core.ui.components.HorizontalBarChart
import app.riyaspullur.personalmoneymanagement.core.ui.components.HorizontalBarData
import app.riyaspullur.personalmoneymanagement.core.ui.components.MoneyText
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel
import app.riyaspullur.personalmoneymanagement.core.ui.icon
import app.riyaspullur.personalmoneymanagement.ui.theme.finance

@Composable
fun BalanceCard(summary: DashboardSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = stringResource(R.string.dashboard_net_worth), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(4.dp))
            MoneyText(money = summary.netWorth, style = MaterialTheme.typography.displaySmall)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = stringResource(R.string.dashboard_available_now), style = MaterialTheme.typography.labelMedium)
                    MoneyText(money = summary.availableMoney, style = MaterialTheme.typography.titleMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.dashboard_total_balance), style = MaterialTheme.typography.labelMedium)
                    MoneyText(money = summary.totalBalance, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun AssetBreakdownCard(summary: DashboardSummary) {
    val palette = MaterialTheme.finance.chartPalette
    val data = listOf(
        HorizontalBarData(stringResource(R.string.dashboard_cash_in_hand), summary.cashBalance.amount.toFloat(), summary.cashBalance, palette[0]),
        HorizontalBarData(stringResource(R.string.dashboard_bank_accounts), summary.bankBalance.amount.toFloat(), summary.bankBalance, palette[1]),
        HorizontalBarData(stringResource(R.string.dashboard_investments), summary.investmentBalance.amount.toFloat(), summary.investmentBalance, palette[2]),
        HorizontalBarData(stringResource(R.string.dashboard_deposits), summary.depositBalance.amount.toFloat(), summary.depositBalance, palette[3]),
        HorizontalBarData(stringResource(R.string.dashboard_receivables), summary.receivableBalance.amount.toFloat(), summary.receivableBalance, palette[4])
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.dashboard_distribution_title), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalBarChart(data = data)
        }
    }
}

@Composable
fun BudgetCard(status: BudgetStatus) {
    val progress = status.progress
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = stringResource(R.string.dashboard_budget_title, status.budget.name), style = MaterialTheme.typography.titleMedium)
                MoneyText(money = status.budget.totalLimit, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(MaterialTheme.shapes.medium),
                color = if (progress > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = stringResource(R.string.dashboard_spent_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    MoneyText(money = status.spent, style = MaterialTheme.typography.bodyMedium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(R.string.dashboard_remaining_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    MoneyText(money = status.remaining, style = MaterialTheme.typography.bodyMedium, color = if (status.remaining.amount < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(R.string.dashboard_percent_used, (progress * 100).toInt()), style = MaterialTheme.typography.bodySmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text(text = pluralStringResource(R.plurals.dashboard_days_remaining, status.daysRemaining, status.daysRemaining), style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(
                        text = stringResource(R.string.dashboard_recommended_daily_spend_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    MoneyText(
                        money = status.dailyAllowance,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Money,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = color)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MoneyText(money = amount, style = MaterialTheme.typography.titleLarge, color = color)
        }
    }
}

@Composable
fun SavingsGoalCard(goal: SavingsGoal) {
    val progress = if (goal.targetAmount > 0) goal.currentAmount.toFloat() / goal.targetAmount else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = goal.name, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.finance.savingsGoal,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MoneyText(money = Money(goal.currentAmount, goal.currency), style = MaterialTheme.typography.labelSmall)
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                MoneyText(money = Money(goal.targetAmount, goal.currency), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction) {
    val dateFormat = remember { java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val amountColor = tx.type.color

        Surface(
            modifier = Modifier.size(40.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = amountColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = tx.type.icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = amountColor
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.description ?: tx.type.displayLabel(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = dateFormat.format(java.util.Date(tx.transactionDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        val prefix = if (tx.type == TransactionType.INCOME) "+" else "-"

        MoneyText(
            money = Money(tx.amount, tx.currency),
            prefix = prefix,
            style = MaterialTheme.typography.titleMedium,
            color = amountColor
        )
    }
}

@Composable
fun DashboardSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(actionLabel)
            }
        }
    }
}
