package app.riyaspullur.personalmoneymanagement.core.domain.model

sealed interface DomainError {
    data class DatabaseError(val message: String) : DomainError
    data class FileError(val message: String) : DomainError
    data class ValidationError(val message: String) : DomainError
    data class NotFoundError(val message: String) : DomainError
    data class UnknownError(val message: String) : DomainError
}
