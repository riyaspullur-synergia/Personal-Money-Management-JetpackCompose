package app.riyaspullur.personalmoneymanagement.core.ai

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import app.riyaspullur.personalmoneymanagement.core.domain.model.AiInitializationResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiAssistantEngine @Inject constructor() : AiAssistantClient {
    
    private var generativeModel: GenerativeModel? = null
    private var chat: Chat? = null

    override suspend fun initialize(apiKey: String, systemInstruction: String): AiInitializationResult {
        return try {
            if (apiKey.isBlank()) return AiInitializationResult.InvalidKey
            
            generativeModel = GenerativeModel(
                modelName = "gemini-flash-latest",
                apiKey = apiKey,
                systemInstruction = content { text(systemInstruction) }
            )
            chat = generativeModel?.startChat()
            AiInitializationResult.Success
        } catch (e: Exception) {
            AiInitializationResult.Error(e.message ?: "Failed to initialize Gemini")
        }
    }

    override fun generateResponseStream(prompt: String): Flow<String> {
        val currentChat = chat ?: throw IllegalStateException("Gemini not initialized. Please provide a valid API Key.")
        return currentChat.sendMessageStream(prompt).map { it.text ?: "" }
    }

    override suspend fun generateResponse(prompt: String): String {
        val currentChat = chat ?: throw IllegalStateException("Gemini not initialized. Please provide a valid API Key.")
        return try {
            currentChat.sendMessage(prompt).text ?: ""
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    override fun isInitialized(): Boolean = generativeModel != null

    fun release() {
        chat = null
        generativeModel = null
    }
}
