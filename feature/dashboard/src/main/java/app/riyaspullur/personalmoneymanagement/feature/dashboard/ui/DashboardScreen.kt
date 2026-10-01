package app.riyaspullur.personalmoneymanagement.feature.dashboard.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DashboardSummary
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.LoadingScreen
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import app.riyaspullur.personalmoneymanagement.ui.theme.finance
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * DashboardViewModel keeps a fixed English literal ("Unknown error", unchanged, for backward
 * compatibility with existing ViewModel tests) as its default error fallback. This maps that
 * one known literal to a localized string at the UI boundary; any other (real exception) text
 * is shown as-is since it isn't designed, translatable UI copy.
 */
@Composable
private fun localizedDashboardError(message: String): String = when (message) {
    "Unknown error" -> stringResource(R.string.common_unknown_error)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    recentTransactions: List<Transaction>,
    event: SharedFlow<DashboardEvent>,
    maxExpenseLimit: Long = 0L,
    expenseLimitStartDay: String = "1",
    onSetExpenseLimitClick: (Long, String) -> Unit = { _, _ -> },
    onAddExpenseClick: () -> Unit,
    onAddIncomeClick: () -> Unit,
    onTransferClick: () -> Unit,
    onViewAllClick: () -> Unit,
    onImportClick: (Uri) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showImportMenu by remember { mutableStateOf(false) }
    var showTemplatePreview by remember { mutableStateOf(false) }
    var showLimitConfigDialog by remember { mutableStateOf(false) }
    var limitInput by remember(maxExpenseLimit) { mutableStateOf(maxExpenseLimit.toString()) }
    var startDayInput by remember(expenseLimitStartDay) { mutableStateOf(expenseLimitStartDay) }
    
    var showCustomRangePicker by remember { mutableStateOf(false) }

    val dateInfo = remember(expenseLimitStartDay) {
        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val targetDay = expenseLimitStartDay.toIntOrNull() ?: 1
        
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        
        if (currentDay < targetDay) {
            cal.add(Calendar.MONTH, -1)
        }
        cal.set(Calendar.DAY_OF_MONTH, targetDay)
        val startD = cal.time
        
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.DAY_OF_MONTH, -1)
        val endD = cal.time
        
        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        Triple(df.format(startD), df.format(endD), "${SimpleDateFormat("dd MMM", Locale.getDefault()).format(startD)} - ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(endD)}")
    }

    var hasShownAlert by remember { mutableStateOf(false) }
    LaunchedEffect(maxExpenseLimit, uiState) {
        val totalExpense = (uiState as? DashboardUiState.Success)?.summary?.totalExpense?.amount ?: 0L
        if (maxExpenseLimit > 0L && totalExpense > maxExpenseLimit * 100L && !hasShownAlert) {
            snackbarHostState.showSnackbar("Warning: Exceeded maximum expense spend limit for ${dateInfo.third}!")
            hasShownAlert = true
        }
    }

    if (showCustomRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showCustomRangePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateRangePickerState.selectedStartDateMillis?.let { s ->
                        val calS = Calendar.getInstance().apply { timeInMillis = s }
                        startDayInput = calS.get(Calendar.DAY_OF_MONTH).toString()
                    }
                    showCustomRangePicker = false
                }) {
                    Text(stringResource(R.string.common_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomRangePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = { 
                    Text(
                        text = "Select Cycle Range", 
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                        style = MaterialTheme.typography.labelLarge
                    ) 
                },
                headline = { 
                    Text(
                        text = "Start day will be saved", 
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp),
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showLimitConfigDialog) {
        AlertDialog(
            onDismissRequest = { showLimitConfigDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Expense Control") 
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = limitInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) limitInput = it },
                        label = { Text("Monthly Spend Limit") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("AED ") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Card(
                        onClick = { showCustomRangePicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Current Billing Cycle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "From: ${dateInfo.first}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "To: ${dateInfo.second}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Change Cycle",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = "Your limit will reset on Day $startDayInput of every month. Tap the card above to change the start day.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val limit = limitInput.toLongOrNull() ?: 0L
                    val day = startDayInput.toIntOrNull()?.coerceIn(1, 31)?.toString() ?: "1"
                    onSetExpenseLimitClick(limit, day)
                    showLimitConfigDialog = false
                }) {
                    Text("Save Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitConfigDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { onImportClick(it) }
        }
    )

    if (showTemplatePreview) {
        AlertDialog(
            onDismissRequest = { showTemplatePreview = false },
            title = { Text(stringResource(R.string.reports_template_preview_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.reports_template_preview_headers),
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "Example:\n2026-08-20 10:00, EXPENSE, 100.0, AED, Cash, Food, Supermarket, Weekly food, CLEARED",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTemplatePreview = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }

    val importSuccessTemplate = stringResource(R.string.dashboard_import_success)

    LaunchedEffect(Unit) {
        event.collect { e ->
            when (e) {
                is DashboardEvent.ImportResult -> {
                    snackbarHostState.showSnackbar(
                        importSuccessTemplate.format(e.count)
                    )
                }
                is DashboardEvent.Error -> {
                    snackbarHostState.showSnackbar(e.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard)) },
                actions = {
                    IconButton(onClick = { showLimitConfigDialog = true }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Expense Control Info"
                        )
                    }
                    Box {
                        IconButton(onClick = { showImportMenu = true }) {
                            Icon(
                                Icons.Default.FileUpload,
                                contentDescription = stringResource(R.string.common_import)
                            )
                        }
                        DropdownMenu(
                            expanded = showImportMenu,
                            onDismissRequest = { showImportMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.common_import)) },
                                onClick = {
                                    showImportMenu = false
                                    launcher.launch(arrayOf("text/*", "application/octet-stream", "text/csv"))
                                },
                                leadingIcon = { Icon(Icons.Default.FileUpload, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reports_preview_template_menu_item)) },
                                onClick = {
                                    showImportMenu = false
                                    showTemplatePreview = true
                                },
                                leadingIcon = { Icon(Icons.Default.Info, null) }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(onClick = onAddIncomeClick, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = stringResource(R.string.dashboard_add_income_content_description),
                        tint = MaterialTheme.finance.income
                    )
                }
                SmallFloatingActionButton(onClick = onTransferClick, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = stringResource(R.string.common_transfer),
                        tint = MaterialTheme.finance.transfer
                    )
                }
                FloatingActionButton(onClick = onAddExpenseClick) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.dashboard_add_expense_content_description))
                }
            }
        }
    ) { padding ->
        when (uiState) {
            is DashboardUiState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    summary = uiState.summary,
                    savingsGoals = uiState.savingsGoals,
                    recentTransactions = recentTransactions,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    onAddIncomeClick = onAddIncomeClick,
                    onViewAllClick = onViewAllClick
                )
            }
            is DashboardUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = localizedDashboardError(uiState.message), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    summary: DashboardSummary,
    savingsGoals: List<SavingsGoal>,
    recentTransactions: List<Transaction>,
    onAddIncomeClick: () -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            BalanceCard(summary = summary)
        }

        item {
            AssetBreakdownCard(summary = summary)
        }

        summary.budgetStatus?.let { status ->
            item {
                BudgetCard(status = status)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SummaryCard(
                    title = stringResource(R.string.common_income),
                    amount = summary.totalIncome,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = MaterialTheme.finance.income,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = stringResource(R.string.dashboard_expenses_label),
                    amount = summary.totalExpense,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = MaterialTheme.finance.expense,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        if (savingsGoals.isNotEmpty()) {
            item {
                DashboardSectionHeader(title = stringResource(R.string.dashboard_savings_goals_title))
            }
            items(savingsGoals.size) { index ->
                val goal = savingsGoals[index]
                SavingsGoalCard(goal = goal)
            }
        }

        if (recentTransactions.isNotEmpty()) {
            item {
                DashboardSectionHeader(
                    title = stringResource(R.string.dashboard_recent_activity_title),
                    actionLabel = stringResource(R.string.dashboard_view_all),
                    onActionClick = onViewAllClick
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        recentTransactions.forEachIndexed { index, tx ->
                            TransactionItem(tx)
                            if (index < recentTransactions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun DashboardScreenPreview() {
    val mockSummary = DashboardSummary(
        totalBalance = Money(5000000L, Currency.AED),
        availableMoney = Money(3500000L, Currency.AED),
        netWorth = Money(15000000L, Currency.AED),
        cashBalance = Money(500000L, Currency.AED),
        bankBalance = Money(3000000L, Currency.AED),
        investmentBalance = Money(10000000L, Currency.AED),
        depositBalance = Money(2000000L, Currency.AED),
        receivableBalance = Money(100000L, Currency.AED),
        totalIncome = Money(2500000L, Currency.AED),
        totalExpense = Money(1500000L, Currency.AED),
        netCashFlow = Money(1000000L, Currency.AED),
        budgetStatus = null
    )

    val mockTransactions = emptyList<Transaction>()
    val mockGoals = emptyList<SavingsGoal>()

    PersonalMoneyManagemntTheme {
        DashboardScreen(
            uiState = DashboardUiState.Success(
                summary = mockSummary,
                savingsGoals = mockGoals,
                recentTransactions = mockTransactions
            ),
            recentTransactions = mockTransactions,
            event = MutableSharedFlow(),
            onAddExpenseClick = {},
            onAddIncomeClick = {},
            onTransferClick = {},
            onViewAllClick = {},
            onImportClick = {}
        )
    }
}
