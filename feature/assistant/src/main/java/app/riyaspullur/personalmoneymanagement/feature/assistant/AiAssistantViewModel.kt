package app.riyaspullur.personalmoneymanagement.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.model.AiInitializationResult
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiAssistantClient
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AiSettingsRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.AiAssistantUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAuthenticatedUserIdUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.InitializeAiAssistantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Message(
    val content: String,
    val isUser: Boolean
)

data class AiAssistantUiState(
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val error: String? = null,
    val isApiKeyMissing: Boolean = false
)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val assistantUseCase: AiAssistantUseCase,
    private val initializeAiAssistantUseCase: InitializeAiAssistantUseCase,
    private val getAuthenticatedUserIdUseCase: GetAuthenticatedUserIdUseCase,
    private val aiClient: AiAssistantClient,
    private val aiSettingsRepository: AiSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedKey = aiSettingsRepository.geminiApiKey.firstOrNull()
            if (savedKey != null && !aiClient.isInitialized()) {
                initializeWithKey(savedKey)
            } else if (!aiClient.isInitialized()) {
                _uiState.update { it.copy(isApiKeyMissing = true) }
            }
        }
    }

    fun initializeWithKey(apiKey: String) {
        viewModelScope.launch {
            val result = initializeAiAssistantUseCase(apiKey)
            _uiState.update { state ->
                when (result) {
                    is AiInitializationResult.Success -> {
                        viewModelScope.launch {
                            aiSettingsRepository.setGeminiApiKey(apiKey)
                        }
                        state.copy(isApiKeyMissing = false, error = null)
                    }
                    is AiInitializationResult.InvalidKey -> 
                        state.copy(isApiKeyMissing = true, error = "Invalid API Key")
                    is AiInitializationResult.Error -> 
                        state.copy(error = result.message)
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        if (!aiClient.isInitialized()) {
            _uiState.update { it.copy(error = "AI not initialized. Please provide an API key.") }
            return
        }

        val userMessage = Message(text, true)
        _uiState.update { it.copy(
            messages = it.messages + userMessage,
            isGenerating = true,
            error = null
        ) }

        viewModelScope.launch {
            try {
                val userId = getAuthenticatedUserIdUseCase().first() ?: -1L
                
                var assistantMessageContent = ""
                assistantUseCase.execute(userId, text).collect { chunk ->
                    if (chunk.startsWith("Thinking (using")) {
                        _uiState.update { state ->
                            state.copy(messages = state.messages + Message(chunk, false))
                        }
                    } else {
                        assistantMessageContent += chunk
                        _uiState.update { state ->
                            val lastMessage = state.messages.lastOrNull()
                            if (lastMessage != null && !lastMessage.isUser && !lastMessage.content.startsWith("Thinking (using")) {
                                val updatedMessages = state.messages.dropLast(1) + lastMessage.copy(content = assistantMessageContent)
                                state.copy(messages = updatedMessages, isGenerating = false)
                            } else {
                                state.copy(messages = state.messages + Message(assistantMessageContent, false), isGenerating = false)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGenerating = false,
                    error = e.message ?: "Failed to generate response"
                ) }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
