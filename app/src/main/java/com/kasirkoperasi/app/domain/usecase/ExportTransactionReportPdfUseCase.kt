package com.kasirkoperasi.app.domain.usecase

import android.net.Uri
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfData
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfExporter
import com.kasirkoperasi.app.domain.model.SalesTransaction

class ExportTransactionReportPdfUseCase(
    private val getSalesTransactionItemsUseCase: GetSalesTransactionItemsUseCase,
    private val getSalesReturnSummariesUseCase: GetSalesReturnSummariesUseCase,
    private val getSalesReturnsUseCase: GetSalesReturnsUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    private val getProductsIncludingInactiveUseCase: GetProductsIncludingInactiveUseCase,
    private val getDebtPaymentsUseCase: GetDebtPaymentsUseCase,
    private val getDebtCustomersUseCase: GetDebtCustomersUseCase,
    private val getStockMovementsUseCase: GetStockMovementsUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
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
        val transactionItemIds = itemsByTransactionId.values.flatten().map { it.id }
        val returnSummariesByItemId = getSalesReturnSummariesUseCase(transactionItemIds)
        val returns = getSalesReturnsUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )
        val products = getProductsUseCase()
        val productsIncludingInactive = getProductsIncludingInactiveUseCase()
        val debtPayments = getDebtPaymentsUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )
        val debtCustomers = getDebtCustomersUseCase()
        val stockMovements = getStockMovementsUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )
        val expenses = getExpensesUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )

        require(
            transactions.isNotEmpty() ||
                returns.isNotEmpty() ||
                products.isNotEmpty() ||
                debtPayments.isNotEmpty() ||
                stockMovements.isNotEmpty() ||
                expenses.isNotEmpty(),
        ) {
            "Tidak ada data untuk diexport"
        }

        return transactionReportPdfExporter.export(
            TransactionReportPdfData(
                periodLabel = periodLabel,
                transactions = transactions,
                itemsByTransactionId = itemsByTransactionId,
                returnSummariesByItemId = returnSummariesByItemId,
                returns = returns,
                stockProducts = products,
                productNamesById = productsIncludingInactive.associate { it.id to it.name },
                debtPayments = debtPayments,
                debtCustomers = debtCustomers,
                stockMovements = stockMovements,
                expenses = expenses,
            ),
        )
    }
}
