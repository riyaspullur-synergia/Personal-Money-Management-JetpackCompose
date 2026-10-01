package app.riyaspullur.personalmoneymanagement.feature.reports.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountRecord
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetActiveAccountsUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetMonthlyReportUseCase
import app.riyaspullur.personalmoneymanagement.core.export.CsvGenerator
import app.riyaspullur.personalmoneymanagement.core.export.ExportManager
import app.riyaspullur.personalmoneymanagement.core.export.PdfGenerator
import app.riyaspullur.personalmoneymanagement.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val getMonthlyReportUseCase: GetMonthlyReportUseCase,
    private val getActiveAccountsUseCase: GetActiveAccountsUseCase,
    private val pdfGenerator: PdfGenerator,
    private val csvGenerator: CsvGenerator,
    private val exportManager: ExportManager
) : ViewModel() {

    private val _startDate = MutableStateFlow(getStartOfMonth())
    private val _endDate = MutableStateFlow(getEndOfMonth())
    private val _selectedAccountId = MutableStateFlow<Long?>(null)

    private val _event = MutableSharedFlow<ReportsEvent>()
    val event = _event.asSharedFlow()

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    val accounts: StateFlow<List<AccountRecord>> = getAuthenticatedUserIdUseCase()
        .filterNotNull()
        .flatMapLatest { userId -> getActiveAccountsUseCase(userId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<ReportsUiState> = combine(
        _startDate, _endDate, _selectedAccountId, getAuthenticatedUserIdUseCase().filterNotNull()
    ) { start, end, accountId, userId ->
        val formattedRange = "${dateFormat.format(Date(start))} - ${dateFormat.format(Date(end))}"
        
        getMonthlyReportUseCase(userId, start, end, accountId)
            .map { report -> ReportsUiState.Success(report, formattedRange) as ReportsUiState }
            .catch { e -> emit(ReportsUiState.Error(e.message ?: "Unknown error")) }
    }.flattenConcat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportsUiState.Loading)

    fun setDateRange(start: Long, end: Long) {
        _startDate.value = start
        _endDate.value = end
    }

    fun selectAccount(accountId: Long?) {
        _selectedAccountId.value = accountId
    }

    private fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun viewPdf() {
        val currentState = uiState.value
        if (currentState is ReportsUiState.Success) {
            viewModelScope.launch {
                val file = exportManager.getExportFile("Report_${currentState.formattedMonthYear}.pdf")
                val title = context.getString(R.string.pdf_gen_monthly_report_title, currentState.formattedMonthYear)
                pdfGenerator.generateMonthlyReport(file, currentState.report, title)
                _event.emit(ReportsEvent.NavigateToPdfViewer(file.absolutePath))
            }
        }
    }

    fun sharePdf() {
        val currentState = uiState.value
        if (currentState is ReportsUiState.Success) {
            viewModelScope.launch {
                val file = exportManager.getExportFile("Report_${currentState.formattedMonthYear}.pdf")
                val title = context.getString(R.string.pdf_gen_monthly_report_title, currentState.formattedMonthYear)
                pdfGenerator.generateMonthlyReport(file, currentState.report, title)
                exportManager.shareFile(file, "application/pdf")
            }
        }
    }

    fun shareFile(path: String) {
        viewModelScope.launch {
            val file = File(path)
            if (file.exists()) {
                exportManager.shareFile(file, "application/pdf")
            }
        }
    }

    fun exportCsv() {
        val currentState = uiState.value
        if (currentState is ReportsUiState.Success) {
            viewModelScope.launch {
                val file = exportManager.getExportFile("Full_Report_${currentState.formattedMonthYear}.csv")
                val title = context.getString(R.string.pdf_gen_report_info, currentState.formattedMonthYear)
                csvGenerator.generateFullReportCsv(file, currentState.report, title)
                exportManager.shareFile(file, "text/csv")
            }
        }
    }

    fun downloadTemplate() {
        viewModelScope.launch {
            val file = exportManager.getExportFile("Transaction_Import_Template.csv")
            csvGenerator.generateSampleTemplate(file)
            exportManager.shareFile(file, "text/csv")
        }
    }
}

sealed interface ReportsUiState {
    data object Loading : ReportsUiState
    data class Success(
        val report: MonthlyReport,
        val formattedMonthYear: String
    ) : ReportsUiState
    data class Error(val message: String) : ReportsUiState
}

sealed interface ReportsEvent {
    data class NavigateToPdfViewer(val filePath: String) : ReportsEvent
}
