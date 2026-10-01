package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupArchive
import app.riyaspullur.personalmoneymanagement.core.domain.model.BackupFormat
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.BackupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(destination: String, format: BackupFormat) {
        repository.export(requireNotNull(authRepository.getCurrentUserId().first()), destination, format)
    }
}

class ReadBackupUseCase @Inject constructor(private val repository: BackupRepository) {
    suspend operator fun invoke(source: String): BackupArchive = repository.read(source)
}

class RestoreBackupUseCase @Inject constructor(
    private val repository: BackupRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(archive: BackupArchive, expectedUserId: Long): Boolean {
        check(authRepository.getCurrentUserId().first() == expectedUserId)
        return repository.restore(expectedUserId, archive)
    }
}
