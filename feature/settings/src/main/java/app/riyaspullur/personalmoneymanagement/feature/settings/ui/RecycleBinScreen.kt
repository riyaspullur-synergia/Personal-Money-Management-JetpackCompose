package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.EmptyState
import app.riyaspullur.personalmoneymanagement.core.ui.components.MoneyText
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    deletedTransactions: List<Transaction>,
    onRestoreClick: (Transaction) -> Unit,
    onClearAllClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.recycle_bin_clear_dialog_title)) },
            text = { Text(stringResource(R.string.recycle_bin_clear_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllClick()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.recycle_bin_clear_all), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_recycle_bin)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (deletedTransactions.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.recycle_bin_clear_all))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (deletedTransactions.isEmpty()) {
            EmptyState(
                icon = Icons.Default.DeleteSweep,
                title = stringResource(R.string.recycle_bin_empty_title),
                subtitle = stringResource(R.string.recycle_bin_empty_subtitle),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(deletedTransactions) { tx ->
                    DeletedTransactionItem(tx, onRestoreClick)
                }
            }
        }
    }
}

@Composable
private fun DeletedTransactionItem(
    tx: Transaction,
    onRestoreClick: (Transaction) -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tx.description ?: tx.type.displayLabel(), style = MaterialTheme.typography.bodyMedium)
                MoneyText(money = Money(tx.amount, tx.currency), style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = { onRestoreClick(tx) }) {
                Icon(Icons.Default.Restore, contentDescription = stringResource(R.string.recycle_bin_restore_content_description), tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun RecycleBinScreenPreview() {
    val mockTransactions = listOf(
        Transaction(
            id = 1,
            userId = 1,
            accountId = 1,
            amount = 5000L,
            currency = Currency.AED,
            categoryId = 1,
            type = TransactionType.EXPENSE,
            merchant = null,
            description = "Coffee",
            notes = null,
            transactionDate = System.currentTimeMillis()
        )
    )
    PersonalMoneyManagemntTheme {
        RecycleBinScreen(
            deletedTransactions = mockTransactions,
            onRestoreClick = {},
            onClearAllClick = {},
            onBackClick = {}
        )
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun RecycleBinScreenEmptyPreview() {
    PersonalMoneyManagemntTheme {
        RecycleBinScreen(
            deletedTransactions = emptyList(),
            onRestoreClick = {},
            onClearAllClick = {},
            onBackClick = {}
        )
    }
}
