package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import android.util.Base64
import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetCurrentUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.UpdatePasswordUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.UpdateProfileUseCase
import app.riyaspullur.personalmoneymanagement.core.security.PasswordHasher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
    private val updateProfileUseCase = UpdateProfileUseCase(authRepository)
    private val updatePasswordUseCase = UpdatePasswordUseCase(authRepository)
    
    private lateinit var viewModel: ProfileViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val mockUser = User(1, "test", "hash", "Test User")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any<ByteArray>(), any()) } answers {
            java.util.Base64.getEncoder().encodeToString(firstArg<ByteArray>())
        }
        every { Base64.decode(any<String>(), any()) } answers {
            java.util.Base64.getDecoder().decode(firstArg<String>())
        }
        coEvery { authRepository.getCurrentUser() } returns flowOf(mockUser)
        viewModel = ProfileViewModel(getCurrentUserUseCase, updateProfileUseCase, updatePasswordUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Base64::class)
    }

    @Test
    fun `Initial state is success when user exists`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state is ProfileUiState.Success)
            assertEquals("Test User", (state as ProfileUiState.Success).user.displayName)
        }
    }

    @Test
    fun `Initial state is error when no user is signed in`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns flowOf(null)
        val vm = ProfileViewModel(
            getCurrentUserUseCase,
            updateProfileUseCase,
            updatePasswordUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            assertEquals(ProfileUiState.Error("User not found"), awaitItem())
        }
    }

    @Test
    fun `updateProfile calls repository`() = runTest {
        val newName = "New Name"
        viewModel.updateProfile(newName)
        testDispatcher.scheduler.advanceUntilIdle()
        coVerify { authRepository.updateUser(newName) }
    }

    @Test
    fun `updateProfile emits an error event when the repository throws`() = runTest {
        coEvery { authRepository.updateUser(any()) } throws RuntimeException("network down")

        viewModel.event.test {
            viewModel.updateProfile("New Name")
            assertEquals(ProfileEvent.Error("network down"), awaitItem())
        }
    }

    @Test
    fun `verifyAndChangePassword with correct password succeeds`() = runTest {
        val storedHash = PasswordHasher.hashPassword("oldPass")
        coEvery { authRepository.getCurrentUser() } returns flowOf(mockUser.copy(passwordHash = storedHash))
        val capturedHash = slot<String>()
        coEvery { authRepository.updatePassword(capture(capturedHash)) } returns Unit

        viewModel.event.test {
            viewModel.verifyAndChangePassword("oldPass", "newPass")
            assertEquals(ProfileEvent.PasswordChanged, awaitItem())
        }
        assertTrue(PasswordHasher.verifyPassword("newPass", capturedHash.captured))
    }

    @Test
    fun `verifyAndChangePassword with wrong password emits an error and does not update`() = runTest {
        val storedHash = PasswordHasher.hashPassword("oldPass")
        coEvery { authRepository.getCurrentUser() } returns flowOf(mockUser.copy(passwordHash = storedHash))

        viewModel.event.test {
            viewModel.verifyAndChangePassword("wrongPass", "newPass")
            assertEquals(ProfileEvent.Error("Incorrect current password"), awaitItem())
        }
        coVerify(exactly = 0) { authRepository.updatePassword(any()) }
    }

    @Test
    fun `verifyAndChangePassword when there is no current user emits an error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns flowOf(null)

        viewModel.event.test {
            viewModel.verifyAndChangePassword("oldPass", "newPass")
            assertEquals(ProfileEvent.Error("User not found"), awaitItem())
        }
    }
}
