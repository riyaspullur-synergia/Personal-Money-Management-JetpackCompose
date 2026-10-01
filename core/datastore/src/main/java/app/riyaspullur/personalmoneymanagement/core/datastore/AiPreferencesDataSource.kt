package app.riyaspullur.personalmoneymanagement.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiPreferencesStore: DataStore<AiPreferences> by dataStore(
    fileName = "ai_prefs.pb",
    serializer = AiPreferencesSerializer()
)

@Singleton
class AiPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : AiSettingsRepository {
    override val geminiApiKey: Flow<String?> = context.aiPreferencesStore.data.map { 
        it.geminiApiKey.ifEmpty { null }
    }

    override suspend fun setGeminiApiKey(apiKey: String?) {
        context.aiPreferencesStore.updateData { currentPreferences ->
            currentPreferences.toBuilder()
                .setGeminiApiKey(apiKey ?: "")
                .build()
        }
    }
}
