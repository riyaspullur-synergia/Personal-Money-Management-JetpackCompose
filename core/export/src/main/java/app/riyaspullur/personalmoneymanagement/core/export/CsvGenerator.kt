package app.riyaspullur.personalmoneymanagement.core.export

import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.domain.model.Transaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class CsvGenerator @Inject constructor() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    fun generateFullReportCsv(file: File, report: MonthlyReport, title: String) {
        FileOutputStream(file).use { out ->
            // Summary Section
            out.write("Summary: $title\n\n".toByteArray())
            out.write("Total Income,${report.totalIncome.format(includeSymbol = false, useGrouping = false, locale = Locale.US)}\n".toByteArray())
            out.write("Total Expense,${report.totalExpense.format(includeSymbol = false, useGrouping = false, locale = Locale.US)}\n".toByteArray())
            out.write("Total Deposit,${report.totalDeposit.format(includeSymbol = false, useGrouping = false, locale = Locale.US)}\n".toByteArray())
            out.write("Total Investment,${report.totalInvestment.format(includeSymbol = false, useGrouping = false, locale = Locale.US)}\n".toByteArray())
            out.write("Net Balance,${report.netBalance.format(includeSymbol = false, useGrouping = false, locale = Locale.US)}\n\n".toByteArray())

            // Category Breakdown
            out.write("Category Breakdown\n".toByteArray())
            out.write("Category,Amount,Percentage\n".toByteArray())
            report.categoryBreakdown.forEach {
                val row = "${it.category?.name ?: "Other"},${it.amount.format(includeSymbol = false, useGrouping = false, locale = Locale.US)},${(it.percentage * 100).toInt()}%\n"
                out.write(row.toByteArray())
            }
            out.write("\n".toByteArray())

            // Transactions
            out.write("Transaction Details\n".toByteArray())
            val header = "Date,Type,Amount,Currency,Category,Merchant,Description,Status\n"
            out.write(header.toByteArray())

            report.transactions.forEach { tx ->
                val row = StringBuilder()
                row.append(dateFormat.format(Date(tx.transactionDate))).append(",")
                row.append(tx.type.name).append(",")
                row.append(Money(tx.amount, tx.currency).format(includeSymbol = false, useGrouping = false, locale = Locale.US)).append(",")
                row.append(tx.currency.code).append(",")
                row.append("\"${tx.categoryId ?: ""}\"").append(",") // Simplified for now
                row.append("\"${tx.merchant ?: ""}\"").append(",")
                row.append("\"${tx.description ?: ""}\"").append(",")
                row.append(tx.paymentStatus).append("\n")

                out.write(row.toString().toByteArray())
            }
        }
    }

    fun generateSampleTemplate(file: File) {
        FileOutputStream(file).use { out ->
            val header = "Date (yyyy-MM-dd HH:mm),Type (EXPENSE/INCOME/DEPOSIT/INVESTMENT/TRANSFER),Amount,Currency (AED/USD/INR),Account Name,Category,Merchant,Description,Status (CLEARED/PENDING)\n"
            out.write(header.toByteArray())
            
            val sampleRow = "${dateFormat.format(Date())},EXPENSE,50.00,AED,Cash,Food,Supermarket,Weekly groceries,CLEARED\n"
            out.write(sampleRow.toByteArray())
        }
    }

    fun generateTransactionsCsv(file: File, transactions: List<Transaction>) {
        FileOutputStream(file).use { out ->
            val header = "Date,Type,Amount,Currency,Description,Merchant,Status\n"
            out.write(header.toByteArray())

            transactions.forEach { tx ->
                val row = StringBuilder()
                row.append(dateFormat.format(Date(tx.transactionDate))).append(",")
                row.append(tx.type.name).append(",")
                row.append(Money(tx.amount, tx.currency).format(includeSymbol = false, useGrouping = false, locale = Locale.US)).append(",")
                row.append(tx.currency.code).append(",")
                row.append("\"${tx.description ?: ""}\"").append(",")
                row.append("\"${tx.merchant ?: ""}\"").append(",")
                row.append(tx.paymentStatus).append("\n")

                out.write(row.toString().toByteArray())
            }
        }
    }
}
