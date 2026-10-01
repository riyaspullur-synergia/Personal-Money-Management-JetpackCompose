package app.riyaspullur.personalmoneymanagement.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<String>
    val language: Flow<String>
    val isAppLockEnabled: Flow<Boolean>
    
    suspend fun setThemeMode(mode: String)
    suspend fun setLanguage(code: String)
    suspend fun setAppLockEnabled(enabled: Boolean)
}
