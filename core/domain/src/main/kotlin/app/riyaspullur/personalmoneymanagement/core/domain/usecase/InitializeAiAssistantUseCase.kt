package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.AiInitializationResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantClient
import javax.inject.Inject

class InitializeAiAssistantUseCase @Inject constructor(
    private val client: AiAssistantClient,
    private val assistantUseCase: AiAssistantUseCase
) {
    suspend operator fun invoke(apiKey: String): AiInitializationResult {
        val systemInstruction = assistantUseCase.getSystemInstruction()
        return client.initialize(apiKey, systemInstruction)
    }
}
