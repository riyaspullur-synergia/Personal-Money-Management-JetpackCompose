package app.riyaspullur.personalmoneymanagement.feature.reports.ui

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategoryReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DailyTrendItem
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.core.ui.FontScalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.LocalePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.R
import app.riyaspullur.personalmoneymanagement.core.ui.ThemePreviews
import app.riyaspullur.personalmoneymanagement.core.ui.components.EmptyState
import app.riyaspullur.personalmoneymanagement.core.ui.components.LoadingScreen
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme

/**
 * ReportsViewModel keeps a fixed English error literal (unchanged, to stay backward compatible
 * with existing ViewModel tests). This maps the known default fallback to a localized string at
 * the UI boundary; anything unrecognized (e.g. a raw exception message) is shown as-is.
 */
@Composable
private fun localizedReportsError(message: String): String = when (message) {
    "Unknown error" -> stringResource(R.string.common_unknown_error)
    else -> message
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    uiState: ReportsUiState,
    accounts: List<AccountRecord>,
    onViewPdf: () -> Unit,
    onSharePdf: () -> Unit,
    onExportCsv: () -> Unit,
    onDownloadTemplate: () -> Unit,
    onSelectAccount: (Long?) -> Unit,
    onDateRangeSelect: (Long, Long) -> Unit
) {
    var showPdfMenu by remember { mutableStateOf(false) }
    var showCsvMenu by remember { mutableStateOf(false) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTemplatePreview by remember { mutableStateOf(false) }

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
                TextButton(onClick = { 
                    showTemplatePreview = false
                    onDownloadTemplate()
                }) {
                    Text(stringResource(R.string.reports_download_template_menu_item))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTemplatePreview = false }) {
                    Text(stringResource(R.string.common_ok))
                }
            }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val start = datePickerState.selectedStartDateMillis
                    val end = datePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onDateRangeSelect(start, end)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.reports_date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DateRangePicker(state = datePickerState, modifier = Modifier.weight(1f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports)) },
                actions = {
                    Box {
                        IconButton(onClick = { showPdfMenu = true }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = stringResource(R.string.reports_export_pdf_content_description))
                        }
                        DropdownMenu(
                            expanded = showPdfMenu,
                            onDismissRequest = { showPdfMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reports_view_pdf_menu_item)) },
                                onClick = {
                                    showPdfMenu = false
                                    onViewPdf()
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reports_share_pdf_menu_item)) },
                                onClick = {
                                    showPdfMenu = false
                                    onSharePdf()
                                },
                                leadingIcon = { Icon(Icons.Default.Share, null) }
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showCsvMenu = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.reports_export_csv_content_description))
                        }
                        DropdownMenu(
                            expanded = showCsvMenu,
                            onDismissRequest = { showCsvMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reports_export_csv_content_description)) },
                                onClick = {
                                    showCsvMenu = false
                                    onExportCsv()
                                },
                                leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reports_preview_template_menu_item)) },
                                onClick = {
                                    showCsvMenu = false
                                    showTemplatePreview = true
                                },
                                leadingIcon = { Icon(Icons.Default.Info, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reports_download_template_menu_item)) },
                                onClick = {
                                    showCsvMenu = false
                                    onDownloadTemplate()
                                },
                                leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                            )
                        }
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.reports_select_date_range_content_description))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Report Info Header
            val allAccountsLabel = stringResource(R.string.reports_all_accounts_label)
            val accountName = selectedAccountId?.let { id ->
                accounts.find { it.id == id }?.name
            } ?: allAccountsLabel

            val infoText = if (uiState is ReportsUiState.Success) {
                stringResource(R.string.reports_info_with_month, accountName, uiState.formattedMonthYear)
            } else {
                stringResource(R.string.reports_info_without_month, accountName)
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .basicMarquee(),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Account Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FilterChip(
                    selected = selectedAccountId == null,
                    onClick = { 
                        selectedAccountId = null
                        onSelectAccount(null)
                    },
                    label = { Text(allAccountsLabel) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                accounts.forEach { account ->
                    FilterChip(
                        selected = selectedAccountId == account.id,
                        onClick = { 
                            selectedAccountId = account.id
                            onSelectAccount(account.id)
                        },
                        label = { Text(account.name) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            when (uiState) {
                is ReportsUiState.Loading -> {
                    LoadingScreen()
                }
                is ReportsUiState.Success -> {
                    ReportsContent(
                        report = uiState.report,
                        modifier = Modifier.weight(1f)
                    )
                }
                is ReportsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = localizedReportsError(uiState.message), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsContent(
    report: MonthlyReport,
    modifier: Modifier = Modifier
) {
    if (report.categoryBreakdown.isEmpty()) {
        EmptyState(
            icon = Icons.Default.BarChart,
            title = stringResource(R.string.reports_empty_title),
            subtitle = stringResource(R.string.reports_empty_subtitle),
            modifier = modifier
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SummarySection(report)
        }

        item {
            Text(text = stringResource(R.string.reports_daily_expenses_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            DailyTrendSection(report)
        }

        item {
            Text(text = stringResource(R.string.reports_spending_by_category_title), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            PieChartSection(report)
        }

        item {
            CategoryBarChartSection(report)
        }

        items(report.categoryBreakdown.size) { index ->
            CategoryBreakdownItem(report.categoryBreakdown[index], index)
        }
    }
}

@FontScalePreviews
@ThemePreviews
@LocalePreviews
@Composable
fun ReportsScreenPreview() {
    val mockAccounts = listOf(
        AccountRecord(id = 1, userId = 1, name = "Cash", type = app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType.CASH, initialBalance = 100000, currency = Currency.AED, icon = null, color = 0xFF4CAF50.toInt()),
        AccountRecord(id = 2, userId = 1, name = "Bank", type = app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType.BANK, initialBalance = 500000, currency = Currency.AED, icon = null, color = 0xFF2196F3.toInt())
    )

    val mockReport = MonthlyReport(
        totalIncome = Money(500000L, Currency.AED),
        totalExpense = Money(250000L, Currency.AED),
        netBalance = Money(250000L, Currency.AED),
        categoryBreakdown = listOf(
            CategoryReport(
                category = Category(id = 1, userId = 1, name = "Food", icon = null, color = 0xFFF44336.toInt(), type = TransactionType.EXPENSE),
                amount = Money(150000L, Currency.AED),
                percentage = 0.6f
            ),
            CategoryReport(
                category = Category(id = 2, userId = 1, name = "Transport", icon = null, color = 0xFF2196F3.toInt(), type = TransactionType.EXPENSE),
                amount = Money(100000L, Currency.AED),
                percentage = 0.4f
            )
        ),
        dailyExpenses = listOf(
            DailyTrendItem(date = System.currentTimeMillis(), amount = Money(50000L, Currency.AED))
        ),
        dailyIncomes = listOf(
            DailyTrendItem(date = System.currentTimeMillis(), amount = Money(100000L, Currency.AED))
        )
    )

    PersonalMoneyManagemntTheme {
        ReportsScreen(
            uiState = ReportsUiState.Success(mockReport, "August 2026"),
            accounts = mockAccounts,
            onViewPdf = {},
            onSharePdf = {},
            onExportCsv = {},
            onDownloadTemplate = {},
            onSelectAccount = {},
            onDateRangeSelect = { _, _ -> }
        )
    }
}
