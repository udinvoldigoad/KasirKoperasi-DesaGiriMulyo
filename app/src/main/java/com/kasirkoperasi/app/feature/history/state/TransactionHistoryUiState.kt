package com.kasirkoperasi.app.feature.history.state

import com.kasirkoperasi.app.domain.model.SalesTransaction

data class TransactionHistoryUiState(
    val isLoading: Boolean = false,
    val selectedRange: TransactionHistoryRange = TransactionHistoryRange.Today,
    val transactions: List<SalesTransaction> = emptyList(),
    val errorMessage: String? = null,
) {
    val totalSales: Long
        get() = transactions.sumOf { it.totalAmount }

    val totalProfit: Long
        get() = transactions.sumOf { it.totalProfit }

    val totalItems: Int
        get() = transactions.sumOf { it.itemCount }
}

enum class TransactionHistoryRange(
    val label: String,
    val description: String,
    val daySpan: Int,
) {
    Today(
        label = "Hari Ini",
        description = "Transaksi hari ini",
        daySpan = 1,
    ),
    SevenDays(
        label = "7 Hari",
        description = "7 hari terakhir",
        daySpan = 7,
    ),
    ThirtyDays(
        label = "30 Hari",
        description = "30 hari terakhir",
        daySpan = 30,
    ),
}
