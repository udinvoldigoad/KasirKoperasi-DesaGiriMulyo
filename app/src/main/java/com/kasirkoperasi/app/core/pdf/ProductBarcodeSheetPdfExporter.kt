package com.kasirkoperasi.app.core.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.kasirkoperasi.app.core.barcode.Code128BarcodeGenerator
import com.kasirkoperasi.app.core.settings.StoreProfileStore
import com.kasirkoperasi.app.domain.model.Product
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProductBarcodeSheetPdfExporter(
    private val context: Context,
) {
    fun export(products: List<Product>): Uri {
        val barcodeProducts = products
            .filter { it.isActive }
            .mapNotNull { product ->
                val barcode = product.barcode.orEmpty().trim()
                if (barcode.isBlank()) null else ProductBarcodeLabel(product = product, barcode = barcode)
            }

        require(barcodeProducts.isNotEmpty()) {
            "Belum ada produk yang memiliki barcode"
        }

        val document = PdfDocument()
        ProductBarcodeSheetRenderer(
            document = document,
            storeName = StoreProfileStore.load(context).storeName,
        ).render(barcodeProducts)

        val directory = File(context.cacheDir, BARCODE_DIRECTORY).apply {
            mkdirs()
        }
        val file = File(directory, "barcode-produk-a4-${System.currentTimeMillis()}.pdf")

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
        const val BARCODE_DIRECTORY = "product-barcodes"
    }
}

private data class ProductBarcodeLabel(
    val product: Product,
    val barcode: String,
)

private class ProductBarcodeSheetRenderer(
    private val document: PdfDocument,
    private val storeName: String,
) {
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(7, 84, 54)
        textSize = 17f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 115, 109)
        textSize = 9.5f
    }
    private val productNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(23, 34, 27)
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 115, 109)
        textSize = 8.2f
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(216, 222, 216)
        style = Paint.Style.STROKE
        strokeWidth = 1.1f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private lateinit var canvas: Canvas
    private lateinit var page: PdfDocument.Page

    fun render(labels: List<ProductBarcodeLabel>) {
        labels.chunked(LABELS_PER_PAGE).forEachIndexed { pageIndex, pageLabels ->
            startPage(pageIndex + 1)
            drawHeader(totalLabels = labels.size)
            drawLabels(pageLabels)
            finishPage()
        }
    }

    private fun startPage(number: Int) {
        page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create(),
        )
        canvas = page.canvas
        canvas.drawColor(Color.WHITE)
    }

    private fun finishPage() {
        document.finishPage(page)
    }

    private fun drawHeader(totalLabels: Int) {
        canvas.drawText("Barcode Produk - $storeName", MARGIN, 30f, titlePaint)
        canvas.drawText(
            "Total $totalLabels label. Cetak ukuran A4, lalu tempel barcode pada rak barang.",
            MARGIN,
            47f,
            subtitlePaint,
        )
        canvas.drawText(
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(Date()),
            PAGE_WIDTH - MARGIN - 96f,
            30f,
            subtitlePaint,
        )
    }

    private fun drawLabels(labels: List<ProductBarcodeLabel>) {
        labels.forEachIndexed { index, label ->
            val column = index % COLUMNS
            val row = index / COLUMNS
            val left = MARGIN + column * (LABEL_WIDTH + GUTTER)
            val top = FIRST_LABEL_TOP + row * (LABEL_HEIGHT + GUTTER)
            drawLabel(label = label, left = left, top = top)
        }
    }

    private fun drawLabel(label: ProductBarcodeLabel, left: Float, top: Float) {
        val rect = RectF(left, top, left + LABEL_WIDTH, top + LABEL_HEIGHT)
        canvas.drawRoundRect(rect, 9f, 9f, fillPaint)
        canvas.drawRoundRect(rect, 9f, 9f, borderPaint)

        drawCenteredLine(label.product.name, left + 8f, top + 17f, left + LABEL_WIDTH - 8f, productNamePaint)

        val barcodeBitmap = Code128BarcodeGenerator.generate(
            value = label.barcode,
            moduleWidth = 6,
            barcodeHeight = 150,
            textHeight = 52,
        )
        val barcodeRect = RectF(left + 10f, top + 27f, left + LABEL_WIDTH - 10f, top + 88f)
        canvas.drawBitmap(barcodeBitmap, null, barcodeRect, null)

        drawCenteredLine(label.product.category, left + 8f, top + 105f, left + LABEL_WIDTH - 8f, smallPaint)
    }

    private fun drawCenteredLine(text: String, left: Float, baseline: Float, right: Float, paint: Paint) {
        val availableWidth = right - left
        val displayText = if (paint.measureText(text) <= availableWidth) {
            text
        } else {
            val ellipsis = "..."
            val ellipsisWidth = paint.measureText(ellipsis)
            val count = paint.breakText(text, true, availableWidth - ellipsisWidth, null)
            text.take(count).trimEnd() + ellipsis
        }

        val x = left + (availableWidth - paint.measureText(displayText)) / 2f
        canvas.drawText(displayText, x, baseline, paint)
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        const val MARGIN = 28f
        const val GUTTER = 9f
        const val COLUMNS = 3
        const val ROWS = 6
        const val LABELS_PER_PAGE = COLUMNS * ROWS
        const val FIRST_LABEL_TOP = 66f
        const val LABEL_WIDTH = (PAGE_WIDTH - MARGIN * 2 - GUTTER * (COLUMNS - 1)) / COLUMNS
        const val LABEL_HEIGHT = 116f
    }
}
