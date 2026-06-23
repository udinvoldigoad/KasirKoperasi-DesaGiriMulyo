package com.kasirkoperasi.app.core.printer

import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptEscPosFormatter {
    fun formatTestPrint(storeName: String): ByteArray {
        val output = ByteArrayOutputStream()

        output.write(byteArrayOf(ESC, '@'.code.toByte()))
        output.write(byteArrayOf(ESC, 't'.code.toByte(), 0))
        output.alignCenter()
        output.bold(true)
        output.text("TEST PRINT")
        output.bold(false)
        output.text(storeName.ifBlank { "KasirKoperasi" }.take(PRINTER_LINE_WIDTH))
        output.text(System.currentTimeMillis().toDateTime())
        output.text("")
        output.text("Printer berhasil terhubung")
        output.text("")
        output.feed(lines = 4)

        return output.toByteArray()
    }

    fun format(data: ReceiptPrintData): ByteArray {
        val output = ByteArrayOutputStream()

        output.write(byteArrayOf(ESC, '@'.code.toByte()))
        output.write(byteArrayOf(ESC, 't'.code.toByte(), 0))
        output.alignCenter()
        output.bold(true)
        output.text(data.storeName.ifBlank { "KasirKoperasi" }.take(PRINTER_LINE_WIDTH))
        output.bold(false)
        output.text("Struk Pembayaran")
        output.text(data.printedAtMillis.toDateTime())
        output.text("")

        output.alignLeft()
        output.text(separator())
        if (data.buyerName.isNotBlank()) {
            output.text(column("Pembeli", data.buyerName.take(20)))
        }
        output.text(column("Pembayaran", data.paymentMethod))
        if (data.paymentMethod.equals("Hutang", ignoreCase = true) &&
            data.paidAmount > 0L &&
            data.paidPaymentMethod.isNotBlank()
        ) {
            output.text(column("Uang Muka Via", data.paidPaymentMethod))
        }
        output.text(separator())

        data.items.forEach { item ->
            output.text(item.name.take(PRINTER_LINE_WIDTH))
            output.text(
                column(
                    left = "${item.quantity} ${item.unit} x ${item.sellingPrice.toRupiah()}",
                    right = item.subtotal.toRupiah(),
                ),
            )
        }

        output.text(separator())
        output.bold(true)
        output.text(column("TOTAL", data.totalAmount.toRupiah()))
        output.bold(false)
        output.text(column("Dibayar", data.paidAmount.toRupiah()))
        if (data.debtAmount > 0L) {
            output.text(column("Sisa Hutang", data.debtAmount.toRupiah()))
        } else {
            output.text(column("Kembali", data.changeAmount.toRupiah()))
        }
        output.text(separator())

        output.alignCenter()
        output.text("Terima kasih")
        output.text("Barang yang sudah dibeli")
        output.text("tidak dapat dikembalikan")
        output.feed(lines = 4)

        return output.toByteArray()
    }

    private fun ByteArrayOutputStream.alignLeft() {
        write(byteArrayOf(ESC, 'a'.code.toByte(), 0))
    }

    private fun ByteArrayOutputStream.alignCenter() {
        write(byteArrayOf(ESC, 'a'.code.toByte(), 1))
    }

    private fun ByteArrayOutputStream.bold(enabled: Boolean) {
        write(byteArrayOf(ESC, 'E'.code.toByte(), if (enabled) 1 else 0))
    }

    private fun ByteArrayOutputStream.feed(lines: Int) {
        write(byteArrayOf(ESC, 'd'.code.toByte(), lines.coerceAtLeast(0).toByte()))
    }

    private fun ByteArrayOutputStream.text(value: String) {
        write((value + "\r\n").toByteArray(PRINTER_CHARSET))
    }

    private fun column(left: String, right: String): String {
        val cleanLeft = left.take(PRINTER_LINE_WIDTH)
        val cleanRight = right.take(PRINTER_LINE_WIDTH)
        val availableSpace = PRINTER_LINE_WIDTH - cleanLeft.length - cleanRight.length

        return if (availableSpace > 0) {
            cleanLeft + " ".repeat(availableSpace) + cleanRight
        } else {
            cleanLeft.take((PRINTER_LINE_WIDTH - cleanRight.length - 1).coerceAtLeast(0)) + " " + cleanRight
        }
    }

    private fun separator(): String = "-".repeat(PRINTER_LINE_WIDTH)

    private fun Long.toDateTime(): String {
        return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("id-ID")).format(Date(this))
    }

    private fun Long.toRupiah(): String {
        val grouped = toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return "Rp$grouped"
    }

    private val PRINTER_CHARSET = Charsets.ISO_8859_1
    private const val PRINTER_LINE_WIDTH = 32
    private const val ESC = 0x1B.toByte()
}
