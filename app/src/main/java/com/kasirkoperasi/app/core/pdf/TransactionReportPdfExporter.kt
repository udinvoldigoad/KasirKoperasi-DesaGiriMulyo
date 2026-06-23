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
import com.kasirkoperasi.app.domain.model.DebtCustomerSummary
import com.kasirkoperasi.app.domain.model.DebtPayment
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.model.StockMovement
import com.kasirkoperasi.app.domain.model.StockMovementType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TransactionReportPdfData(
    val periodLabel: String,
    val transactions: List<SalesTransaction>,
    val itemsByTransactionId: Map<Long, List<SalesTransactionItem>>,
    val stockProducts: List<Product>,
    val debtPayments: List<DebtPayment>,
    val debtCustomers: List<DebtCustomerSummary>,
    val stockMovements: List<StockMovement>,
    val exportedAtMillis: Long = System.currentTimeMillis(),
)

class TransactionReportPdfExporter(
    private val context: Context,
) {
    fun export(data: TransactionReportPdfData): Uri {
        require(
            data.transactions.isNotEmpty() ||
                data.stockProducts.isNotEmpty() ||
                data.debtPayments.isNotEmpty() ||
                data.stockMovements.isNotEmpty(),
        ) {
            "Tidak ada data untuk diexport"
        }

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
        textSize = 10.5f
    }
    private val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 9.8f
    }
    private val boldBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 9.8f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(7, 84, 54)
        textSize = 9.8f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(184, 197, 188)
        strokeWidth = 0.8f
        style = Paint.Style.STROKE
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
        drawTransactionSection(data)
        drawDebtTransactionSection(data.transactions.filter { it.debtAmount > 0L })
        drawDebtPaymentSection(data.debtPayments)
        drawDebtCustomerSection(data.debtCustomers)
        drawStockMovementSection(
            stockMovements = data.stockMovements,
            productNameById = data.stockProducts.associate { it.id to it.name },
        )
        drawStockSection(data.stockProducts)

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
        val totalCash = data.transactions.sumOf { transaction ->
            if (transaction.paidPaymentMethod.equals("Cash", ignoreCase = true)) {
                    transaction.paidAmount - transaction.changeAmount
            } else {
                0L
            }
        } + data.debtPayments
            .filter { it.paymentMethod.equals("Cash", ignoreCase = true) }
            .sumOf { it.amount }
        val totalQris = data.transactions
            .filter { it.paidPaymentMethod.equals("QRIS", ignoreCase = true) }
            .sumOf { it.paidAmount } + data.debtPayments
            .filter { it.paymentMethod.equals("QRIS", ignoreCase = true) }
            .sumOf { it.amount }
        val totalDebt = data.transactions.sumOf { it.debtAmount }
        val totalDebtPayment = data.debtPayments.sumOf { it.amount }

        drawSummaryBox(
            x = MARGIN_LEFT,
            width = SUMMARY_BOX_WIDTH,
            title = "Total Penjualan",
            value = totalSales.toRupiah(),
        )
        drawSummaryBox(
            x = MARGIN_LEFT + SUMMARY_BOX_WIDTH + SUMMARY_BOX_GAP,
            width = SUMMARY_BOX_WIDTH,
            title = "Total Profit",
            value = totalProfit.toRupiah(),
        )
        drawSummaryBox(
            x = MARGIN_LEFT + ((SUMMARY_BOX_WIDTH + SUMMARY_BOX_GAP) * 2),
            width = SUMMARY_BOX_WIDTH,
            title = "Item Terjual",
            value = totalItems.toString(),
        )
        drawSummaryBox(
            x = MARGIN_LEFT,
            width = SUMMARY_BOX_WIDTH,
            title = "Total Cash",
            value = totalCash.toRupiah(),
            yOffset = 50f,
        )
        drawSummaryBox(
            x = MARGIN_LEFT + SUMMARY_BOX_WIDTH + SUMMARY_BOX_GAP,
            width = SUMMARY_BOX_WIDTH,
            title = "Total QRIS",
            value = totalQris.toRupiah(),
            yOffset = 50f,
        )
        drawSummaryBox(
            x = MARGIN_LEFT + ((SUMMARY_BOX_WIDTH + SUMMARY_BOX_GAP) * 2),
            width = SUMMARY_BOX_WIDTH,
            title = "Hutang / Pelunasan",
            value = "${totalDebt.toRupiah()} / ${totalDebtPayment.toRupiah()}",
            yOffset = 50f,
        )
        y += 104f
    }

    private fun drawSummaryBox(
        x: Float,
        width: Float,
        title: String,
        value: String,
        yOffset: Float = 0f,
    ) {
        val top = y + yOffset
        fillPaint.color = Color.rgb(247, 248, 247)
        canvas.drawRoundRect(x, top, x + width, top + 42f, 8f, 8f, fillPaint)
        canvas.drawText(title, x + 10f, top + 16f, subtitlePaint)
        canvas.drawText(value.ellipsize(width - 20f, sectionPaint), x + 10f, top + 32f, sectionPaint)
    }

    private fun drawTransactionSection(data: TransactionReportPdfData) {
        drawSectionTitle("Rekapan Transaksi")
        drawTransactionTableHeader()

        if (data.transactions.isEmpty()) {
            drawEmptyTableMessage("Belum ada transaksi pada periode ini.")
            y += 10f
            return
        }

        var rowNumber = 1
        data.transactions.forEach { transaction ->
            val items = data.itemsByTransactionId[transaction.id].orEmpty()
            if (items.isEmpty()) {
                drawTransactionRow(
                    rowNumber = rowNumber,
                    transaction = transaction,
                    item = null,
                )
                rowNumber += 1
            } else {
                items.forEach { item ->
                    drawTransactionRow(
                        rowNumber = rowNumber,
                        transaction = transaction,
                        item = item,
                    )
                    rowNumber += 1
                }
            }
        }

        y += 18f
    }

    private fun drawStockSection(products: List<Product>) {
        drawSectionTitle("Laporan Stok Saat Ini")
        drawStockTableHeader()

        if (products.isEmpty()) {
            drawEmptyTableMessage("Belum ada data barang.")
            return
        }

        products.forEachIndexed { index, product ->
            drawStockRow(
                rowNumber = index + 1,
                product = product,
            )
        }
    }

    private fun drawDebtTransactionSection(transactions: List<SalesTransaction>) {
        drawSectionTitle("Daftar Transaksi Hutang")
        drawDebtTransactionTableHeader()

        if (transactions.isEmpty()) {
            drawEmptyTableMessage("Belum ada transaksi hutang pada periode ini.")
            y += 10f
            return
        }

        transactions.forEachIndexed { index, transaction ->
            drawDebtTransactionRow(
                rowNumber = index + 1,
                transaction = transaction,
            )
        }

        y += 18f
    }

    private fun drawDebtPaymentSection(payments: List<DebtPayment>) {
        drawSectionTitle("Daftar Pembayaran Hutang")
        drawDebtPaymentTableHeader()

        if (payments.isEmpty()) {
            drawEmptyTableMessage("Belum ada pembayaran hutang pada periode ini.")
            y += 10f
            return
        }

        payments.forEachIndexed { index, payment ->
            drawDebtPaymentRow(
                rowNumber = index + 1,
                payment = payment,
            )
        }

        y += 18f
    }

    private fun drawDebtCustomerSection(customers: List<DebtCustomerSummary>) {
        drawSectionTitle("Sisa Hutang Per Pembeli")
        drawDebtCustomerTableHeader()

        if (customers.isEmpty()) {
            drawEmptyTableMessage("Tidak ada sisa hutang aktif.")
            y += 10f
            return
        }

        customers.forEachIndexed { index, customer ->
            drawDebtCustomerRow(
                rowNumber = index + 1,
                customer = customer,
            )
        }

        y += 18f
    }

    private fun drawStockMovementSection(
        stockMovements: List<StockMovement>,
        productNameById: Map<Long, String>,
    ) {
        drawSectionTitle("Keluar Masuk Stok Barang")
        drawStockMovementTableHeader()

        if (stockMovements.isEmpty()) {
            drawEmptyTableMessage("Belum ada mutasi stok pada periode ini.")
            y += 10f
            return
        }

        stockMovements.forEachIndexed { index, movement ->
            drawStockMovementRow(
                rowNumber = index + 1,
                movement = movement,
                productName = productNameById[movement.productId] ?: "Barang #${movement.productId}",
            )
        }

        y += 18f
    }

    private fun drawSectionTitle(title: String) {
        ensureSpace(SECTION_TITLE_HEIGHT + TABLE_HEADER_HEIGHT + ROW_HEIGHT)
        canvas.drawText(title, MARGIN_LEFT, y, sectionPaint)
        y += SECTION_TITLE_HEIGHT
    }

    private fun drawTransactionTableHeader() {
        ensureSpace(TABLE_HEADER_HEIGHT + ROW_HEIGHT)

        val top = y
        fillPaint.color = Color.rgb(237, 238, 238)
        canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + TABLE_HEADER_HEIGHT, fillPaint)

        var x = MARGIN_LEFT + CELL_PADDING
        TRANSACTION_TABLE_COLUMNS.forEach { column ->
            canvas.drawText(column.title, x, top + 16f, headerPaint)
            x += column.width
        }
        drawTableGrid(
            columns = TRANSACTION_TABLE_COLUMNS,
            top = top,
            height = TABLE_HEADER_HEIGHT,
        )
        y += TABLE_HEADER_HEIGHT
    }

    private fun drawStockTableHeader() {
        ensureSpace(TABLE_HEADER_HEIGHT + ROW_HEIGHT)

        val top = y
        fillPaint.color = Color.rgb(237, 238, 238)
        canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + TABLE_HEADER_HEIGHT, fillPaint)

        var x = MARGIN_LEFT + CELL_PADDING
        STOCK_TABLE_COLUMNS.forEach { column ->
            canvas.drawText(column.title, x, top + 16f, headerPaint)
            x += column.width
        }
        drawTableGrid(
            columns = STOCK_TABLE_COLUMNS,
            top = top,
            height = TABLE_HEADER_HEIGHT,
        )
        y += TABLE_HEADER_HEIGHT
    }

    private fun drawDebtTransactionTableHeader() {
        drawHeaderRow(DEBT_TRANSACTION_TABLE_COLUMNS)
    }

    private fun drawDebtPaymentTableHeader() {
        drawHeaderRow(DEBT_PAYMENT_TABLE_COLUMNS)
    }

    private fun drawDebtCustomerTableHeader() {
        drawHeaderRow(DEBT_CUSTOMER_TABLE_COLUMNS)
    }

    private fun drawStockMovementTableHeader() {
        drawHeaderRow(STOCK_MOVEMENT_TABLE_COLUMNS)
    }

    private fun drawHeaderRow(columns: List<TableColumn>) {
        ensureSpace(TABLE_HEADER_HEIGHT + ROW_HEIGHT)

        val top = y
        fillPaint.color = Color.rgb(237, 238, 238)
        canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + TABLE_HEADER_HEIGHT, fillPaint)

        var x = MARGIN_LEFT + CELL_PADDING
        columns.forEach { column ->
            canvas.drawText(column.title, x, top + 16f, headerPaint)
            x += column.width
        }
        drawTableGrid(
            columns = columns,
            top = top,
            height = TABLE_HEADER_HEIGHT,
        )
        y += TABLE_HEADER_HEIGHT
    }

    private fun drawTransactionRow(
        rowNumber: Int,
        transaction: SalesTransaction,
        item: SalesTransactionItem?,
    ) {
        ensureSpace(ROW_HEIGHT) {
            drawTransactionTableHeader()
        }

        val top = y
        if (rowNumber % 2 == 0) {
            fillPaint.color = Color.rgb(252, 252, 252)
            canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + ROW_HEIGHT, fillPaint)
        }

        val values = listOf(
            rowNumber.toString(),
            transaction.createdAtMillis.toShortDateTime(),
            buyerLabel(
                buyerName = transaction.buyerName,
                buyerContact = transaction.buyerContact,
                fallback = "Pembeli Umum",
            ),
            item?.productName ?: "-",
            item?.let { "${it.quantity} ${it.unit}" } ?: "-",
            item?.sellingPrice?.toRupiah() ?: "-",
            item?.subtotal?.toRupiah() ?: transaction.totalAmount.toRupiah(),
            transaction.toPaymentLabel(),
        )

        var x = MARGIN_LEFT + CELL_PADDING
        TRANSACTION_TABLE_COLUMNS.forEachIndexed { index, column ->
            val paint = if (index == 6) boldBodyPaint else bodyPaint
            drawCell(
                text = values[index],
                x = x,
                top = top,
                width = column.width,
                paint = paint,
                maxLines = if (index == 3) 2 else 1,
            )
            x += column.width
        }

        drawTableGrid(
            columns = TRANSACTION_TABLE_COLUMNS,
            top = top,
            height = ROW_HEIGHT,
        )
        y += ROW_HEIGHT
    }

    private fun drawStockRow(
        rowNumber: Int,
        product: Product,
    ) {
        ensureSpace(ROW_HEIGHT) {
            drawStockTableHeader()
        }

        val top = y
        if (rowNumber % 2 == 0) {
            fillPaint.color = Color.rgb(252, 252, 252)
            canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + ROW_HEIGHT, fillPaint)
        }

        val values = listOf(
            rowNumber.toString(),
            product.barcode.orEmpty().ifBlank { "-" },
            product.name,
            product.category,
            product.stockQuantity.toString(),
            product.unit,
            product.purchasePrice.toRupiah(),
            product.sellingPrice.toRupiah(),
        )

        var x = MARGIN_LEFT + CELL_PADDING
        STOCK_TABLE_COLUMNS.forEachIndexed { index, column ->
            drawCell(
                text = values[index],
                x = x,
                top = top,
                width = column.width,
                paint = if (index == 4) boldBodyPaint else bodyPaint,
                maxLines = if (index == 2) 2 else 1,
            )
            x += column.width
        }

        drawTableGrid(
            columns = STOCK_TABLE_COLUMNS,
            top = top,
            height = ROW_HEIGHT,
        )
        y += ROW_HEIGHT
    }

    private fun drawDebtTransactionRow(
        rowNumber: Int,
        transaction: SalesTransaction,
    ) {
        ensureSpace(ROW_HEIGHT) {
            drawDebtTransactionTableHeader()
        }

        val values = listOf(
            rowNumber.toString(),
            transaction.createdAtMillis.toShortDateTime(),
            buyerLabel(
                buyerName = transaction.buyerName,
                buyerContact = transaction.buyerContact,
                fallback = "Tanpa Nama",
            ),
            transaction.transactionNumber,
            transaction.totalAmount.toRupiah(),
            transaction.paidAmount.toRupiah(),
            transaction.debtAmount.toRupiah(),
        )

        drawGenericRow(
            rowNumber = rowNumber,
            values = values,
            columns = DEBT_TRANSACTION_TABLE_COLUMNS,
            boldIndexes = setOf(6),
            multilineIndexes = setOf(2),
        )
    }

    private fun drawDebtPaymentRow(
        rowNumber: Int,
        payment: DebtPayment,
    ) {
        ensureSpace(ROW_HEIGHT) {
            drawDebtPaymentTableHeader()
        }

        val values = listOf(
            rowNumber.toString(),
            payment.createdAtMillis.toShortDateTime(),
            buyerLabel(
                buyerName = payment.buyerName,
                buyerContact = payment.buyerContact,
                fallback = "Tanpa Nama",
            ),
            payment.paymentMethod,
            payment.amount.toRupiah(),
            payment.note.orEmpty().ifBlank { "-" },
        )

        drawGenericRow(
            rowNumber = rowNumber,
            values = values,
            columns = DEBT_PAYMENT_TABLE_COLUMNS,
            boldIndexes = setOf(4),
            multilineIndexes = setOf(2, 5),
        )
    }

    private fun drawDebtCustomerRow(
        rowNumber: Int,
        customer: DebtCustomerSummary,
    ) {
        ensureSpace(ROW_HEIGHT) {
            drawDebtCustomerTableHeader()
        }

        val values = listOf(
            rowNumber.toString(),
            buyerLabel(
                buyerName = customer.buyerName,
                buyerContact = customer.buyerContact,
                fallback = "Tanpa Nama",
            ),
            customer.totalDebt.toRupiah(),
            customer.totalPaid.toRupiah(),
            customer.remainingDebt.toRupiah(),
        )

        drawGenericRow(
            rowNumber = rowNumber,
            values = values,
            columns = DEBT_CUSTOMER_TABLE_COLUMNS,
            boldIndexes = setOf(4),
            multilineIndexes = setOf(1),
        )
    }

    private fun drawStockMovementRow(
        rowNumber: Int,
        movement: StockMovement,
        productName: String,
    ) {
        ensureSpace(ROW_HEIGHT) {
            drawStockMovementTableHeader()
        }

        val values = listOf(
            rowNumber.toString(),
            movement.createdAtMillis.toShortDateTime(),
            productName,
            movement.type.toReportLabel(),
            movement.quantity.toString(),
            movement.currentStock.toString(),
            movement.note.orEmpty().ifBlank { "-" },
        )

        drawGenericRow(
            rowNumber = rowNumber,
            values = values,
            columns = STOCK_MOVEMENT_TABLE_COLUMNS,
            boldIndexes = setOf(3, 4, 5),
            multilineIndexes = setOf(2, 6),
        )
    }

    private fun drawGenericRow(
        rowNumber: Int,
        values: List<String>,
        columns: List<TableColumn>,
        boldIndexes: Set<Int> = emptySet(),
        multilineIndexes: Set<Int> = emptySet(),
    ) {
        val top = y
        if (rowNumber % 2 == 0) {
            fillPaint.color = Color.rgb(252, 252, 252)
            canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + ROW_HEIGHT, fillPaint)
        }

        var x = MARGIN_LEFT + CELL_PADDING
        columns.forEachIndexed { index, column ->
            drawCell(
                text = values.getOrElse(index) { "-" },
                x = x,
                top = top,
                width = column.width,
                paint = if (index in boldIndexes) boldBodyPaint else bodyPaint,
                maxLines = if (index in multilineIndexes) 2 else 1,
            )
            x += column.width
        }

        drawTableGrid(
            columns = columns,
            top = top,
            height = ROW_HEIGHT,
        )
        y += ROW_HEIGHT
    }

    private fun drawEmptyTableMessage(message: String) {
        ensureSpace(ROW_HEIGHT)
        val top = y
        canvas.drawText(message, MARGIN_LEFT + CELL_PADDING, top + 20f, bodyPaint)
        canvas.drawRect(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top + ROW_HEIGHT, linePaint)
        y += ROW_HEIGHT
    }

    private fun drawCell(
        text: String,
        x: Float,
        top: Float,
        width: Float,
        paint: Paint,
        maxLines: Int = 1,
    ) {
        val lines = text.wrapToLines(
            maxWidth = width - (CELL_PADDING * 2),
            paint = paint,
            maxLines = maxLines,
        )
        lines.forEachIndexed { index, line ->
            canvas.drawText(line, x, top + 15.5f + (index * 11.5f), paint)
        }
    }

    private fun drawTableGrid(
        columns: List<TableColumn>,
        top: Float,
        height: Float,
    ) {
        val bottom = top + height
        canvas.drawLine(MARGIN_LEFT, top, PAGE_WIDTH - MARGIN_RIGHT, top, linePaint)
        canvas.drawLine(MARGIN_LEFT, bottom, PAGE_WIDTH - MARGIN_RIGHT, bottom, linePaint)

        var x = MARGIN_LEFT
        canvas.drawLine(x, top, x, bottom, linePaint)
        columns.forEach { column ->
            x += column.width
            canvas.drawLine(x, top, x, bottom, linePaint)
        }
    }

    private fun ensureSpace(
        requiredHeight: Float,
        onPageBreak: (() -> Unit)? = null,
    ) {
        if (y + requiredHeight <= PAGE_HEIGHT - MARGIN_BOTTOM) return

        finishPage()
        startPage()
        onPageBreak?.invoke()
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
        const val CELL_PADDING = 4f
        const val SECTION_TITLE_HEIGHT = 20f
        const val TABLE_HEADER_HEIGHT = 25f
        const val ROW_HEIGHT = 40f
        const val SUMMARY_BOX_WIDTH = 254f
        const val SUMMARY_BOX_GAP = 12f

        val TRANSACTION_TABLE_COLUMNS = listOf(
            TableColumn("No", 32f),
            TableColumn("Tanggal", 92f),
            TableColumn("Pembeli", 112f),
            TableColumn("Barang", 246f),
            TableColumn("Qty", 54f),
            TableColumn("Harga", 86f),
            TableColumn("Subtotal", 96f),
            TableColumn("Bayar", 68f),
        )

        val STOCK_TABLE_COLUMNS = listOf(
            TableColumn("No", 32f),
            TableColumn("Kode", 70f),
            TableColumn("Barang", 250f),
            TableColumn("Kategori", 104f),
            TableColumn("Stok", 58f),
            TableColumn("Satuan", 60f),
            TableColumn("Harga Beli", 106f),
            TableColumn("Harga Jual", 106f),
        )

        val DEBT_TRANSACTION_TABLE_COLUMNS = listOf(
            TableColumn("No", 32f),
            TableColumn("Tanggal", 92f),
            TableColumn("Pembeli", 150f),
            TableColumn("Transaksi", 116f),
            TableColumn("Total", 98f),
            TableColumn("Bayar", 98f),
            TableColumn("Sisa Hutang", 200f),
        )

        val DEBT_PAYMENT_TABLE_COLUMNS = listOf(
            TableColumn("No", 32f),
            TableColumn("Tanggal", 100f),
            TableColumn("Pembeli", 220f),
            TableColumn("Metode", 90f),
            TableColumn("Nominal", 120f),
            TableColumn("Catatan", 224f),
        )

        val DEBT_CUSTOMER_TABLE_COLUMNS = listOf(
            TableColumn("No", 32f),
            TableColumn("Pembeli", 276f),
            TableColumn("Total Hutang", 160f),
            TableColumn("Total Bayar", 150f),
            TableColumn("Sisa Hutang", 168f),
        )

        val STOCK_MOVEMENT_TABLE_COLUMNS = listOf(
            TableColumn("No", 32f),
            TableColumn("Tanggal", 100f),
            TableColumn("Barang", 244f),
            TableColumn("Jenis", 90f),
            TableColumn("Qty", 72f),
            TableColumn("Stok Akhir", 92f),
            TableColumn("Catatan", 156f),
        )
    }
}

