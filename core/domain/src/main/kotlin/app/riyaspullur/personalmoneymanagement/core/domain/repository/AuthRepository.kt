package app.riyaspullur.personalmoneymanagement.core.domain.repository

import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    fun getCurrentUserId(): Flow<Long?>
    suspend fun getUserByUsername(username: String): User?
    suspend fun registerUser(username: String, passwordHash: String, displayName: String): Long
    suspend fun login(userId: Long)
    suspend fun logout()
    suspend fun hasUsers(): Boolean
    suspend fun updateUser(displayName: String)
    suspend fun updatePassword(newPasswordHash: String)
    fun getAllUsers(): Flow<List<User>>
}
