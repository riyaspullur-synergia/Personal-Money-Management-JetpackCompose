package app.riyaspullur.personalmoneymanagement.core.database

import androidx.room.TypeConverter
import app.riyaspullur.personalmoneymanagement.core.domain.model.AccountType
import app.riyaspullur.personalmoneymanagement.core.domain.model.Currency
import app.riyaspullur.personalmoneymanagement.core.domain.model.TransactionType

class Converters {
    @TypeConverter
    fun fromCurrency(currency: Currency): String = currency.code

    @TypeConverter
    fun toCurrency(code: String): Currency = Currency.fromCode(code)

    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(name: String): TransactionType = TransactionType.valueOf(name)

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(name: String): AccountType = AccountType.valueOf(name)
}
