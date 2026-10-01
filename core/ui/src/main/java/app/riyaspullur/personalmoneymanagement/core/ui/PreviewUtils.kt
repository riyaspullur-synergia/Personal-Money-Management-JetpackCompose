package app.riyaspullur.personalmoneymanagement.core.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Small Font",
    group = "Font Scales",
    fontScale = 0.85f,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = "Normal Font",
    group = "Font Scales",
    fontScale = 1.0f,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = "Large Font",
    group = "Font Scales",
    fontScale = 1.15f,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
@Preview(
    name = "Extra Large Font",
    group = "Font Scales",
    fontScale = 1.3f,
    showBackground = true,
    backgroundColor = 0xFFFFFFFF
)
annotation class FontScalePreviews

@Preview(
    name = "Light Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    group = "Themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
annotation class ThemePreviews

// One preview per supported app language, so translated text is checked for clipping/overlap
// and Arabic is checked for RTL mirroring directly in the Android Studio preview pane.
@Preview(name = "English", group = "Locales", locale = "en", showBackground = true)
@Preview(name = "Malayalam", group = "Locales", locale = "ml", showBackground = true)
@Preview(name = "Hindi", group = "Locales", locale = "hi", showBackground = true)
@Preview(name = "Tamil", group = "Locales", locale = "ta", showBackground = true)
@Preview(name = "Arabic (RTL)", group = "Locales", locale = "ar", showBackground = true)
annotation class LocalePreviews
