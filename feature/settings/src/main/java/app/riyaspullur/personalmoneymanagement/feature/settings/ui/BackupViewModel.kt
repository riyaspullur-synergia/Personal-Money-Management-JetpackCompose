package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupArchive
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.ExportBackupUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetCurrentUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.ReadBackupUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.RestoreBackupUseCase
import app.riyaspullur.personalmoneymanagement.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BackupUiState(
    val busy: Boolean = false,
    val preview: BackupArchive? = null,
    val message: Int? = null
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val exportBackup: ExportBackupUseCase,
    private val readBackup: ReadBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
    private val getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState = _uiState.asStateFlow()
    private var restoreUserId: Long? = null

    fun export(destination: String, format: BackupFormat) = runOperation(R.string.backup_export_error) {
        exportBackup(destination, format)
        BackupUiState(message = R.string.backup_export_success)
    }

    fun inspect(source: String) = runOperation(R.string.backup_read_error) {
        restoreUserId = requireNotNull(getCurrentUser().first()).id
        BackupUiState(preview = readBackup(source))
    }

    fun restore() {
        val archive = _uiState.value.preview ?: return
        val userId = restoreUserId ?: return
        runOperation(R.string.backup_restore_error) {
            val preferencesRestored = restoreBackup(archive, userId)
            restoreUserId = null
            BackupUiState(message = if (preferencesRestored) R.string.backup_restore_success else R.string.backup_preferences_error)
        }
    }

    fun reset() {
        if (!_uiState.value.busy) {
            restoreUserId = null
            _uiState.value = BackupUiState()
        }
    }

    private fun runOperation(errorMessage: Int, operation: suspend () -> BackupUiState) {
        if (_uiState.value.busy) return
        _uiState.value = BackupUiState(busy = true)
        viewModelScope.launch {
            try {
                _uiState.value = operation()
            } catch (exception: CancellationException) {
                _uiState.value = BackupUiState()
                throw exception
            } catch (_: Exception) {
                _uiState.value = BackupUiState(message = errorMessage)
            }
        }
    }
}
