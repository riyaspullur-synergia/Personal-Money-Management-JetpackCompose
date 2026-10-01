package app.riyaspullur.personalmoneymanagement.core.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import app.riyaspullur.personalmoneymanagement.core.domain.model.Money
import app.riyaspullur.personalmoneymanagement.core.domain.model.MonthlyReport
import app.riyaspullur.personalmoneymanagement.core.ui.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class PdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val txDateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

    fun generateMonthlyReport(file: File, report: MonthlyReport, title: String) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        var y = 50f
        val margin = 50f
        val contentWidth = 595 - (margin * 2)

        // Header
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        canvas.drawText(context.getString(R.string.pdf_gen_report_title), margin, y, paint)
        y += 35f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        paint.color = Color.GRAY
        canvas.drawText(title, margin, y, paint)
        y += 45f

        // Draw Line
        paint.color = Color.LTGRAY
        canvas.drawLine(margin, y, margin + contentWidth, y, paint)
        y += 40f

        // Summary
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText(context.getString(R.string.pdf_gen_summary), margin, y, paint)
        y += 30f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        canvas.drawText(context.getString(R.string.pdf_gen_total_income), margin, y, paint)
        canvas.drawText(report.totalIncome.format(), margin + 150f, y, paint)
        y += 25f
        canvas.drawText(context.getString(R.string.pdf_gen_total_expense), margin, y, paint)
        canvas.drawText(report.totalExpense.format(), margin + 150f, y, paint)
        y += 25f

        if (report.totalDeposit.amount > 0) {
            canvas.drawText(context.getString(R.string.common_deposit), margin, y, paint)
            canvas.drawText(report.totalDeposit.format(), margin + 150f, y, paint)
            y += 25f
        }

        if (report.totalInvestment.amount > 0) {
            canvas.drawText(context.getString(R.string.common_investment), margin, y, paint)
            canvas.drawText(report.totalInvestment.format(), margin + 150f, y, paint)
            y += 25f
        }
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(context.getString(R.string.pdf_gen_net_balance), margin, y, paint)
        canvas.drawText(report.netBalance.format(), margin + 150f, y, paint)
        y += 50f

        // Category Breakdown
        canvas.drawText(context.getString(R.string.pdf_gen_category_breakdown), margin, y, paint)
        y += 30f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        report.categoryBreakdown.forEach { breakdown ->
            val label = breakdown.category?.name ?: context.getString(R.string.pdf_gen_other_category)
            canvas.drawText(label, margin + 20f, y, paint)
            canvas.drawText(breakdown.amount.format(), margin + 150f, y, paint)
            canvas.drawText("(${(breakdown.percentage * 100).toInt()}%)", margin + 280f, y, paint)
            y += 25f
        }
        y += 30f

        // Recent Transactions
        if (report.transactions.isNotEmpty()) {
            if (y > 700) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(context.getString(R.string.pdf_gen_recent_transactions), margin, y, paint)
            y += 30f

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(context.getString(R.string.pdf_gen_date_header), margin, y, paint)
            canvas.drawText(context.getString(R.string.pdf_gen_description_header), margin + 70f, y, paint)
            canvas.drawText(context.getString(R.string.pdf_gen_amount_header), margin + 300f, y, paint)
            y += 10f
            canvas.drawLine(margin, y, margin + contentWidth, y, paint)
            y += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            report.transactions.take(20).forEach { tx ->
                if (y > 800) {
                    document.finishPage(page)
                    page = document.startPage(pageInfo)
                    canvas = page.canvas
                    y = 50f
                }
                canvas.drawText(txDateFormat.format(Date(tx.transactionDate)), margin, y, paint)
                canvas.drawText(tx.description ?: tx.type.name, margin + 70f, y, paint)
                canvas.drawText(Money(tx.amount, tx.currency).format(), margin + 300f, y, paint)
                y += 20f
            }
        }

        document.finishPage(page)
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
    }
}
