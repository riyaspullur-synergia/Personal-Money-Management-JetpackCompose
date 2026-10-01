package app.riyaspullur.personalmoneymanagement.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetLanguageUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.GetThemeModeUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.IsAppLockEnabledUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SetAppLockEnabledUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SetLanguageUseCase
import app.riyaspullur.personalmoneymanagement.core.domain.usecase.SetThemeModeUseCase
import app.riyaspullur.personalmoneymanagement.core.util.AppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val isAppLockEnabledUseCase: IsAppLockEnabledUseCase,
    private val getThemeModeUseCase: GetThemeModeUseCase,
    private val getLanguageUseCase: GetLanguageUseCase,
    private val setAppLockEnabledUseCase: SetAppLockEnabledUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setLanguageUseCase: SetLanguageUseCase
) : ViewModel() {

    val isAppLockEnabled: StateFlow<Boolean> = isAppLockEnabledUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = getThemeModeUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val language: StateFlow<String> = getLanguageUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.Default.code)

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            setAppLockEnabledUseCase(enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            setThemeModeUseCase(mode)
        }
    }

    fun setLanguage(code: String) {
        viewModelScope.launch {
            setLanguageUseCase(code)
        }
    }
}
