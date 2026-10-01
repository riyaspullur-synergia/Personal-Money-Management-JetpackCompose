package app.riyaspullur.personalmoneymanagement.feature.auth.ui

import android.util.Base64
import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.model.User
import app.riyaspullur.personalmoneymanagement.core.domain.repository.AuthRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetAllUsersUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetCurrentUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetUserByUsernameUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.HasUsersUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.LoginUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.LogoutUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.RegisterUserUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SeedDefaultDataUseCase
import app.riyaspullur.personalmoneymanagement.core.security.PasswordHasher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
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
class AuthViewModelTest {

    private val authRepository: AuthRepository = mockk()
    
    private val registerUserUseCase = RegisterUserUseCase(authRepository)
    private val loginUseCase = LoginUseCase(authRepository)
    private val logoutUseCase = LogoutUseCase(authRepository)
    private val hasUsersUseCase = HasUsersUseCase(authRepository)
    private val getCurrentUserUseCase = GetCurrentUserUseCase(authRepository)
    private val getUserByUsernameUseCase = GetUserByUsernameUseCase(authRepository)
    private val getAllUsersUseCase = GetAllUsersUseCase(authRepository)
    private val seedDefaultDataUseCase: SeedDefaultDataUseCase = mockk()
    
    private val testDispatcher = StandardTestDispatcher()

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
        every { authRepository.getAllUsers() } returns flowOf(emptyList())
        coEvery { seedDefaultDataUseCase(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Base64::class)
    }

    private fun createViewModel(): AuthViewModel {
        val vm = AuthViewModel(
            registerUserUseCase,
            loginUseCase,
            logoutUseCase,
            hasUsersUseCase,
            getCurrentUserUseCase,
            getUserByUsernameUseCase,
            getAllUsersUseCase,
            seedDefaultDataUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `requires registration when there are no users yet`() = runTest {
        coEvery { authRepository.hasUsers() } returns false

        val viewModel = createViewModel()

        assertEquals(AuthUiState.RegistrationRequired, viewModel.uiState.value)
    }

    @Test
    fun `requires login when users exist but none is signed in`() = runTest {
        coEvery { authRepository.hasUsers() } returns true
        every { authRepository.getCurrentUser() } returns flowOf(null)

        val viewModel = createViewModel()

        assertEquals(AuthUiState.LoginRequired, viewModel.uiState.value)
    }

    @Test
    fun `is authenticated when a user is already signed in`() = runTest {
        val user = User(id = 5, username = "riyas", passwordHash = "hash", displayName = "Riyas")
        coEvery { authRepository.hasUsers() } returns true
        every { authRepository.getCurrentUser() } returns flowOf(user)

        val viewModel = createViewModel()

        assertEquals(AuthUiState.Authenticated(5L), viewModel.uiState.value)
    }

    @Test
    fun `register creates the account, seeds data, logs in and authenticates`() = runTest {
        coEvery { authRepository.hasUsers() } returns false
        every { authRepository.getCurrentUser() } returns flowOf(null)
        val viewModel = createViewModel()
        coEvery { authRepository.registerUser("riyas", any(), "Riyas") } returns 10L
        coEvery { authRepository.login(10L) } returns Unit

        viewModel.register("riyas", "password123", "Riyas")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthUiState.Authenticated(10L), viewModel.uiState.value)
        coVerify { seedDefaultDataUseCase(10L) }
        coVerify { authRepository.login(10L) }
    }

    @Test
    fun `register surfaces an error when registration fails`() = runTest {
        coEvery { authRepository.hasUsers() } returns false
        every { authRepository.getCurrentUser() } returns flowOf(null)
        val viewModel = createViewModel()
        coEvery { authRepository.registerUser(any(), any(), any()) } throws RuntimeException("username taken")

        viewModel.register("riyas", "password123", "Riyas")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AuthUiState.Error)
        assertTrue((state as AuthUiState.Error).message.contains("username taken"))
    }

    @Test
    fun `login authenticates with the correct password`() = runTest {
        coEvery { authRepository.hasUsers() } returns true
        every { authRepository.getCurrentUser() } returns flowOf(null)
        val viewModel = createViewModel()
        val storedHash = PasswordHasher.hashPassword("password123")
        val user = User(id = 1, username = "riyas", passwordHash = storedHash, displayName = "Riyas")
        coEvery { authRepository.getUserByUsername("riyas") } returns user
        coEvery { authRepository.login(1L) } returns Unit

        viewModel.login("riyas", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthUiState.Authenticated(1L), viewModel.uiState.value)
        coVerify { authRepository.login(1L) }
    }

    @Test
    fun `login fails with the wrong password`() = runTest {
        coEvery { authRepository.hasUsers() } returns true
        every { authRepository.getCurrentUser() } returns flowOf(null)
        val viewModel = createViewModel()
        val storedHash = PasswordHasher.hashPassword("password123")
        val user = User(id = 1, username = "riyas", passwordHash = storedHash, displayName = "Riyas")
        coEvery { authRepository.getUserByUsername("riyas") } returns user

        viewModel.login("riyas", "wrongPassword")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthUiState.Error("Invalid username or password"), viewModel.uiState.value)
    }

    @Test
    fun `login fails for an unknown username`() = runTest {
        coEvery { authRepository.hasUsers() } returns true
        every { authRepository.getCurrentUser() } returns flowOf(null)
        val viewModel = createViewModel()
        coEvery { authRepository.getUserByUsername("ghost") } returns null

        viewModel.login("ghost", "password123")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthUiState.Error("Invalid username or password"), viewModel.uiState.value)
    }

    @Test
    fun `logout clears the session and requires login again`() = runTest {
        val user = User(id = 1, username = "riyas", passwordHash = "hash", displayName = "Riyas")
        coEvery { authRepository.hasUsers() } returns true
        every { authRepository.getCurrentUser() } returns flowOf(user)
        val viewModel = createViewModel()
        coEvery { authRepository.logout() } returns Unit

        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(AuthUiState.LoginRequired, viewModel.uiState.value)
        coVerify { authRepository.logout() }
    }

    @Test
    fun `allUsers streams the repository's user list`() = runTest {
        coEvery { authRepository.hasUsers() } returns false
        every { authRepository.getCurrentUser() } returns flowOf(null)
        val user = User(id = 1, username = "riyas", passwordHash = "hash", displayName = "Riyas")
        every { authRepository.getAllUsers() } returns flowOf(listOf(user))
        val viewModel = createViewModel()

        viewModel.allUsers.test {
            assertEquals(emptyList<User>(), awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(listOf(user), awaitItem())
        }
    }
}
