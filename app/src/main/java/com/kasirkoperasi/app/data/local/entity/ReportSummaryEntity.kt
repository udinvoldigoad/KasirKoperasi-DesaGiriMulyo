package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo

data class ReportSummaryEntity(
    @ColumnInfo(name = "total_sales")
    val totalSales: Long = 0L,
    @ColumnInfo(name = "total_profit")
    val totalProfit: Long = 0L,
    @ColumnInfo(name = "sold_item_count")
    val soldItemCount: Int = 0,
    @ColumnInfo(name = "low_stock_item_count")
    val lowStockItemCount: Int = 0,
)
