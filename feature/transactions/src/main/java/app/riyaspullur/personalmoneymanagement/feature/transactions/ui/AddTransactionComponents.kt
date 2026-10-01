package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.color
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel
import app.riyaspullur.personalmoneymanagement.core.ui.icon

@Composable
fun TransactionTypePicker(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        TransactionType.entries.forEach { t ->
            val selected = selectedType == t
            FilterChip(
                selected = selected,
                onClick = { onTypeSelected(t) },
                label = { Text(t.displayLabel()) },
                leadingIcon = {
                    Icon(
                        imageVector = t.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = t.color.copy(alpha = 0.15f),
                    selectedLabelColor = t.color,
                    selectedLeadingIconColor = t.color
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun AccountPicker(
    accounts: List<AccountRecord>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        accounts.forEach { acc ->
            val accColor = Color(acc.color)
            FilterChip(
                selected = selectedAccountId == acc.id,
                onClick = { onAccountSelected(acc.id) },
                label = { Text(acc.name) },
                leadingIcon = {
                    Icon(
                        imageVector = accountTypeIcon(acc.type),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = accColor
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = accColor.copy(alpha = 0.15f),
                    selectedLabelColor = accColor,
                    selectedLeadingIconColor = accColor
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    onAddCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        categories.forEach { cat ->
            val catColor = Color(cat.color)
            FilterChip(
                selected = selectedCategoryId == cat.id,
                onClick = { onCategorySelected(cat.id) },
                label = { Text(cat.displayLabel()) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(catColor)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = catColor.copy(alpha = 0.15f),
                    selectedLabelColor = catColor
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        InputChip(
            selected = false,
            onClick = onAddCategoryClick,
            label = { Text(stringResource(R.string.common_new)) },
            leadingIcon = {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

fun accountTypeIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CASH -> Icons.Default.Payments
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
    AccountType.DEBIT_CARD -> Icons.Default.CreditCard
    AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
    AccountType.SAVINGS -> Icons.Default.Savings
    AccountType.INVESTMENT -> Icons.AutoMirrored.Filled.ShowChart
    AccountType.FIXED_DEPOSIT -> Icons.Default.Savings
    AccountType.RECURRING_DEPOSIT -> Icons.Default.Savings
    AccountType.GOLD -> Icons.Default.Savings
    AccountType.ASSET -> Icons.Default.Category
    AccountType.RECEIVABLE -> Icons.Default.Payments
    AccountType.LIABILITY -> Icons.Default.CreditCard
    AccountType.CUSTOM -> Icons.Default.Category
}
