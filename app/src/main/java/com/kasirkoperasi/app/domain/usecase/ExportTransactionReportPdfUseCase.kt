package com.kasirkoperasi.app.domain.usecase

import android.net.Uri
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfData
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfExporter
import com.kasirkoperasi.app.domain.model.SalesTransaction

class ExportTransactionReportPdfUseCase(
    private val getSalesTransactionItemsUseCase: GetSalesTransactionItemsUseCase,
    private val transactionReportPdfExporter: TransactionReportPdfExporter,
) {
    suspend operator fun invoke(
        periodLabel: String,
        transactions: List<SalesTransaction>,
    ): Uri {
        require(transactions.isNotEmpty()) { "Tidak ada transaksi untuk diexport" }

        val itemsByTransactionId = transactions.associate { transaction ->
            transaction.id to getSalesTransactionItemsUseCase(transaction.id)
        }

        return transactionReportPdfExporter.export(
            TransactionReportPdfData(
                periodLabel = periodLabel,
                transactions = transactions,
                itemsByTransactionId = itemsByTransactionId,
            ),
        )
    }
}
