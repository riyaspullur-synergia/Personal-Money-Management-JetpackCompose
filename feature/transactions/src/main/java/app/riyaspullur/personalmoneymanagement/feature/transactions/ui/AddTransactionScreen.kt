package app.riyaspullur.personalmoneymanagement.feature.transactions.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.color
import app.riyaspullur.personalmoneymanagement.core.ui.components.LoadingScreen
import app.riyaspullur.personalmoneymanagement.core.ui.components.MoneyText
import app.riyaspullur.personalmoneymanagement.core.ui.components.SectionCard
import app.riyaspullur.personalmoneymanagement.core.ui.displayLabel
import app.riyaspullur.personalmoneymanagement.core.ui.icon
import app.riyaspullur.personalmoneymanagement.core.util.Calculator
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import java.math.BigDecimal

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AddTransactionViewModel keeps fixed English error literals (unchanged, for backward
 * compatibility with existing ViewModel tests). This maps each known literal to a localized
 * string at the UI boundary.
 */
@Composable
private fun localizedAddTransactionError(message: String): String = when (message) {
    "Amount must be greater than zero" -> stringResource(R.string.error_amount_must_be_positive)
    "Description too long" -> stringResource(R.string.error_description_too_long)
    "Invalid account selected" -> stringResource(R.string.error_invalid_account)
    "Failed to save transaction" -> stringResource(R.string.error_failed_to_save_transaction)
    "Unknown error" -> stringResource(R.string.common_unknown_error)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    accounts: List<AccountRecord>,
    categories: List<Category>,
    uiState: AddTransactionUiState,
    onAddClick: (Long, TransactionType, Long?, Long, Long?, String?, String, String?, Long) -> Unit,
    onAddCategoryClick: (String, TransactionType) -> Unit,
    onBackClick: () -> Unit
) {
    var amountExpr by remember { mutableStateOf("") }
    val calculatedAmount = remember(amountExpr) { Calculator.evaluate(amountExpr) }
    var description by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var toAccountId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var isCleared by remember { mutableStateOf(true) }
    var selectedReceiptUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)

    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePickerDialog = false
                }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val scrollState = rememberScrollState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedReceiptUri = uri }
    )

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text(stringResource(R.string.category_add_title)) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.category_name_label)) },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategoryClick(newCategoryName, type)
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.common_add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    LaunchedEffect(accounts) {
        if (selectedAccountId == null && accounts.isNotEmpty()) {
            selectedAccountId = accounts.first().id
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.add_transaction_title)) })
        }
    ) { padding ->
        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.common_no_accounts_found))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text(stringResource(R.string.common_go_back))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (uiState is AddTransactionUiState.Loading) {
                    LoadingScreen(message = stringResource(R.string.add_transaction_saving))
                }

                if (uiState is AddTransactionUiState.Error) {
                    Text(
                        text = localizedAddTransactionError(uiState.message),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Logical labels for account selection
                val showToAccount =
                    type == TransactionType.TRANSFER || type == TransactionType.INVESTMENT || type == TransactionType.DEPOSIT

                val sourceLabel = when (type) {
                    TransactionType.INCOME -> stringResource(R.string.transaction_label_deposit_to)
                    TransactionType.EXPENSE -> stringResource(R.string.transaction_label_pay_from)
                    TransactionType.TRANSFER -> stringResource(R.string.transaction_label_from_account_source)
                    TransactionType.INVESTMENT -> stringResource(R.string.transaction_label_from_account_investing)
                    TransactionType.DEPOSIT -> stringResource(R.string.transaction_label_from_account_funding)
                    else -> stringResource(R.string.common_account)
                }

                SectionCard(title = stringResource(R.string.common_amount)) {
                    val heroCurrency =
                        accounts.find { it.id == selectedAccountId }?.currency ?: Currency.AED
                    val heroAmount = Money(
                        (calculatedAmount ?: BigDecimal.ZERO).multiply(BigDecimal(100)).toLong(),
                        heroCurrency
                    )

                    val isExpense = type == TransactionType.EXPENSE
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isExpense) Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(type.color.copy(alpha = 0.08f))
                                    .padding(vertical = 12.dp)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MoneyText(
                                money = heroAmount,
                                style = MaterialTheme.typography.displaySmall,
                                color = type.color
                            )
                            if (isExpense) {
                                Text(
                                    text = "Outgoing Expense Control Active",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = type.color,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountExpr,
                        onValueChange = { amountExpr = it },
                        label = { Text(stringResource(R.string.add_transaction_amount_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = {
                            if (calculatedAmount != null && amountExpr.contains(Regex("[+\\-*/]"))) {
                                Text(
                                    text = stringResource(R.string.add_account_total_label, calculatedAmount.toString()),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        TransactionType.entries.forEach { t ->
                            val selected = type == t
                            FilterChip(
                                selected = selected,
                                onClick = { type = t },
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

                SectionCard(title = stringResource(R.string.accounts)) {
                    Text(sourceLabel, style = MaterialTheme.typography.labelMedium)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        accounts.forEach { acc ->
                            val accColor = Color(acc.color)
                            FilterChip(
                                selected = selectedAccountId == acc.id,
                                onClick = { selectedAccountId = acc.id },
                                label = { Text(acc.name) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getAccountIcon(acc.type),
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

                    if (showToAccount) {
                        val destinationLabel = when (type) {
                            TransactionType.INVESTMENT -> stringResource(R.string.transaction_label_to_investment_account)
                            TransactionType.DEPOSIT -> stringResource(R.string.transaction_label_to_deposit_account)
                            else -> stringResource(R.string.transaction_label_to_account_destination)
                        }
                        Text(destinationLabel, style = MaterialTheme.typography.labelMedium)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                                val accColor = Color(acc.color)
                                FilterChip(
                                    selected = toAccountId == acc.id,
                                    onClick = { toAccountId = acc.id },
                                    label = { Text(acc.name) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getAccountIcon(acc.type),
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
                }

                val filteredCategories = categories.filter { it.type == type }
                if (type == TransactionType.INCOME || type == TransactionType.EXPENSE) {
                    SectionCard(title = stringResource(R.string.common_category)) {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            filteredCategories.forEach { cat ->
                                val catColor = Color(cat.color)
                                FilterChip(
                                    selected = selectedCategoryId == cat.id,
                                    onClick = { selectedCategoryId = cat.id },
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
                                onClick = { showAddCategoryDialog = true },
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
                }

                SectionCard(title = stringResource(R.string.add_account_details_section_title)) {
                    OutlinedButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ShowChart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Date: ${dateFormatter.format(Date(selectedDateMillis))}")
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(R.string.common_description)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedReceiptUri != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.add_transaction_receipt_attached),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(onClick = { selectedReceiptUri = null }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.common_remove))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                launcher.launch(
                                    arrayOf(
                                        "image/*",
                                        "application/pdf",
                                        "application/msword",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.add_transaction_attach_receipt))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isCleared, onCheckedChange = { isCleared = it })
                        Text(stringResource(R.string.add_transaction_payment_cleared), style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Button(
                    onClick = {
                        val amt =
                            (calculatedAmount?.multiply(BigDecimal(100))?.toLong()) ?: 0L
                        if (selectedAccountId != null) {
                            onAddClick(
                                amt,
                                type,
                                selectedCategoryId,
                                selectedAccountId!!,
                                toAccountId,
                                description,
                                if (isCleared) "CLEARED" else "PENDING",
                                selectedReceiptUri?.toString(),
                                selectedDateMillis
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = type.color),
                    enabled = selectedAccountId != null && amountExpr.isNotBlank() && (!showToAccount || toAccountId != null)
                ) {
                    Text(
                        stringResource(R.string.add_transaction_save_action, type.displayLabel()),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
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
fun AddTransactionScreenPreview() {
    val mockAccounts = listOf(
        AccountRecord(
            id = 1,
            userId = 1,
            name = "Cash",
            type = AccountType.CASH,
            initialBalance = 100000,
            currency = Currency.AED,
            icon = null,
            color = 0xFF4CAF50.toInt()
        ),
        AccountRecord(
            id = 2,
            userId = 1,
            name = "Bank",
            type = AccountType.BANK,
            initialBalance = 500000,
            currency = Currency.AED,
            icon = null,
            color = 0xFF2196F3.toInt()
        )
    )
    val mockCategories = listOf(
        Category(
            id = 1,
            userId = 1,
            name = "Food",
            icon = null,
            color = 0xFFF44336.toInt(),
            type = TransactionType.EXPENSE
        ),
        Category(
            id = 2,
            userId = 1,
            name = "Salary",
            icon = null,
            color = 0xFF4CAF50.toInt(),
            type = TransactionType.INCOME
        )
    )

    PersonalMoneyManagemntTheme {
        AddTransactionScreen(
            accounts = mockAccounts,
            categories = mockCategories,
            uiState = AddTransactionUiState.Idle,
            onAddClick = { _, _, _, _, _, _, _, _, _ -> },
            onAddCategoryClick = { _, _ -> },
            onBackClick = {}
        )
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun AddTransactionScreenLoadingPreview() {
    val mockAccounts = listOf(
        AccountRecord(
            id = 1,
            userId = 1,
            name = "Cash",
            type = AccountType.CASH,
            initialBalance = 100000,
            currency = Currency.AED,
            icon = null,
            color = 0xFF4CAF50.toInt()
        )
    )
    val mockCategories = listOf(
        Category(
            id = 1,
            userId = 1,
            name = "Food",
            icon = null,
            color = 0xFFF44336.toInt(),
            type = TransactionType.EXPENSE
        )
    )

    PersonalMoneyManagemntTheme {
        AddTransactionScreen(
            accounts = mockAccounts,
            categories = mockCategories,
            uiState = AddTransactionUiState.Loading,
            onAddClick = { _, _, _, _, _, _, _, _, _ -> },
            onAddCategoryClick = { _, _ -> },
            onBackClick = {}
        )
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun AddTransactionScreenErrorPreview() {
    val mockAccounts = listOf(
        AccountRecord(
            id = 1,
            userId = 1,
            name = "Cash",
            type = AccountType.CASH,
            initialBalance = 100000,
            currency = Currency.AED,
            icon = null,
            color = 0xFF4CAF50.toInt()
        )
    )
    val mockCategories = listOf(
        Category(
            id = 1,
            userId = 1,
            name = "Food",
            icon = null,
            color = 0xFFF44336.toInt(),
            type = TransactionType.EXPENSE
        )
    )

    PersonalMoneyManagemntTheme {
        AddTransactionScreen(
            accounts = mockAccounts,
            categories = mockCategories,
            uiState = AddTransactionUiState.Error("Failed to save transaction"),
            onAddClick = { _, _, _, _, _, _, _, _, _ -> },
            onAddCategoryClick = { _, _ -> },
            onBackClick = {}
        )
    }
}

@FontScalePreviews
@ThemePreviews
@Composable
fun AddTransactionScreenNoAccountsPreview() {
    PersonalMoneyManagemntTheme {
        AddTransactionScreen(
            accounts = emptyList(),
            categories = emptyList(),
            uiState = AddTransactionUiState.Idle,
            onAddClick = { _, _, _, _, _, _, _, _, _ -> },
            onAddCategoryClick = { _, _ -> },
            onBackClick = {}
        )
    }
}
