package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.repository.SettingsRepository
import javax.inject.Inject

class SetAppLockEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = settingsRepository.setAppLockEnabled(enabled)
}
