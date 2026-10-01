package app.riyaspullur.personalmoneymanagement.core.domain.model

sealed class AiToolResult {
    data class Success(val data: Any) : AiToolResult()
    data class Error(val message: String) : AiToolResult()
    
    override fun toString(): String = when(this) {
        is Success -> data.toString()
        is Error -> "Error: $message"
    }
}