private fun String.wrapToLines(
    maxWidth: Float,
    paint: Paint,
    maxLines: Int,
): List<String> {
    if (maxLines <= 1) return listOf(ellipsize(maxWidth, paint))

    val words = trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (words.isEmpty()) return listOf("")

    val lines = mutableListOf<String>()
    var currentLine = ""

    words.forEachIndexed { index, word ->
        val candidate = if (currentLine.isBlank()) word else "$currentLine $word"
        if (paint.measureText(candidate) <= maxWidth) {
            currentLine = candidate
            return@forEachIndexed
        }

        if (currentLine.isNotBlank()) {
            lines += currentLine
        }

        val remainingText = words.drop(index).joinToString(" ")
        if (lines.size == maxLines - 1) {
            lines += remainingText.ellipsize(maxWidth, paint)
            return lines
        }

        currentLine = word
    }

    if (currentLine.isNotBlank() && lines.size < maxLines) {
        lines += currentLine.ellipsize(maxWidth, paint)
    }

    return lines.take(maxLines)
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

private fun buyerLabel(
    buyerName: String,
    buyerContact: String,
    fallback: String,
): String {
    val name = buyerName.trim().ifBlank { fallback }
    val contact = buyerContact.trim()

    return if (contact.isBlank()) name else "$name ($contact)"
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

private fun StockMovementType.toReportLabel(): String {
    return when (this) {
        StockMovementType.IN -> "Masuk"
        StockMovementType.OUT -> "Keluar"
        StockMovementType.ADJUSTMENT -> "Penyesuaian"
    }
}

private fun SalesTransaction.toPaymentLabel(): String {
    return if (
        paymentMethod.equals("Hutang", ignoreCase = true) &&
        paidAmount > 0L &&
        paidPaymentMethod.isNotBlank()
    ) {
        "Hutang/$paidPaymentMethod"
    } else {
        paymentMethod
    }
}
