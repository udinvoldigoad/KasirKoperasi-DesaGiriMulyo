package com.kasirkoperasi.app.di

import android.content.Context
import com.kasirkoperasi.app.core.pdf.TransactionReportPdfExporter
import com.kasirkoperasi.app.data.local.database.KasirDatabase
import com.kasirkoperasi.app.data.repository.DebtRepositoryImpl
import com.kasirkoperasi.app.data.repository.ExpenseRepositoryImpl
import com.kasirkoperasi.app.data.repository.ProductRepositoryImpl
import com.kasirkoperasi.app.data.repository.ReportRepositoryImpl
import com.kasirkoperasi.app.data.repository.SalesTransactionRepositoryImpl
import com.kasirkoperasi.app.data.repository.StockRepositoryImpl
import com.kasirkoperasi.app.domain.repository.DebtRepository
import com.kasirkoperasi.app.domain.repository.ExpenseRepository
import com.kasirkoperasi.app.domain.repository.ProductRepository
import com.kasirkoperasi.app.domain.repository.ReportRepository
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository
import com.kasirkoperasi.app.domain.repository.StockRepository
import com.kasirkoperasi.app.domain.usecase.CompleteSalesTransactionUseCase
import com.kasirkoperasi.app.domain.usecase.DeactivateProductUseCase
import com.kasirkoperasi.app.domain.usecase.ExportTransactionReportPdfUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtCustomerDetailUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtCustomersUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtPaymentsUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.GetExpensesUseCase
import com.kasirkoperasi.app.domain.usecase.GetProductsUseCase
import com.kasirkoperasi.app.domain.usecase.GetProductsIncludingInactiveUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionItemsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase
import com.kasirkoperasi.app.domain.usecase.GetStockMovementsUseCase
import com.kasirkoperasi.app.domain.usecase.ImportProductsCsvUseCase
import com.kasirkoperasi.app.domain.usecase.RecordDebtPaymentUseCase
import com.kasirkoperasi.app.domain.usecase.SaveProductUseCase
import com.kasirkoperasi.app.domain.usecase.SaveExpenseUseCase
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

    private val debtRepository: DebtRepository = DebtRepositoryImpl(
        debtPaymentDao = database.debtPaymentDao(),
        salesTransactionDao = database.salesTransactionDao(),
    )

    private val stockRepository: StockRepository = StockRepositoryImpl(
        stockDao = database.stockDao(),
    )

    private val expenseRepository: ExpenseRepository = ExpenseRepositoryImpl(
        expenseDao = database.expenseDao(),
    )

    private val reportRepository: ReportRepository = ReportRepositoryImpl(
        reportDao = database.reportDao(),
    )

    val getProductsUseCase = GetProductsUseCase(productRepository)
    val getProductsIncludingInactiveUseCase = GetProductsIncludingInactiveUseCase(productRepository)
    val saveProductUseCase = SaveProductUseCase(productRepository)
    val importProductsCsvUseCase = ImportProductsCsvUseCase(productRepository)
    val updateProductWithStockInUseCase = UpdateProductWithStockInUseCase(productRepository)
    val deactivateProductUseCase = DeactivateProductUseCase(productRepository)
    val saveExpenseUseCase = SaveExpenseUseCase(expenseRepository)
    val getExpensesUseCase = GetExpensesUseCase(expenseRepository)
    val completeSalesTransactionUseCase = CompleteSalesTransactionUseCase(salesTransactionRepository)
    val getSalesTransactionItemsUseCase = GetSalesTransactionItemsUseCase(salesTransactionRepository)
    val getSalesTransactionsUseCase = GetSalesTransactionsUseCase(salesTransactionRepository)
    val getDebtTransactionsUseCase = GetDebtTransactionsUseCase(salesTransactionRepository)
    val getDebtCustomerDetailUseCase = GetDebtCustomerDetailUseCase(debtRepository)
    val getDebtCustomersUseCase = GetDebtCustomersUseCase(debtRepository)
    val getDebtPaymentsUseCase = GetDebtPaymentsUseCase(debtRepository)
    val recordDebtPaymentUseCase = RecordDebtPaymentUseCase(debtRepository)
    val getStockMovementsUseCase = GetStockMovementsUseCase(stockRepository)
    val exportTransactionReportPdfUseCase = ExportTransactionReportPdfUseCase(
        getSalesTransactionItemsUseCase = getSalesTransactionItemsUseCase,
        getProductsUseCase = getProductsUseCase,
        getProductsIncludingInactiveUseCase = getProductsIncludingInactiveUseCase,
        getDebtPaymentsUseCase = getDebtPaymentsUseCase,
        getDebtCustomersUseCase = getDebtCustomersUseCase,
        getStockMovementsUseCase = getStockMovementsUseCase,
        getExpensesUseCase = getExpensesUseCase,
        transactionReportPdfExporter = transactionReportPdfExporter,
    )
    val getSimpleReportUseCase = GetSimpleReportUseCase(reportRepository)
}
