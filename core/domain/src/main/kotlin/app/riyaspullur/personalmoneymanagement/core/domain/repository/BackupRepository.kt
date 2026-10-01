package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupArchive
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat

interface BackupRepository {
    suspend fun export(userId: Long, destination: String, format: BackupFormat)
    suspend fun read(source: String): BackupArchive
    /** Replaces only this user's records. Returns false if appearance preferences could not be restored. */
    suspend fun restore(userId: Long, archive: BackupArchive): Boolean
}
