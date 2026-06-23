package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.ReportSummaryEntity
import com.kasirkoperasi.app.domain.model.ReportSummary

fun ReportSummaryEntity.toDomain(): ReportSummary = ReportSummary(
    totalSales = totalSales,
    totalProfit = totalProfit,
    totalCash = totalCash,
    totalQris = totalQris,
    totalDebt = totalDebt,
    soldItemCount = soldItemCount,
    lowStockItemCount = lowStockItemCount,
)
