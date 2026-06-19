package com.kasirkoperasi.app.core.barcode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface

object Code128BarcodeGenerator {
    fun generate(
        value: String,
        moduleWidth: Int = 4,
        barcodeHeight: Int = 160,
        textHeight: Int = 46,
    ): Bitmap {
        require(value.isNotBlank()) { "Kode barcode tidak boleh kosong" }
        require(value.all { it.code in 32..126 }) { "Code 128 hanya mendukung karakter ASCII standar" }

        val encodedValues = encodeCode128B(value)
        val pattern = encodedValues.joinToString(separator = "") { CODE_PATTERNS[it] }
        val quietZoneModules = 10
        val width = (pattern.sumOf { it.digitToInt() } + quietZoneModules * 2) * moduleWidth
        val height = barcodeHeight + textHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawColor(Color.WHITE)

        var x = quietZoneModules * moduleWidth
        var drawBar = true
        pattern.forEach { widthChar ->
            val segmentWidth = widthChar.digitToInt() * moduleWidth
            if (drawBar) {
                canvas.drawRect(
                    x.toFloat(),
                    0f,
                    (x + segmentWidth).toFloat(),
                    barcodeHeight.toFloat(),
                    barPaint,
                )
            }
            x += segmentWidth
            drawBar = !drawBar
        }

        canvas.drawText(
            value,
            width / 2f,
            barcodeHeight + 32f,
            textPaint,
        )

        return bitmap
    }

    private fun encodeCode128B(value: String): List<Int> {
        val values = mutableListOf(START_CODE_B)
        values += value.map { it.code - 32 }

        val checksum = values
            .drop(1)
            .foldIndexed(START_CODE_B) { index, total, codeValue ->
                total + codeValue * (index + 1)
            } % 103

        values += checksum
        values += STOP_CODE
        return values
    }

    private const val START_CODE_B = 104
    private const val STOP_CODE = 106

    private val CODE_PATTERNS = listOf(
        "212222", "222122", "222221", "121223", "121322", "131222", "122213", "122312",
        "132212", "221213", "221312", "231212", "112232", "122132", "122231", "113222",
        "123122", "123221", "223211", "221132", "221231", "213212", "223112", "312131",
        "311222", "321122", "321221", "312212", "322112", "322211", "212123", "212321",
        "232121", "111323", "131123", "131321", "112313", "132113", "132311", "211313",
        "231113", "231311", "112133", "112331", "132131", "113123", "113321", "133121",
        "313121", "211331", "231131", "213113", "213311", "213131", "311123", "311321",
        "331121", "312113", "312311", "332111", "314111", "221411", "431111", "111224",
        "111422", "121124", "121421", "141122", "141221", "112214", "112412", "122114",
        "122411", "142112", "142211", "241211", "221114", "413111", "241112", "134111",
        "111242", "121142", "121241", "114212", "124112", "124211", "411212", "421112",
        "421211", "212141", "214121", "412121", "111143", "111341", "131141", "114113",
        "114311", "411113", "411311", "113141", "114131", "311141", "411131", "211412",
        "211214", "211232", "2331112",
    )
}
