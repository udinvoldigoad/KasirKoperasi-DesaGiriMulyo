package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionDraftItem
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.model.SalesTransactionPayment
import com.kasirkoperasi.app.domain.model.SalesReturn
import com.kasirkoperasi.app.domain.model.SalesReturnSummary

interface SalesTransactionRepository {
    suspend fun completeTransaction(
        items: List<SalesTransactionDraftItem>,
        payment: SalesTransactionPayment,
    ): Long

    suspend fun getTransactionsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesTransaction>

    suspend fun getDebtTransactions(
        limit: Int,
    ): List<SalesTransaction>

    suspend fun getDebtTransactionsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesTransaction>

    suspend fun getTransactionItems(transactionId: Long): List<SalesTransactionItem>

    suspend fun getReturnedQuantity(transactionItemId: Long): Int

    suspend fun getReturnSummaries(transactionItemIds: List<Long>): Map<Long, SalesReturnSummary>

    suspend fun getReturnsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesReturn>

    suspend fun returnTransactionItem(
        transaction: SalesTransaction,
        item: SalesTransactionItem,
        quantity: Int,
    ): Long
}
