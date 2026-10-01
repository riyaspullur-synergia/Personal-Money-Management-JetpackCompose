package app.riyaspullur.personalmoneymanagement.core.util

import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import app.riyaspullur.personalmoneymanagement.core.util.AppLocaleManager.applyToSystem
import app.riyaspullur.personalmoneymanagement.core.util.AppLocaleManager.localizedContext
import app.riyaspullur.personalmoneymanagement.core.util.AppLocaleManager.wrap
import java.util.Locale

/**
 * Applies the persisted [AppPreferencesDataSource] language choice to the process.
 *
 * MainActivity is a plain FragmentActivity, not AppCompatActivity, so
 * AppCompatDelegate.setApplicationLocales (which only takes effect through an installed
 * AppCompatActivity delegate) cannot be used here, and - verified on-device - a live platform
 * LocaleManager change alone does not make a running Compose tree re-resolve stringResource()
 * or LocalLayoutDirection without an activity recreation. So the app takes two, independent
 * paths instead of relying on Activity/Configuration plumbing at all:
 * - [wrap] locale-wraps the base Context for [MainActivity.attachBaseContext]
 *   [app.riyaspullur.personalmoneymanagement.MainActivity], so a cold start renders in the
 *   persisted language from the very first frame (including system-level RTL for the window).
 * - Live, in-session switches are handled entirely at the Compose level: AppNavigation wraps
 *   its content in a CompositionLocalProvider keyed on the current language, overriding
 *   LocalContext (via [localizedContext]) / LocalLayoutDirection for the whole tree - no activity
 *   recreation, no flash. [localizedContext] wraps the real Activity rather than replacing it
 *   (only getResources() is overridden) specifically because Hilt's hiltViewModel() walks the
 *   LocalContext ContextWrapper chain looking for the Activity - a plain
 *   createConfigurationContext() result breaks that chain and crashes every hiltViewModel() call
 *   below it in the tree (confirmed on-device).
 * - [applyToSystem] only keeps the OS's own per-app language setting (API 33+) in sync with the
 *   choice; MainActivity declares android:configChanges="locale|layoutDirection" so this never
 *   triggers a redundant system-driven recreate on top of the Compose-level switch.
 */
object AppLocaleManager {

    @Volatile
    var currentLanguageCode: String = AppLanguage.Default.code
        private set

    fun updateCurrentLanguage(code: String) {
        currentLanguageCode = AppLanguage.fromCode(code).code
    }

    /** Pushes [code] to the platform LocaleManager (API 33+) so OS-level surfaces stay in sync. */
    fun applyToSystem(context: Context, code: String) {
        updateCurrentLanguage(code)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)?.applicationLocales =
                LocaleList.forLanguageTags(currentLanguageCode)
        }
    }

    fun wrap(base: Context): Context = configurationContext(base, currentLanguageCode)

    /** A Context whose Resources resolve strings/layout direction for [code], independent of [base]'s. */
    private fun configurationContext(base: Context, code: String): Context {
        Locale.setDefault(localeFor(code))
        return base.createConfigurationContext(configurationFor(base, code))
    }

    /**
     * A Context wrapping the real [base] (Activity) that only overrides getResources(), so any
     * code that unwraps ContextWrapper.baseContext looking for the Activity - hiltViewModel(),
     * LocalActivity, permission launchers, etc. - still finds it, while stringResource() and
     * friends (which read LocalContext.current.resources) resolve against [code]'s locale.
     */
    fun localizedContext(base: Context, code: String): Context {
        Locale.setDefault(localeFor(code))
        val localizedResources = base.createConfigurationContext(configurationFor(base, code)).resources
        return object : ContextWrapper(base) {
            override fun getResources(): Resources = localizedResources
        }
    }

    private fun localeFor(code: String): Locale = Locale.forLanguageTag(code)

    private fun configurationFor(base: Context, code: String): Configuration {
        val locale = localeFor(code)
        val configuration = Configuration(base.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return configuration
    }
}
