package app.riyaspullur.personalmoneymanagement.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AppLanguageTest {

    @Test
    fun `default language is English`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.Default)
        assertEquals("en", AppLanguage.Default.code)
    }

    @Test
    fun `fromCode maps stable codes to the expected language`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("en"))
        assertEquals(AppLanguage.MALAYALAM, AppLanguage.fromCode("ml"))
        assertEquals(AppLanguage.HINDI, AppLanguage.fromCode("hi"))
        assertEquals(AppLanguage.TAMIL, AppLanguage.fromCode("ta"))
        assertEquals(AppLanguage.ARABIC, AppLanguage.fromCode("ar"))
    }

    @Test
    fun `fromCode falls back to English for unknown or missing codes`() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("fr"))
    }

    @Test
    fun `every language exposes a stable code and a non-empty native display name`() {
        val expectedCodes = setOf("en", "ml", "hi", "ta", "ar")
        assertEquals(expectedCodes, AppLanguage.entries.map { it.code }.toSet())
        AppLanguage.entries.forEach { language ->
            assert(language.displayName.isNotBlank()) { "${language.name} must have a display name" }
        }
    }
}
