package com.kasirkoperasi.app.core.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.kasirkoperasi.app.core.settings.StoreProfileStore
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransactionReportPdfData(
    val periodLabel: String,
    val transactions: List<SalesTransaction>,
    val itemsByTransactionId: Map<Long, List<SalesTransactionItem>>,
    val exportedAtMillis: Long = System.currentTimeMillis(),
)

class TransactionReportPdfExporter(
    private val context: Context,
) {
    fun export(data: TransactionReportPdfData): Uri {
        require(data.transactions.isNotEmpty()) { "Tidak ada transaksi untuk diexport" }

        val document = PdfDocument()
        val renderer = PdfRenderer(
            document = document,
            storeName = StoreProfileStore.load(context).storeName,
        )
        renderer.render(data)

        val directory = File(context.cacheDir, REPORT_DIRECTORY).apply {
            mkdirs()
        }
        val fileName = "laporan-transaksi-${data.exportedAtMillis}.pdf"
        val file = File(directory, fileName)

        FileOutputStream(file).use { output ->
            document.writeTo(output)
        }
        document.close()

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    private companion object {
        const val REPORT_DIRECTORY = "reports"
    }
}

private class PdfRenderer(
    private val document: PdfDocument,
    private val storeName: String,
) {
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(7, 84, 54)
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 115, 109)
        textSize = 10f
    }
    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 11f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 8.5f
    }
    private val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(7, 84, 54)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(199, 210, 202)
        strokeWidth = 0.8f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var pageNumber = 0
    private lateinit var page: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var y = 0f

    fun render(data: TransactionReportPdfData) {
        startPage()
        drawReportHeader(data)
        drawSummary(data)
        drawTableHeader()

        var rowNumber = 1
        data.transactions.forEach { transaction ->
            val items = data.itemsByTransactionId[transaction.id].orEmpty()
            if (items.isEmpty()) {
                drawRow(
                    rowNumber = rowNumber,
                    transaction = transaction,
                    item = null,
                )
                rowNumber += 1
            } else {
                items.forEach { item ->
                    drawRow(
                        rowNumber = rowNumber,
                        transaction = transaction,
                        item = item,
                    )
                    rowNumber += 1
                }
            }
        }

        finishPage()
    }

    private fun startPage() {
        pageNumber += 1
        page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create(),
        )
        canvas = page.canvas
        y = MARGIN_TOP
    }

    private fun finishPage() {
        drawFooter()
        document.finishPage(page)
    }

    private fun drawReportHeader(data: TransactionReportPdfData) {
        canvas.drawText(storeName, MARGIN_LEFT, y, titlePaint)
        y += 16f
        canvas.drawText("Laporan Rincian Transaksi", MARGIN_LEFT, y, sectionPaint)
        y += 14f
        canvas.drawText("Periode: ${data.periodLabel}", MARGIN_LEFT, y, subtitlePaint)
        canvas.drawText("Export: ${data.exportedAtMillis.toDateTime()}", PAGE_WIDTH - MARGIN_RIGHT - 150f, y, subtitlePaint)
        y += 18f
        drawDivider()
        y += 12f
    }

    private fun drawSummary(data: TransactionReportPdfData) {
        val totalSales = data.transactions.sumOf { it.totalAmount }
        val totalProfit = data.transactions.sumOf { it.totalProfit }
        val totalItems = data.transactions.sumOf { it.itemCount }
        val totalTransactions = data.transactions.size

        drawSummaryBox(
            x = MARGIN_LEFT,
            title = "Total Penjualan",
            value = totalSales.toRupiah(),
        )
        drawSummaryBox(
            x = MARGIN_LEFT + 190f,
            title = "Total Profit",
            value = totalProfit.toRupiah(),
        )
        drawSummaryBox(
            x = MARGIN_LEFT + 380f,
            title = "Transaksi",
            value = totalTransactions.toString(),
        )
        drawSummaryBox(
            x = MARGIN_LEFT + 570f,
            title = "Item Terjual",
            value = totalItems.toString(),
        )
        y += 54f
    }

    private fun drawSummaryBox(
        x: Float,
        title: String,
        value: String,
    ) {
        fillPaint.color = Color.rgb(247, 248, 247)
        canvas.drawRoundRect(x, y, x + 170f, y + 42f, 8f, 8f, fillPaint)
        canvas.drawText(title, x + 10f, y + 16f, subtitlePaint)
        canvas.drawText(value, x + 10f, y + 32f, sectionPaint)
    }

    private fun drawTableHeader() {
        ensureSpace(TABLE_HEADER_HEIGHT + ROW_HEIGHT)

        fillPaint.color = Color.rgb(237, 238, 238)
        canvas.drawRoundRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + TABLE_HEADER_HEIGHT, 6f, 6f, fillPaint)

        var x = MARGIN_LEFT + CELL_PADDING
        TABLE_COLUMNS.forEach { column ->
            canvas.drawText(column.title, x, y + 15f, headerPaint)
            x += column.width
        }
        y += TABLE_HEADER_HEIGHT
    }

    private fun drawRow(
        rowNumber: Int,
        transaction: SalesTransaction,
        item: SalesTransactionItem?,
    ) {
        ensureSpace(ROW_HEIGHT)

        if (rowNumber % 2 == 0) {
            fillPaint.color = Color.rgb(252, 252, 252)
            canvas.drawRect(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y + ROW_HEIGHT, fillPaint)
        }

        val values = listOf(
            rowNumber.toString(),
            transaction.createdAtMillis.toShortDateTime(),
            transaction.transactionNumber,
            transaction.buyerName.ifBlank { "Pembeli Umum" },
            item?.productName ?: "-",
            item?.let { "${it.quantity} ${it.unit}" } ?: "-",
            item?.sellingPrice?.toRupiah() ?: "-",
            item?.subtotal?.toRupiah() ?: transaction.totalAmount.toRupiah(),
            item?.profit?.toRupiah() ?: transaction.totalProfit.toRupiah(),
            transaction.paymentMethod,
        )

        var x = MARGIN_LEFT + CELL_PADDING
        TABLE_COLUMNS.forEachIndexed { index, column ->
            val text = values[index].ellipsize(column.width - (CELL_PADDING * 2), bodyPaint)
            canvas.drawText(text, x, y + 16f, if (index == 7 || index == 8) boldBodyPaint else bodyPaint)
            x += column.width
        }

        canvas.drawLine(MARGIN_LEFT, y + ROW_HEIGHT, PAGE_WIDTH - MARGIN_RIGHT, y + ROW_HEIGHT, linePaint)
        y += ROW_HEIGHT
    }

    private fun ensureSpace(requiredHeight: Float) {
        if (y + requiredHeight <= PAGE_HEIGHT - MARGIN_BOTTOM) return

        finishPage()
        startPage()
        drawTableHeader()
    }

    private fun drawDivider() {
        canvas.drawLine(MARGIN_LEFT, y, PAGE_WIDTH - MARGIN_RIGHT, y, linePaint)
    }

    private fun drawFooter() {
        val footerY = PAGE_HEIGHT - 20f
        canvas.drawLine(MARGIN_LEFT, footerY - 12f, PAGE_WIDTH - MARGIN_RIGHT, footerY - 12f, linePaint)
        canvas.drawText("$storeName - Pembukuan Transaksi", MARGIN_LEFT, footerY, subtitlePaint)
        canvas.drawText("Halaman $pageNumber", PAGE_WIDTH - MARGIN_RIGHT - 55f, footerY, subtitlePaint)
    }

    private data class TableColumn(
        val title: String,
        val width: Float,
    )

    private companion object {
        const val PAGE_WIDTH = 842
        const val PAGE_HEIGHT = 595
        const val MARGIN_LEFT = 28f
        const val MARGIN_RIGHT = 28f
        const val MARGIN_TOP = 30f
        const val MARGIN_BOTTOM = 36f
        const val CELL_PADDING = 5f
        const val TABLE_HEADER_HEIGHT = 22f
        const val ROW_HEIGHT = 24f

        val TABLE_COLUMNS = listOf(
            TableColumn("No", 28f),
            TableColumn("Tanggal", 76f),
            TableColumn("No Transaksi", 112f),
            TableColumn("Pembeli", 82f),
            TableColumn("Barang", 134f),
            TableColumn("Qty", 45f),
            TableColumn("Harga", 70f),
            TableColumn("Subtotal", 80f),
            TableColumn("Profit", 68f),
            TableColumn("Bayar", 70f),
        )
    }
}

private fun String.ellipsize(
    maxWidth: Float,
    paint: Paint,
): String {
    if (paint.measureText(this) <= maxWidth) return this

    var result = this
    while (result.length > 1 && paint.measureText("$result...") > maxWidth) {
        result = result.dropLast(1)
    }

    return "$result..."
}

private fun Long.toRupiah(): String {
    val grouped = toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "Rp$grouped"
}

private fun Long.toDateTime(): String {
    return SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(Date(this))
}

private fun Long.toShortDateTime(): String {
    return SimpleDateFormat("dd/MM/yy HH:mm", Locale.forLanguageTag("id-ID")).format(Date(this))
}
