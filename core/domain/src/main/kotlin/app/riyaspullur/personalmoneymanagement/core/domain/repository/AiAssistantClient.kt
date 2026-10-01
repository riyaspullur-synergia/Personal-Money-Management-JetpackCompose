package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiInitializationResult
import kotlinx.coroutines.flow.Flow

interface AiAssistantClient {
    suspend fun initialize(apiKey: String, systemInstruction: String): AiInitializationResult
    fun generateResponseStream(prompt: String): Flow<String>
    suspend fun generateResponse(prompt: String): String
    fun isInitialized(): Boolean
}
