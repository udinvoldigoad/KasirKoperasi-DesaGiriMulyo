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
        textSize = 7.8f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(105, 115, 109)
        textSize = 6.8f
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(216, 222, 216)
        style = Paint.Style.STROKE
        strokeWidth = 0.9f
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
            "Total $totalLabels label kecil. Cetak ukuran A4, lalu tempel barcode pada rak barang.",
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
        canvas.drawRoundRect(rect, 7f, 7f, fillPaint)
        canvas.drawRoundRect(rect, 7f, 7f, borderPaint)

        drawCenteredLine(label.product.name, left + 6f, top + 13f, left + LABEL_WIDTH - 6f, productNamePaint)

        val barcodeBitmap = Code128BarcodeGenerator.generate(
            value = label.barcode,
            moduleWidth = 5,
            barcodeHeight = 122,
            textHeight = 0,
        )
        val barcodeRect = RectF(left + 6f, top + 21f, left + LABEL_WIDTH - 6f, top + 53f)
        canvas.drawBitmap(barcodeBitmap, null, barcodeRect, null)

        drawCenteredLine(label.product.category, left + 6f, top + 65f, left + LABEL_WIDTH - 6f, smallPaint)
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
        const val MARGIN = 20f
        const val GUTTER = 5f
        const val COLUMNS = 5
        const val ROWS = 10
        const val LABELS_PER_PAGE = COLUMNS * ROWS
        const val FIRST_LABEL_TOP = 58f
        const val LABEL_WIDTH = (PAGE_WIDTH - MARGIN * 2 - GUTTER * (COLUMNS - 1)) / COLUMNS
        const val LABEL_HEIGHT = 72f
    }
}
