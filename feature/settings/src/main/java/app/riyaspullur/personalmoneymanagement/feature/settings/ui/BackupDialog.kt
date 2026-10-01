package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import app.riyaspullur.personalmoneymanagement.core.ui.R
import java.text.DateFormat
import java.util.Date

@Composable
fun BackupDialog(viewModel: BackupViewModel, onDismiss: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupFormat.CSV.mimeType)) {
        it?.let { uri -> viewModel.export(uri.toString(), BackupFormat.CSV) }
    }
    val excelLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupFormat.XLSX.mimeType)) {
        it?.let { uri -> viewModel.export(uri.toString(), BackupFormat.XLSX) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri -> viewModel.inspect(uri.toString()) }
    }
    val preview = state.preview
    AlertDialog(
        onDismissRequest = { if (!state.busy) { viewModel.reset(); onDismiss() } },
        title = { Text(stringResource(if (preview == null) R.string.settings_backup_restore else R.string.backup_restore_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.busy) {
                    LinearProgressIndicator()
                    Text(stringResource(R.string.backup_working))
                } else if (preview != null) {
                    Text(stringResource(R.string.backup_preview,
                        preview.displayName,
                        DateFormat.getDateTimeInstance().format(Date(preview.createdAt)),
                        preview.data.accounts.size, preview.data.transactions.size,
                        preview.data.categories.size, preview.data.budgets.size,
                        preview.data.savingsGoals.size, preview.receipts.size))
                    Text(stringResource(R.string.backup_replace_warning))
                } else {
                    Text(stringResource(R.string.backup_instructions))
                    state.message?.let { Text(stringResource(it)) }
                    TextButton(onClick = { csvLauncher.launch("Money-backup-${System.currentTimeMillis()}.csv") }) {
                        Text(stringResource(R.string.backup_export_csv))
                    }
                    TextButton(onClick = { excelLauncher.launch("Money-backup-${System.currentTimeMillis()}.xlsx") }) {
                        Text(stringResource(R.string.backup_export_excel))
                    }
                    TextButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) {
                        Text(stringResource(R.string.backup_import))
                    }
                }
            }
        },
        confirmButton = {
            if (preview != null) TextButton(onClick = viewModel::restore, enabled = !state.busy) {
                Text(stringResource(R.string.backup_replace_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.reset(); onDismiss() }, enabled = !state.busy) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
