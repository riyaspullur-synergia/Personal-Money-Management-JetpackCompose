package app.riyaspullur.personalmoneymanagement.core.ui

import androidx.annotation.DrawableRes
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency

@DrawableRes
fun Currency.getIconResId(): Int? {
    return when (this) {
        Currency.AED -> R.drawable.icon_aed_symbol
        else -> null
    }
}
