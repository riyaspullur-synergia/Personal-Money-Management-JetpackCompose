package app.riyaspullur.personalmoneymanagement.core.datastore

import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiSettingsRepository
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    abstract fun bindSettingsRepository(impl: AppPreferencesDataSource): SettingsRepository

    @Binds
    abstract fun bindAiSettingsRepository(impl: AiPreferencesDataSource): AiSettingsRepository
}
