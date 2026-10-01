package app.riyaspullur.personalmoneymanagement

import android.app.Application
import app.riyaspullur.personalmoneymanagement.core.datastore.AppPreferencesDataSource
import app.riyaspullur.personalmoneymanagement.core.util.AppLocaleManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class MoneyManagementApp : Application() {

    @Inject
    lateinit var appPreferencesDataSource: AppPreferencesDataSource

    override fun onCreate() {
        super.onCreate()
        // Read synchronously before any Activity is created so MainActivity.attachBaseContext
        // (and, on API 33+, the platform LocaleManager) already see the persisted language for
        // the very first frame, avoiding an English->language flicker on cold start.
        val languageCode = runBlocking { appPreferencesDataSource.language.first() }
        AppLocaleManager.applyToSystem(this, languageCode)
    }
}
