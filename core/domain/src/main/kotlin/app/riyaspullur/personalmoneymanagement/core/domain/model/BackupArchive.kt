package app.riyaspullur.personalmoneymanagement.core.domain.model

import kotlinx.serialization.Serializable

enum class BackupFormat(val extension: String, val mimeType: String) {
    CSV("csv", "text/csv"),
    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
}

@Serializable
data class BackupArchive(
    val format: String = "PersonalMoneyManagementBackup",
    val version: Int = 1,
    val createdAt: Long,
    val displayName: String,
    val themeMode: String,
    val language: String,
    val data: BackupData,
    // Keys are transaction IDs, never paths supplied by the importing device.
    val receipts: Map<String, String> = emptyMap()
)
