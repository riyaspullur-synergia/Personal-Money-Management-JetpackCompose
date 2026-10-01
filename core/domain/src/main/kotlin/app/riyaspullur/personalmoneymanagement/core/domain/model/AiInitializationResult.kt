package app.riyaspullur.personalmoneymanagement.core.domain.model

sealed class AiInitializationResult {
    data object Success : AiInitializationResult()
    data object InvalidKey : AiInitializationResult()
    data class Error(val message: String) : AiInitializationResult()
}
