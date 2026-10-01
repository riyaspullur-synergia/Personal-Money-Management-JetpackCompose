package app.riyaspullur.personalmoneymanagement.core.data.repository

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.data.mapper.toDomain
import app.riyaspullur.personalmoneymanagement.core.database.dao.UserDao
import app.riyaspullur.personalmoneymanagement.core.database.entity.UserEntity
import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {

    private val userDao: UserDao = mockk()
    private val preferencesDataSource: AppPreferencesDataSource = mockk()
    private val repository = AuthRepositoryImpl(userDao, preferencesDataSource)

    private val user = UserEntity(id = 1, username = "riyas", passwordHash = "hash", displayName = "Riyas")

    @Test
    fun `getCurrentUser emits null when no user id is stored`() = runTest {
        every { preferencesDataSource.currentUserId } returns flowOf(null)

        repository.getCurrentUser().test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getCurrentUser loads the user matching the stored id`() = runTest {
        every { preferencesDataSource.currentUserId } returns flowOf(1L)
        every { userDao.getUserById(1L) } returns flowOf(user)

        repository.getCurrentUser().test {
            assertEquals(user.toDomain(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getUserByUsername delegates to dao`() = runTest {
        coEvery { userDao.getUserByUsername("riyas") } returns user

        assertEquals(user.toDomain(), repository.getUserByUsername("riyas"))
    }

    @Test
    fun `registerUser inserts a new user with the given credentials`() = runTest {
        val captured = slot<UserEntity>()
        coEvery { userDao.insertUser(capture(captured)) } returns 5L

        val id = repository.registerUser("riyas", "hashed", "Riyas Pullur")

        assertEquals(5L, id)
        assertEquals("riyas", captured.captured.username)
        assertEquals("hashed", captured.captured.passwordHash)
        assertEquals("Riyas Pullur", captured.captured.displayName)
    }

    @Test
    fun `login stores the given user id as the current user`() = runTest {
        coEvery { preferencesDataSource.setCurrentUserId(7L) } returns Unit

        repository.login(7L)

        coVerify { preferencesDataSource.setCurrentUserId(7L) }
    }

    @Test
    fun `logout clears the current user id`() = runTest {
        coEvery { preferencesDataSource.setCurrentUserId(null) } returns Unit

        repository.logout()

        coVerify { preferencesDataSource.setCurrentUserId(null) }
    }

    @Test
    fun `hasUsers is true when at least one user exists`() = runTest {
        coEvery { userDao.getUserCount() } returns 3

        assertTrue(repository.hasUsers())
    }

    @Test
    fun `hasUsers is false when no users exist`() = runTest {
        coEvery { userDao.getUserCount() } returns 0

        assertFalse(repository.hasUsers())
    }

    @Test
    fun `updateUser updates the display name of the current user`() = runTest {
        every { preferencesDataSource.currentUserId } returns flowOf(1L)
        every { userDao.getUserById(1L) } returns flowOf(user)
        val captured = slot<UserEntity>()
        coEvery { userDao.updateUser(capture(captured)) } returns Unit

        repository.updateUser("New Name")

        assertEquals("New Name", captured.captured.displayName)
        assertEquals(user.username, captured.captured.username)
    }

    @Test
    fun `updateUser does nothing when there is no current user`() = runTest {
        every { preferencesDataSource.currentUserId } returns flowOf(null)

        repository.updateUser("New Name")

        coVerify(exactly = 0) { userDao.updateUser(any()) }
    }

    @Test
    fun `updatePassword updates the password hash of the current user`() = runTest {
        every { preferencesDataSource.currentUserId } returns flowOf(1L)
        every { userDao.getUserById(1L) } returns flowOf(user)
        val captured = slot<UserEntity>()
        coEvery { userDao.updateUser(capture(captured)) } returns Unit

        repository.updatePassword("newHash")

        assertEquals("newHash", captured.captured.passwordHash)
    }

    @Test
    fun `getAllUsers delegates to dao`() = runTest {
        every { userDao.getAllUsers() } returns flowOf(listOf(user))

        repository.getAllUsers().test {
            assertEquals(listOf(user.toDomain()), awaitItem())
            awaitComplete()
        }
    }
}
