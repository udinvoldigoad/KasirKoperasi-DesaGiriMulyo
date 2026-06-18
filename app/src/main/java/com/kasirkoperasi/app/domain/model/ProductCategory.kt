package com.kasirkoperasi.app.domain.model

object ProductCategory {
    const val ALL = "Semua"
    const val DEFAULT = "Obat"

    val options = listOf(DEFAULT, "Pupuk", "Benih", "Alat Tani")
    val filterOptions = listOf(ALL) + options

    fun normalize(category: String): String {
        return options.firstOrNull { it.equals(category.trim(), ignoreCase = true) } ?: DEFAULT
    }
}
