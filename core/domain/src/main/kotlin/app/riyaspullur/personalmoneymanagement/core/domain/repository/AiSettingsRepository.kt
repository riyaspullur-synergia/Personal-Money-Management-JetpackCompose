package app.riyaspullur.personalmoneymanagement.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface AiSettingsRepository {
    val geminiApiKey: Flow<String?>
    suspend fun setGeminiApiKey(apiKey: String?)
}
