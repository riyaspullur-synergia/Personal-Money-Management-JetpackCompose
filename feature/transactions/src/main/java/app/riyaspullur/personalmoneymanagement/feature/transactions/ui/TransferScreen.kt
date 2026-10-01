package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.SectionCard
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    accounts: List<AccountRecord>,
    onTransferClick: (Long, Long, Long, Currency, String?) -> Unit,
    onBackClick: () -> Unit
) {
    var fromAccountId by remember { mutableStateOf<Long?>(null) }
    var toAccountId by remember { mutableStateOf<Long?>(null) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.transfer_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard(title = stringResource(R.string.accounts)) {
                Text(stringResource(R.string.transaction_label_from_account_source), style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = fromAccountId == acc.id,
                            onClick = { fromAccountId = acc.id },
                            label = { Text(acc.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getAccountIcon(acc.type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Text(stringResource(R.string.transaction_label_to_account_destination), style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    accounts.filter { it.id != fromAccountId }.forEach { acc ->
                        FilterChip(
                            selected = toAccountId == acc.id,
                            onClick = { toAccountId = acc.id },
                            label = { Text(acc.name) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getAccountIcon(acc.type),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            SectionCard(title = stringResource(R.string.add_account_details_section_title)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.common_amount)) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.transfer_note_label)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (fromAccountId != null && toAccountId != null) {
                        val amt = amount.toLongOrNull() ?: 0L
                        // Use currency of the source account
                        val currency = accounts.find { it.id == fromAccountId }?.currency ?: Currency.AED
                        onTransferClick(fromAccountId!!, toAccountId!!, amt, currency, note)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = fromAccountId != null && toAccountId != null && amount.isNotBlank()
            ) {
                Text(stringResource(R.string.transfer_execute_button))
            }
        }
    }
}

private fun getAccountIcon(type: AccountType): ImageVector = when (type) {
    AccountType.CASH -> Icons.Default.Payments
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.DIGITAL_WALLET -> Icons.Default.Wallet
    AccountType.SAVINGS, AccountType.FIXED_DEPOSIT, AccountType.RECURRING_DEPOSIT -> Icons.Default.Savings
    AccountType.INVESTMENT, AccountType.GOLD -> Icons.Default.ShowChart
    else -> Icons.Default.AccountBalance
}

@FontScalePreviews
@ThemePreviews
@Composable
fun TransferScreenPreview() {
    val mockAccounts = listOf(
        AccountRecord(id = 1, userId = 1, name = "Cash", type = AccountType.CASH, initialBalance = 100000, currency = Currency.AED, icon = null, color = 0xFF4CAF50.toInt()),
        AccountRecord(id = 2, userId = 1, name = "Bank", type = AccountType.BANK, initialBalance = 500000, currency = Currency.AED, icon = null, color = 0xFF2196F3.toInt())
    )
    PersonalMoneyManagemntTheme {
        TransferScreen(
            accounts = mockAccounts,
            onTransferClick = { _, _, _, _, _ -> },
            onBackClick = {}
        )
    }
}
