package app.riyaspullur.personalmoneymanagement

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Budget
import app.riyaspullur.personalmoneymanagement.core.domain.model.BudgetStatus
import app.riyaspullur.personalmoneymanagement.core.domain.model.Category
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.DashboardSummary
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.SavingsGoal
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType
import app.riyaspullur.personalmoneymanagement.feature.dashboard.ui.DashboardScreen
import app.riyaspullur.personalmoneymanagement.feature.dashboard.ui.DashboardUiState
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.AddTransactionScreen
import app.riyaspullur.personalmoneymanagement.feature.transactions.ui.AddTransactionUiState
import app.riyaspullur.personalmoneymanagement.ui.theme.PersonalMoneyManagemntTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualPassPreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun save(fileName: String) {
        composeTestRule.waitForIdle()
        val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
        resolver.openOutputStream(uri)!!.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private val mockSummary = DashboardSummary(
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
        budgetStatus = BudgetStatus(
            budget = Budget(
                id = 1,
                userId = 1,
                name = "Monthly",
                totalLimit = Money(500000L, Currency.AED),
                startDate = 0L,
                endDate = 0L,
                alertThreshold = 0.8f,
                isRolloverEnabled = false
            ),
            spent = Money(350000L, Currency.AED),
            remaining = Money(150000L, Currency.AED),
            progress = 0.7f,
            daysRemaining = 12,
            dailyAllowance = Money(12500L, Currency.AED)
        )
    )

    private val mockTransactions = listOf(
        Transaction(
            id = 1, userId = 1, accountId = 1, amount = 15000L, currency = Currency.AED,
            categoryId = 1, type = TransactionType.EXPENSE, merchant = "Starbucks",
            description = "Coffee", notes = null, transactionDate = System.currentTimeMillis()
        ),
        Transaction(
            id = 2,
            userId = 1,
            accountId = 2,
            amount = 500000L,
            currency = Currency.AED,
            categoryId = 2,
            type = TransactionType.INCOME,
            merchant = "Salary",
            description = "Monthly Salary",
            notes = null,
            transactionDate = System.currentTimeMillis() - 86400000
        ),
        Transaction(
            id = 3,
            userId = 1,
            accountId = 1,
            amount = 20000L,
            currency = Currency.AED,
            categoryId = null,
            type = TransactionType.TRANSFER,
            merchant = null,
            description = "To Savings",
            notes = null,
            transactionDate = System.currentTimeMillis() - 2 * 86400000
        )
    )

    private val mockGoals = listOf(
        SavingsGoal(
            id = 1,
            userId = 1,
            name = "New Car",
            targetAmount = 10000000L,
            currentAmount = 2500000L,
            currency = Currency.AED,
            targetDate = null,
            icon = null,
            color = 0xFF4CAF50.toInt()
        )
    )

    private fun dashboard(dark: Boolean) {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme(darkTheme = dark) {
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
    }

    @Test
    fun dashboardLight() {
        dashboard(false)
        save("vpass_dashboard_light.png")
    }

    @Test
    fun dashboardDark() {
        dashboard(true)
        save("vpass_dashboard_dark.png")
    }

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
    private val mockCategories = listOf(
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

    private fun addTransaction(dark: Boolean) {
        composeTestRule.setContent {
            PersonalMoneyManagemntTheme(darkTheme = dark) {
                AddTransactionScreen(
                    accounts = mockAccounts,
                    categories = mockCategories,
                    uiState = AddTransactionUiState.Idle,
                    onAddClick = { _, _, _, _, _, _, _, _ -> },
                    onAddCategoryClick = { _, _ -> },
                    onBackClick = {}
                )
            }
        }
    }

    @Test
    fun addTransactionLight() {
        addTransaction(false)
        save("vpass_addtx_light.png")
    }

    @Test
    fun addTransactionDark() {
        addTransaction(true)
        save("vpass_addtx_dark.png")
    }
}
