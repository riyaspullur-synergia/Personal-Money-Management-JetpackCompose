package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.UserDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val preferencesDataSource: AppPreferencesDataSource
) : AuthRepository {
    override fun getCurrentUser(): Flow<User?> {
        return preferencesDataSource.currentUserId.flatMapLatest { id ->
            if (id == null) flowOf(null) else userDao.getUserById(id).map { it?.toDomain() }
        }
    }

    override fun getCurrentUserId(): Flow<Long?> = preferencesDataSource.currentUserId

    override suspend fun getUserByUsername(username: String): User? {
        return userDao.getUserByUsername(username)?.toDomain()
    }

    override suspend fun registerUser(username: String, passwordHash: String, displayName: String): Long {
        return userDao.insertUser(
            UserEntity(username = username, passwordHash = passwordHash, displayName = displayName)
        )
    }

    override suspend fun login(userId: Long) {
        preferencesDataSource.setCurrentUserId(userId)
    }

    override suspend fun logout() {
        preferencesDataSource.setCurrentUserId(null)
    }

    override suspend fun hasUsers(): Boolean {
        return userDao.getUserCount() > 0
    }

    override suspend fun updateUser(displayName: String) {
        val currentUserId = preferencesDataSource.currentUserId.first() ?: return
        val user = userDao.getUserById(currentUserId).first() ?: return
        userDao.updateUser(user.copy(displayName = displayName))
    }

    override suspend fun updatePassword(newPasswordHash: String) {
        val currentUserId = preferencesDataSource.currentUserId.first() ?: return
        val user = userDao.getUserById(currentUserId).first() ?: return
        userDao.updateUser(user.copy(passwordHash = newPasswordHash))
    }

    override fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities -> entities.map { it.toDomain() } }
    }
}
