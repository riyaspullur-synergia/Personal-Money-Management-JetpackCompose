package app.riyaspullur.personalmoneymanagement.core.util

/**
 * Supported in-app languages. [code] is the stable persisted/BCP-47 identifier;
 * [displayName] is the language's own endonym and is intentionally shown the same
 * way regardless of the currently active app language (matches platform language pickers).
 */
enum class AppLanguage(val code: String, val displayName: String, val isRtl: Boolean = false) {
    ENGLISH("en", "English"),
    MALAYALAM("ml", "മലയാളം"),
    HINDI("hi", "हिन्दी"),
    TAMIL("ta", "தமிழ்"),
    ARABIC("ar", "العربية", isRtl = true);

    companion object {
        val Default = ENGLISH

        fun fromCode(code: String?): AppLanguage = entries.firstOrNull { it.code == code } ?: Default
    }
}
