package com.kasirkoperasi.app.data.repository

import com.kasirkoperasi.app.data.local.dao.DebtPaymentDao
import com.kasirkoperasi.app.data.local.dao.ProductDao
import com.kasirkoperasi.app.data.local.dao.SalesReturnDao
import com.kasirkoperasi.app.data.local.dao.SalesTransactionDao
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.domain.repository.ReportRepository
import com.kasirkoperasi.app.domain.service.ReportAccountingCalculator

class ReportRepositoryImpl(
    private val salesTransactionDao: SalesTransactionDao,
    private val salesReturnDao: SalesReturnDao,
    private val debtPaymentDao: DebtPaymentDao,
    private val productDao: ProductDao,
) : ReportRepository {
    override suspend fun getSummary(startDateMillis: Long, endDateMillis: Long): ReportSummary {
        val transactions = salesTransactionDao.getTransactionsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = Int.MAX_VALUE,
        ).map { it.toDomain() }
        val returns = salesReturnDao.getReturnsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = Int.MAX_VALUE,
        ).map { it.toDomain() }
        val debtPayments = debtPaymentDao.getPaymentsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = Int.MAX_VALUE,
        ).map { it.toDomain() }
        val lowStockItemCount = productDao.getAllProducts().count { it.stockQuantity <= LOW_STOCK_LIMIT }

        return ReportAccountingCalculator.calculateSummary(
            transactions = transactions,
            returns = returns,
            debtPayments = debtPayments,
            lowStockItemCount = lowStockItemCount,
        )
    }

    private companion object {
        const val LOW_STOCK_LIMIT = 5
    }
}
