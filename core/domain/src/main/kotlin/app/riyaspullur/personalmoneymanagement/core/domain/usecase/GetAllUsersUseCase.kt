package app.riyaspullur.personalmoneymanagement.core.domain.usecase

import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<List<User>> = authRepository.getAllUsers()
}
