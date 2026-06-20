package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.ProductCategory
import com.kasirkoperasi.app.domain.repository.ProductRepository

data class ProductCsvImportResult(
    val totalRows: Int,
    val importedCount: Int,
    val skippedCount: Int,
    val issues: List<ProductCsvImportIssue>,
)

data class ProductCsvImportIssue(
    val rowNumber: Int,
    val reason: String,
)

class ImportProductsCsvUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(csvText: String): ProductCsvImportResult {
        val rows = CsvParser.parse(csvText)
            .filterNot { row -> row.all { it.isBlank() } }

        require(rows.isNotEmpty()) { "File CSV kosong" }

        val header = rows.first().map { it.normalizeHeader() }
        val indexes = resolveColumnIndexes(header)
        val issues = mutableListOf<ProductCsvImportIssue>()
        val seenBarcodes = mutableSetOf<String>()
        var importedCount = 0
        var totalRows = 0

        rows.drop(1).forEachIndexed { index, row ->
            val rowNumber = index + 2
            if (row.all { it.isBlank() }) return@forEachIndexed

            totalRows += 1

            val barcode = row.valueAt(indexes.kode).normalizeBarcode()
            val name = row.valueAt(indexes.nama).trim()
            val category = row.valueAt(indexes.kategori)
                .trim()
                .ifBlank { ProductCategory.DEFAULT }
                .normalizeCategoryOrNull()
            val purchasePrice = row.valueAt(indexes.hargaBeli).toMoneyOrNull()
            val sellingPrice = row.valueAt(indexes.hargaJual).toMoneyOrNull()
            val stockQuantity = row.valueAt(indexes.stok).toQuantityOrNull()
            val unit = row.valueAt(indexes.satuan).trim().ifBlank { "pcs" }

            val validationError = if (barcode == null) {
                "kode/barcode harus angka maksimal 4 digit"
            } else {
                when {
                    !seenBarcodes.add(barcode) -> "kode $barcode duplikat di file CSV"
                    name.isBlank() -> "nama produk wajib diisi"
                    category == null -> "kategori harus salah satu: ${ProductCategory.options.joinToString(", ")}"
                    purchasePrice == null -> "harga_beli tidak valid"
                    sellingPrice == null || sellingPrice <= 0L -> "harga_jual wajib lebih dari 0"
                    stockQuantity == null -> "stok tidak valid"
                    productRepository.getProductByBarcode(barcode) != null -> "kode $barcode sudah ada di database"
                    else -> null
                }
            }

            if (validationError != null) {
                issues += ProductCsvImportIssue(
                    rowNumber = rowNumber,
                    reason = validationError,
                )
                return@forEachIndexed
            }

            runCatching {
                productRepository.saveProduct(
                    Product(
                        name = name,
                        category = requireNotNull(category),
                        barcode = requireNotNull(barcode),
                        unit = unit,
                        purchasePrice = requireNotNull(purchasePrice),
                        sellingPrice = requireNotNull(sellingPrice),
                        stockQuantity = requireNotNull(stockQuantity),
                    ),
                )
            }.onSuccess {
                importedCount += 1
            }.onFailure { throwable ->
                issues += ProductCsvImportIssue(
                    rowNumber = rowNumber,
                    reason = throwable.message ?: "gagal menyimpan produk",
                )
            }
        }

        return ProductCsvImportResult(
            totalRows = totalRows,
            importedCount = importedCount,
            skippedCount = issues.size,
            issues = issues,
        )
    }

    private fun resolveColumnIndexes(header: List<String>): CsvProductColumnIndexes {
        fun indexOf(vararg names: String): Int {
            return names
                .map { it.normalizeHeader() }
                .firstNotNullOfOrNull { normalizedName ->
                    header.indexOf(normalizedName).takeIf { it >= 0 }
                }
                ?: -1
        }

        val indexes = CsvProductColumnIndexes(
            kode = indexOf("kode", "barcode"),
            nama = indexOf("nama", "nama_produk", "produk"),
            kategori = indexOf("kategori", "category"),
            hargaBeli = indexOf("harga_beli", "harga beli", "modal"),
            hargaJual = indexOf("harga_jual", "harga jual", "jual"),
            stok = indexOf("stok", "stock", "qty"),
            satuan = indexOf("satuan", "unit"),
        )

        val missingColumns = buildList {
            if (indexes.kode < 0) add("kode")
            if (indexes.nama < 0) add("nama")
            if (indexes.kategori < 0) add("kategori")
            if (indexes.hargaBeli < 0) add("harga_beli")
            if (indexes.hargaJual < 0) add("harga_jual")
            if (indexes.stok < 0) add("stok")
            if (indexes.satuan < 0) add("satuan")
        }

        require(missingColumns.isEmpty()) {
            "Kolom CSV kurang: ${missingColumns.joinToString(", ")}"
        }

        return indexes
    }

    private data class CsvProductColumnIndexes(
        val kode: Int,
        val nama: Int,
        val kategori: Int,
        val hargaBeli: Int,
        val hargaJual: Int,
        val stok: Int,
        val satuan: Int,
    )
}

private object CsvParser {
    fun parse(csvText: String): List<List<String>> {
        val normalizedText = csvText.removePrefix("\uFEFF")
        val delimiter = normalizedText.detectDelimiter()
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < normalizedText.length) {
            val char = normalizedText[index]
            when {
                char == '"' -> {
                    if (inQuotes && normalizedText.getOrNull(index + 1) == '"') {
                        cell.append('"')
                        index += 1
                    } else {
                        inQuotes = !inQuotes
                    }
                }

                char == delimiter && !inQuotes -> {
                    row += cell.toString()
                    cell.clear()
                }

                (char == '\n' || char == '\r') && !inQuotes -> {
                    row += cell.toString()
                    cell.clear()
                    rows += row.toList()
                    row.clear()

                    if (char == '\r' && normalizedText.getOrNull(index + 1) == '\n') {
                        index += 1
                    }
                }

                else -> cell.append(char)
            }

            index += 1
        }

        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row += cell.toString()
            rows += row.toList()
        }

        return rows
    }

    private fun String.detectDelimiter(): Char {
        val firstLine = lineSequence().firstOrNull { it.isNotBlank() }.orEmpty()
        val commaCount = firstLine.count { it == ',' }
        val semicolonCount = firstLine.count { it == ';' }

        return if (semicolonCount > commaCount) ';' else ','
    }
}

private fun String.normalizeHeader(): String {
    return trim()
        .lowercase()
        .replace(" ", "_")
}

private fun List<String>.valueAt(index: Int): String {
    return getOrNull(index).orEmpty()
}

private fun String.normalizeBarcode(): String? {
    val rawCode = trim()
    if (rawCode.isBlank()) return null
    if (!rawCode.all { it.isDigit() }) return null
    if (rawCode.length > 4) return null

    return rawCode.padStart(4, '0')
}

private fun String.toMoneyOrNull(): Long? {
    val digits = filter { it.isDigit() }
    if (digits.isBlank()) return null

    return digits.toLongOrNull()?.takeIf { it >= 0L }
}

private fun String.toQuantityOrNull(): Int? {
    val digits = trim()
    if (digits.isBlank()) return null
    if (!digits.all { it.isDigit() }) return null

    return digits.toIntOrNull()?.takeIf { it >= 0 }
}

private fun String.normalizeCategoryOrNull(): String? {
    return ProductCategory.options.firstOrNull { option ->
        option.equals(trim(), ignoreCase = true)
    }
}
