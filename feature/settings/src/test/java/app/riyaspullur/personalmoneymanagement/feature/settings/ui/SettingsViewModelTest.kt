package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import app.cash.turbine.test
import app.riyaspullur.personalmoneymanagement.core.domain.repository.SettingsRepository
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetLanguageUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetThemeModeUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.IsAppLockEnabledUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SetAppLockEnabledUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SetLanguageUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SetThemeModeUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsRepository: SettingsRepository = mockk()
    
    private val isAppLockEnabledUseCase = IsAppLockEnabledUseCase(settingsRepository)
    private val getThemeModeUseCase = GetThemeModeUseCase(settingsRepository)
    private val getLanguageUseCase = GetLanguageUseCase(settingsRepository)
    private val setAppLockEnabledUseCase = SetAppLockEnabledUseCase(settingsRepository)
    private val setThemeModeUseCase = SetThemeModeUseCase(settingsRepository)
    private val setLanguageUseCase = SetLanguageUseCase(settingsRepository)
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        val vm = SettingsViewModel(
            isAppLockEnabledUseCase,
            getThemeModeUseCase,
            getLanguageUseCase,
            setAppLockEnabledUseCase,
            setThemeModeUseCase,
            setLanguageUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()
        return vm
    }

    @Test
    fun `isAppLockEnabled streams the stored preference`() = runTest {
        every { settingsRepository.isAppLockEnabled } returns flowOf(true)
        every { settingsRepository.themeMode } returns flowOf("SYSTEM")
        every { settingsRepository.language } returns flowOf("en")
        val viewModel = createViewModel()

        viewModel.isAppLockEnabled.test {
            assertEquals(false, awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals(true, awaitItem())
        }
    }

    @Test
    fun `themeMode streams the stored preference`() = runTest {
        every { settingsRepository.isAppLockEnabled } returns flowOf(false)
        every { settingsRepository.themeMode } returns flowOf("DARK")
        every { settingsRepository.language } returns flowOf("en")
        val viewModel = createViewModel()

        viewModel.themeMode.test {
            assertEquals("SYSTEM", awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("DARK", awaitItem())
        }
    }

    @Test
    fun `setAppLockEnabled persists the new value`() = runTest {
        every { settingsRepository.isAppLockEnabled } returns flowOf(false)
        every { settingsRepository.themeMode } returns flowOf("SYSTEM")
        every { settingsRepository.language } returns flowOf("en")
        val viewModel = createViewModel()
        coEvery { settingsRepository.setAppLockEnabled(true) } returns Unit

        viewModel.setAppLockEnabled(true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setAppLockEnabled(true) }
    }

    @Test
    fun `setThemeMode persists the new value`() = runTest {
        every { settingsRepository.isAppLockEnabled } returns flowOf(false)
        every { settingsRepository.themeMode } returns flowOf("SYSTEM")
        every { settingsRepository.language } returns flowOf("en")
        val viewModel = createViewModel()
        coEvery { settingsRepository.setThemeMode("DARK") } returns Unit

        viewModel.setThemeMode("DARK")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setThemeMode("DARK") }
    }

    @Test
    fun `language streams the stored preference`() = runTest {
        every { settingsRepository.isAppLockEnabled } returns flowOf(false)
        every { settingsRepository.themeMode } returns flowOf("SYSTEM")
        every { settingsRepository.language } returns flowOf("ml")
        val viewModel = createViewModel()

        viewModel.language.test {
            assertEquals("en", awaitItem())
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("ml", awaitItem())
        }
    }

    @Test
    fun `setLanguage persists the new value`() = runTest {
        every { settingsRepository.isAppLockEnabled } returns flowOf(false)
        every { settingsRepository.themeMode } returns flowOf("SYSTEM")
        every { settingsRepository.language } returns flowOf("en")
        val viewModel = createViewModel()
        coEvery { settingsRepository.setLanguage("ar") } returns Unit

        viewModel.setLanguage("ar")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { settingsRepository.setLanguage("ar") }
    }
}
