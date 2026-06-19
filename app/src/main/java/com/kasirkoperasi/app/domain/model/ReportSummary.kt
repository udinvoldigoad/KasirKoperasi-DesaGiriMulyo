package com.kasirkoperasi.app.domain.model

data class ReportSummary(
    val totalSales: Long = 0L,
    val totalProfit: Long = 0L,
    val soldItemCount: Int = 0,
    val lowStockItemCount: Int = 0,
)
