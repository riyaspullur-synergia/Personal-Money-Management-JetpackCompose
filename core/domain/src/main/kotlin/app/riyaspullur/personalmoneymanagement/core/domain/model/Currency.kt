package app.riyaspullur.personalmoneymanagement.core.domain.model

enum class Currency(
    val code: String,
    val symbol: String,
    val minorUnits: Int
) {
    AED("AED", "AED", 2),
    USD("USD", "$", 2),
    INR("INR", "₹", 2),
    EUR("EUR", "€", 2);

    companion object {
        fun fromCode(code: String): Currency = entries.find { it.code == code } ?: AED
    }
}
