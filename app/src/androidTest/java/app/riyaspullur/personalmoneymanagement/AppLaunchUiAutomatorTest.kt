package app.riyaspullur.personalmoneymanagement

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Black-box UiAutomator test: drives the installed app through its real launcher entry point
 * (not hosted in-process like the other androidTest classes), exercising the real Hilt/Room/
 * Navigation wiring end to end. App data persists across runs on the same device/emulator, so
 * whether registration is required depends on prior runs - the registration flow is only
 * exercised when that screen is actually showing; otherwise it's skipped via Assume rather than
 * forced, since there is no in-test way to reset the on-device database without killing the
 * instrumentation process itself (test and app share the same process here).
 */
@RunWith(AndroidJUnit4::class)
class AppLaunchUiAutomatorTest {

    private val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressHome()
    }

    private fun launchApp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), 10_000)
    }

    // BySelector has no OR combinator across distinct text values, so poll for any match instead.
    private fun waitForAnyText(texts: List<String>, timeoutMs: Long): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        do {
            if (texts.any { device.hasObject(By.text(it)) }) return true
            Thread.sleep(200)
        } while (System.currentTimeMillis() < deadline)
        return texts.any { device.hasObject(By.text(it)) }
    }

    @Test
    fun appLaunchesWithoutCrashingToARecognizedDestination() {
        launchApp()

        val reachedKnownScreen = waitForAnyText(
            listOf("Create your account", "Welcome back", "App Locked", "Net Worth"),
            timeoutMs = 15_000
        )

        assertTrue("App did not reach a recognized screen after launch", reachedKnownScreen)
    }

    @Test
    fun registrationFlowReachesDashboardWhenRegistrationIsRequired() {
        launchApp()
        device.wait(Until.hasObject(By.text("Create your account")), 10_000)

        assumeTrue(
            "Registration screen not shown (a user already exists from a prior run on this device)",
            device.hasObject(By.text("Create your account"))
        )

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device.findObject(By.text("Display Name")).click()
        instrumentation.sendStringSync("UI Automator")
        device.pressBack() // dismiss the IME - MainActivity is adjustResize, so it hides the next field otherwise
        device.findObject(By.text("Username")).click()
        instrumentation.sendStringSync("ui_automator_${System.nanoTime()}")
        device.pressBack()
        device.findObject(By.text("Password")).click()
        instrumentation.sendStringSync("TestPass123")
        device.pressBack()
        device.findObject(By.text("Create Account")).click()

        assertTrue(
            "Expected to land on the dashboard after registering",
            device.wait(Until.hasObject(By.text("Net Worth")), 30_000)
        )
    }
}
