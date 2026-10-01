package app.riyaspullur.personalmoneymanagement.feature.reports.ui

import android.content.Context
import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveAccountsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetMonthlyReportUseCase
import app.riyaspullur.personalmoneymanagement.core.export.CsvGenerator
import app.riyaspullur.personalmoneymanagement.core.export.ExportManager
import app.riyaspullur.personalmoneymanagement.core.export.PdfGenerator
import io.mockk.Runs
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val context: Context = mockk()
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase = mockk()
    private val getMonthlyReportUseCase: GetMonthlyReportUseCase = mockk()
    private val getActiveAccountsUseCase: GetActiveAccountsUseCase = mockk()
    private val pdfGenerator: PdfGenerator = mockk()
    private val csvGenerator: CsvGenerator = mockk()
    private val exportManager: ExportManager = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val report = MonthlyReport(
        totalIncome = Money(1000L, Currency.AED),
        totalExpense = Money(500L, Currency.AED),
        netBalance = Money(500L, Currency.AED),
        categoryBreakdown = emptyList(),
        dailyExpenses = emptyList(),
        dailyIncomes = emptyList(),
        transactions = emptyList(),
        totalDeposit = Money.zero(),
        totalInvestment = Money.zero()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getAuthenticatedUserIdUseCase() } returns flowOf(1L)
        every { getActiveAccountsUseCase(1L) } returns flowOf(emptyList<AccountRecord>())
        every { getMonthlyReportUseCase(1L, any(), any(), any()) } returns flowOf(report)
        every { context.getString(any()) } returns "test"
        every { context.getString(any(), any()) } returns "test"
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ReportsViewModel {
        val vm = ReportsViewModel(context, getAuthenticatedUserIdUseCase, getMonthlyReportUseCase, getActiveAccountsUseCase, pdfGenerator, csvGenerator, exportManager)
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `uiState resolves to Success with the monthly report`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ReportsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            val state = awaitItem()
            assertTrue(state is ReportsUiState.Success)
            assertEquals(report, (state as ReportsUiState.Success).report)
        }
    }

    @Test
    fun `uiState is Error when the use case flow throws`() = runTest {
        every { getMonthlyReportUseCase(1L, any(), any(), any()) } returns flow { throw RuntimeException("failed") }
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(ReportsUiState.Loading, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(ReportsUiState.Error("failed"), awaitItem())
        }
    }

    @Test
    fun `accounts stream reflects the repository`() = runTest {
        val account = AccountRecord(id = 1, userId = 1, name = "Cash", type = AccountType.CASH, initialBalance = 0, currency = Currency.AED, icon = null, color = 0)
        every { getActiveAccountsUseCase(1L) } returns flowOf(listOf(account))
        val viewModel = createViewModel()

        viewModel.accounts.test {
            assertEquals(emptyList<AccountRecord>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(account), awaitItem())
        }
    }

    /**
     * uiState is a WhileSubscribed(5000) StateFlow: it only resolves past its Loading seed
     * while something is actively collecting it. Subscribing once here (then letting the
     * collector finish) leaves the resolved value cached on .value for the 5s grace window,
     * which is what viewPdf()/exportCsv() read synchronously.
     */
    private suspend fun settleUiState(viewModel: ReportsViewModel) {
        viewModel.uiState.test {
            awaitItem()
            testDispatcher.scheduler.advanceUntilIdle()
            awaitItem()
        }
    }

    @Test
    fun `viewPdf generates the pdf and emits a navigate event`() = runTest {
        val viewModel = createViewModel()
        settleUiState(viewModel)
        val file = File("/tmp/report.pdf")
        every { exportManager.getExportFile(any()) } returns file
        every { pdfGenerator.generateMonthlyReport(file, report, any()) } just Runs

        viewModel.event.test {
            viewModel.viewPdf()
            val event = awaitItem()
            assertTrue(event is ReportsEvent.NavigateToPdfViewer)
            assertEquals(file.absolutePath, (event as ReportsEvent.NavigateToPdfViewer).filePath)
        }
        coVerify { pdfGenerator.generateMonthlyReport(file, report, any()) }
    }

    @Test
    fun `exportCsv generates the csv and shares it`() = runTest {
        val viewModel = createViewModel()
        settleUiState(viewModel)
        val file = File("/tmp/report.csv")
        every { exportManager.getExportFile(any()) } returns file
        every { csvGenerator.generateFullReportCsv(file, report, any()) } just Runs
        every { exportManager.shareFile(file, "text/csv") } just Runs

        viewModel.exportCsv()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { csvGenerator.generateFullReportCsv(file, report, any()) }
        coVerify { exportManager.shareFile(file, "text/csv") }
    }
}
