package app.riyaspullur.personalmoneymanagement

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import app.riyaspullur.personalmoneymanagement.core.navigation.AppNavigation
import app.riyaspullur.personalmoneymanagement.core.util.AppLocaleManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    // MainActivity is a plain FragmentActivity, not AppCompatActivity, so the persisted
    // app language must be applied by wrapping the base Context directly (AppCompatDelegate's
    // per-app-language support only takes effect through an installed AppCompatActivity delegate).
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrap(newBase))
    }

    // Declared in the manifest (configChanges="locale|layoutDirection") so the system delivers
    // language switches here instead of destroying/recreating the activity - Compose's own
    // configuration-aware recomposition (stringResource, LocalLayoutDirection) picks up the
    // change with no flash. super() is enough; nothing app-specific to do here.
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // savedInstanceState is non-null when this onCreate is a recreate, not a genuine cold
        // start (language switches no longer recreate the activity at all, but this stays as a
        // guard for any other config change that does). The platform doesn't paint the splash
        // branding for a relaunch, so holding it here would just be an artificial black delay.
        var keepSplashScreen = savedInstanceState == null
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        if (savedInstanceState == null) {
            lifecycleScope.launch {
                delay(1000.milliseconds)
                keepSplashScreen = false
            }
        }

        // Security: Prevent screenshots
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }
}
