package app.riyaspullur.personalmoneymanagement.feature.reports.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.CategoryReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockAccounts = listOf(
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

    private val mockReport = MonthlyReport(
        totalIncome = Money(500000L, Currency.AED),
        totalExpense = Money(250000L, Currency.AED),
        // Deliberately not totalIncome - totalExpense: kept distinct from every other amount on
        // screen so each formatted value below matches exactly one node.
        netBalance = Money(160000L, Currency.AED),
        categoryBreakdown = listOf(
            CategoryReport(
                category = Category(
                    id = 1,
                    userId = 1,
                    name = "Food",
                    icon = null,
                    color = 0xFFF44336.toInt(),
                    type = TransactionType.EXPENSE
                ),
                amount = Money(90000L, Currency.AED),
                percentage = 1f
            )
        ),
        dailyExpenses = emptyList(),
        dailyIncomes = emptyList()
    )

    @Test
    fun loadingStateShowsLoadingIndicator() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ReportsScreen(
                    uiState = ReportsUiState.Loading,
                    accounts = emptyList(),
                    onViewPdf = {}, onSharePdf = {}, onExportCsv = {},
                    onDownloadTemplate = {},
                    onSelectAccount = {}, onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Loading...").assertExists()
    }

    @Test
    fun errorStateShowsErrorMessage() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ReportsScreen(
                    uiState = ReportsUiState.Error("Failed to load report"),
                    accounts = emptyList(),
                    onViewPdf = {}, onSharePdf = {}, onExportCsv = {},
                    onDownloadTemplate = {},
                    onSelectAccount = {}, onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to load report").assertExists()
    }

    @Test
    fun successStateShowsIncomeAndExpenseSummary() {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ReportsScreen(
                    uiState = ReportsUiState.Success(mockReport, "August 2026"),
                    accounts = mockAccounts,
                    onViewPdf = {}, onSharePdf = {}, onExportCsv = {},
                    onDownloadTemplate = {},
                    onSelectAccount = {}, onDateRangeSelect = { _, _ -> }
                )
            }
        }

        // "Income"/"Expense" labels also appear in the daily-trend chart legend, and totalExpense
        // is rendered twice (summary row + pie chart center), so assert on totalIncome and
        // netBalance instead - each appears in exactly one place. AED renders via MoneyText as a
        // currency icon + plain amount text (no "AED" text prefix). The amount is formatted via
        // NumberFormat for the current locale, which groups thousands (e.g. "5,000.00").
        composeTestRule.onNodeWithText("Report: All Accounts (August 2026)").assertExists()
        composeTestRule.onNodeWithText("5,000.00").assertExists()
        composeTestRule.onNodeWithText("1,600.00").assertExists()
    }

    @Test
    fun selectingAccountChipInvokesOnSelectAccount() {
        var selectedAccountId: Long? = -1L

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ReportsScreen(
                    uiState = ReportsUiState.Success(mockReport, "August 2026"),
                    accounts = mockAccounts,
                    onViewPdf = {}, onSharePdf = {}, onExportCsv = {},
                    onDownloadTemplate = {},
                    onSelectAccount = { selectedAccountId = it }, onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Cash").performClick()

        assertEquals(1L, selectedAccountId)
    }

    @Test
    fun exportCsvButtonInvokesOnExportCsv() {
        var exportCsvClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ReportsScreen(
                    uiState = ReportsUiState.Success(mockReport, "August 2026"),
                    accounts = mockAccounts,
                    onViewPdf = {}, onSharePdf = {},
                    onExportCsv = { exportCsvClicked = true },
                    onDownloadTemplate = {},
                    onSelectAccount = {}, onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Export CSV").performClick()
        composeTestRule.onNodeWithText("Export CSV").performClick()

        assertTrue(exportCsvClicked)
    }

    @Test
    fun viewPdfMenuItemInvokesOnViewPdf() {
        var viewPdfClicked = false

        composeTestRule.setContent {
            PersonalMoneyManagemntTheme {
                ReportsScreen(
                    uiState = ReportsUiState.Success(mockReport, "August 2026"),
                    accounts = mockAccounts,
                    onViewPdf = { viewPdfClicked = true }, onSharePdf = {}, onExportCsv = {},
                    onDownloadTemplate = {},
                    onSelectAccount = {}, onDateRangeSelect = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Export PDF").performClick()
        composeTestRule.onNodeWithText("View PDF").performClick()

        assertTrue(viewPdfClicked)
    }
}
