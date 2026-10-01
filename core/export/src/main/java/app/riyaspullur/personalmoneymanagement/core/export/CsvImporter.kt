package app.riyaspullur.personalmoneymanagement.core.export

import android.content.Context
import android.net.Uri
import app.riyaspullur.personalmoneymanagement.core.domain.model.RawTransaction
import app.riyaspullur.personalmoneymanagement.core.domain.repository.TransactionImporter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CsvImporter @Inject constructor(
    @ApplicationContext private val context: Context
) : TransactionImporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override suspend fun parseTransactions(source: Any): List<RawTransaction> {
        val uri = source as? Uri ?: return emptyList()
        val result = mutableListOf<RawTransaction>()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                val header = reader.readLine() // Skip header
                var line = reader.readLine()
                while (line != null) {
                    val tokens = line.split(",")
                    if (tokens.size >= 8) {
                        result.add(
                            RawTransaction(
                                date = tokens[0].trim(),
                                type = tokens[1].trim(),
                                amount = tokens[2].trim(),
                                currency = tokens[3].trim(),
                                accountName = tokens[4].trim(),
                                categoryName = tokens[5].trim(),
                                merchant = tokens[6].trim(),
                                description = tokens[7].trim(),
                                status = if (tokens.size > 8) tokens[8].trim() else "CLEARED"
                            )
                        )
                    }
                    line = reader.readLine()
                }
            }
        }
        return result
    }
}
