package app.riyaspullur.personalmoneymanagement.core.domain.model

sealed interface DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>
    data class Error(val error: DomainError) : DomainResult<Nothing>
}
