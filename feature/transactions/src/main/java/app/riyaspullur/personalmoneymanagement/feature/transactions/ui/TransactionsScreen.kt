package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
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
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.EmptyState
import app.riyaspullur.personalmoneymanagement.core.ui.components.LoadingScreen
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

/**
 * The ViewModel keeps a fixed English fallback literal ("Unknown error", unchanged, to stay
 * backward compatible with existing ViewModel tests). This maps it to a localized string at
 * the UI boundary; any other message (a real exception's text) is shown as-is since it isn't
 * designed, translatable UI copy.
 */
@Composable
private fun localizedTransactionsError(message: String): String = when (message) {
    "Unknown error" -> stringResource(R.string.common_unknown_error)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    uiState: TransactionsUiState,
    onBackClick: () -> Unit,
    onDeleteClick: (Transaction) -> Unit,
    onDateRangeSelect: (Long?, Long?) -> Unit
) {
    var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateRangeSelect(
                        datePickerState.selectedStartDateMillis,
                        datePickerState.selectedEndDateMillis
                    )
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    onDateRangeSelect(null, null)
                    showDatePicker = false 
                }) {
                    Text(stringResource(R.string.common_clear_filter))
                }
            }
        ) {
            DateRangePicker(state = datePickerState, modifier = Modifier.weight(1f))
        }
    }

    if (transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text(stringResource(R.string.transactions_delete_title)) },
            text = { Text(stringResource(R.string.transactions_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToDelete?.let { onDeleteClick(it) }
                        transactionToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.transactions_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.transactions_filter_by_date))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (datePickerState.selectedStartDateMillis != null && datePickerState.selectedEndDateMillis != null) {
                val dateFormat = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }
                val start = dateFormat.format(java.util.Date(datePickerState.selectedStartDateMillis!!))
                val end = dateFormat.format(java.util.Date(datePickerState.selectedEndDateMillis!!))
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.transactions_filtering_range, start, end),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        TextButton(
                            onClick = { 
                                onDateRangeSelect(null, null)
                                datePickerState.setSelection(null, null)
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(stringResource(R.string.common_clear), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            when (uiState) {
                is TransactionsUiState.Loading -> {
                    LoadingScreen()
                }
                is TransactionsUiState.Success -> {
                    if (uiState.transactions.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Receipt,
                            title = stringResource(R.string.common_no_transactions),
                            subtitle = stringResource(R.string.transactions_empty_subtitle)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.transactions) { tx ->
                                TransactionCard(tx, onDeleteClick = { transactionToDelete = it })
                            }
                        }
                    }
                }
                is TransactionsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = localizedTransactionsError(uiState.message), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun TransactionsScreenPreview() {
    val mockTransactions = listOf(
        Transaction(
            id = 1,
            userId = 1,
            accountId = 1,
            amount = 15000L,
            currency = Currency.AED,
            categoryId = 1,
            type = TransactionType.EXPENSE,
            merchant = "Starbucks",
            description = "Coffee",
            notes = null,
            transactionDate = System.currentTimeMillis()
        )
    )

    PersonalMoneyManagemntTheme {
        TransactionsScreen(
            uiState = TransactionsUiState.Success(mockTransactions),
            onBackClick = {},
            onDeleteClick = {},
            onDateRangeSelect = { _, _ -> }
        )
    }
}
