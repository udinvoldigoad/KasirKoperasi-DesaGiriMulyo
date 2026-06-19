package com.kasirkoperasi.app.di

import android.content.Context
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfExporter
import com.kasirkoperasi.app.data.local.database.KasirDatabase
import com.kasirkoperasi.app.data.repository.ProductRepositoryImpl
import com.kasirkoperasi.app.data.repository.ReportRepositoryImpl
import com.kasirkoperasi.app.data.repository.SalesTransactionRepositoryImpl
import com.kasirkoperasi.app.domain.repository.ProductRepository
import com.kasirkoperasi.app.domain.repository.ReportRepository
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository
import com.kasirkoperasi.app.domain.usecase.CompleteSalesTransactionUseCase
import com.kasirkoperasi.app.domain.usecase.DeactivateProductUseCase
import com.kasirkoperasi.app.domain.usecase.ExportTransactionReportPdfUseCase
import com.kasirkoperasi.app.domain.usecase.GetProductsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionItemsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase
import com.kasirkoperasi.app.domain.usecase.SaveProductUseCase
import com.kasirkoperasi.app.domain.usecase.UpdateProductWithStockInUseCase

class AppContainer(context: Context) {
    private val database = KasirDatabase.getInstance(context)
    private val transactionReportPdfExporter = TransactionReportPdfExporter(context.applicationContext)

    private val productRepository: ProductRepository = ProductRepositoryImpl(
        productDao = database.productDao(),
        stockDao = database.stockDao(),
        database = database,
    )

    private val salesTransactionRepository: SalesTransactionRepository = SalesTransactionRepositoryImpl(
        salesTransactionDao = database.salesTransactionDao(),
        productDao = database.productDao(),
        stockDao = database.stockDao(),
        database = database,
    )

    private val reportRepository: ReportRepository = ReportRepositoryImpl(
        reportDao = database.reportDao(),
    )

    val getProductsUseCase = GetProductsUseCase(productRepository)
    val saveProductUseCase = SaveProductUseCase(productRepository)
    val updateProductWithStockInUseCase = UpdateProductWithStockInUseCase(productRepository)
    val deactivateProductUseCase = DeactivateProductUseCase(productRepository)
    val completeSalesTransactionUseCase = CompleteSalesTransactionUseCase(salesTransactionRepository)
    val getSalesTransactionItemsUseCase = GetSalesTransactionItemsUseCase(salesTransactionRepository)
    val exportTransactionReportPdfUseCase = ExportTransactionReportPdfUseCase(
        getSalesTransactionItemsUseCase = getSalesTransactionItemsUseCase,
        transactionReportPdfExporter = transactionReportPdfExporter,
    )
    val getSalesTransactionsUseCase = GetSalesTransactionsUseCase(salesTransactionRepository)
    val getSimpleReportUseCase = GetSimpleReportUseCase(reportRepository)
}
