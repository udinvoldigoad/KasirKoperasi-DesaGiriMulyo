package com.kasirkoperasi.app.domain.usecase

import android.net.Uri
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfData
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfExporter
import com.kasirkoperasi.app.domain.model.SalesTransaction

class ExportTransactionReportPdfUseCase(
    private val getSalesTransactionItemsUseCase: GetSalesTransactionItemsUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val getDebtPaymentsUseCase: GetDebtPaymentsUseCase,
    private val getDebtCustomersUseCase: GetDebtCustomersUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase,
    private val transactionReportPdfExporter: TransactionReportPdfExporter,
) {
    suspend operator fun invoke(
        periodLabel: String,
        startDateMillis: Long,
        endDateMillis: Long,
        transactions: List<SalesTransaction>,
    ): Uri {
        val itemsByTransactionId = transactions.associate { transaction ->
            transaction.id to getSalesTransactionItemsUseCase(transaction.id)
        }
        val products = getProductsUseCase()
        val debtPayments = getDebtPaymentsUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )
        val debtCustomers = getDebtCustomersUseCase()
        val stockMovements = getStockMovementsUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )

        require(
            transactions.isNotEmpty() ||
                products.isNotEmpty() ||
                debtPayments.isNotEmpty() ||
                stockMovements.isNotEmpty(),
        ) {
            "Tidak ada data untuk diexport"
        }

        return transactionReportPdfExporter.export(
            TransactionReportPdfData(
                periodLabel = periodLabel,
                transactions = transactions,
                itemsByTransactionId = itemsByTransactionId,
                stockProducts = products,
                debtPayments = debtPayments,
                debtCustomers = debtCustomers,
                stockMovements = stockMovements,
            ),
        )
    }
}
