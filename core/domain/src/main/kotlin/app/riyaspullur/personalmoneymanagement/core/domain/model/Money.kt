package app.riyaspullur.personalmoneymanagement.core.domain.model

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

data class Money(
    val amount: Long, // Amount in minor units (e.g., fils, cents)
    val currency: Currency = Currency.AED
) {
    fun toBigDecimal(): BigDecimal {
        return BigDecimal(amount).movePointLeft(currency.minorUnits)
    }

    fun format(
        includeSymbol: Boolean = true,
        useGrouping: Boolean = true,
        locale: Locale = Locale.getDefault()
    ): String {
        return if (includeSymbol) {
            val format = NumberFormat.getCurrencyInstance(locale)
            try {
                format.currency = java.util.Currency.getInstance(currency.code)
            } catch (e: Exception) {
                // Fallback if currency code is not supported by platform
            }
            format.isGroupingUsed = useGrouping
            format.format(toBigDecimal())
        } else {
            val format = NumberFormat.getNumberInstance(locale)
            format.minimumFractionDigits = currency.minorUnits
            format.maximumFractionDigits = currency.minorUnits
            format.isGroupingUsed = useGrouping
            format.format(toBigDecimal())
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add different currencies: ${currency.code} and ${other.currency.code}" }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "Cannot subtract different currencies: ${currency.code} and ${other.currency.code}" }
        return Money(amount - other.amount, currency)
    }

    operator fun times(factor: Double): Money {
        return Money((amount * factor).toLong(), currency)
    }

    companion object {
        fun zero(currency: Currency = Currency.AED) = Money(0L, currency)
        
        fun fromMajorUnits(amount: Double, currency: Currency = Currency.AED): Money {
            val minorUnits = (amount * 10.0.pow(currency.minorUnits)).toLong()
            return Money(minorUnits, currency)
        }
        
        fun fromMajorUnits(amount: String, currency: Currency = Currency.AED): Money {
            val decimal = BigDecimal(amount).movePointRight(currency.minorUnits)
            return Money(decimal.toLong(), currency)
        }
    }
}
