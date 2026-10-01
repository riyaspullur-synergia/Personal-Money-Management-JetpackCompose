package app.riyaspullur.personalmoneymanagement.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SettingsRepository
import app.riyaspullur.personalmoneymanagement.core.util.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class AppPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private object PreferencesKeys {
        val CURRENT_USER_ID = longPreferencesKey("current_user_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val LOCK_TIMEOUT = longPreferencesKey("lock_timeout")
        val LANGUAGE = stringPreferencesKey("language")
        val MAX_EXPENSE_LIMIT = longPreferencesKey("max_expense_limit")
        val EXPENSE_LIMIT_START_DAY = stringPreferencesKey("expense_limit_start_day")
    }

    val maxExpenseLimit: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.MAX_EXPENSE_LIMIT] ?: 0L
    }

    suspend fun setMaxExpenseLimit(limit: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MAX_EXPENSE_LIMIT] = limit
        }
    }

    val expenseLimitStartDay: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EXPENSE_LIMIT_START_DAY] ?: "1"
    }

    suspend fun setExpenseLimitStartDay(day: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPENSE_LIMIT_START_DAY] = day
        }
    }

    val currentUserId: Flow<Long?> = context.dataStore.data.map { preferences ->
        val id = preferences[PreferencesKeys.CURRENT_USER_ID]
        if (id == null || id == -1L) null else id
    }

    suspend fun setCurrentUserId(userId: Long?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_USER_ID] = userId ?: -1L
        }
    }

    override val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    override suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    override val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_APP_LOCK_ENABLED] ?: false
    }

    override suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_APP_LOCK_ENABLED] = enabled
        }
    }

    override val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.LANGUAGE] ?: AppLanguage.Default.code
    }

    suspend fun restoreAppearance(themeMode: String, language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = themeMode
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    override suspend fun setLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = code
        }
    }
}
