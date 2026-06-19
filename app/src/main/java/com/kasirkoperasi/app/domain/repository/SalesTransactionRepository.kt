package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionDraftItem
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.model.SalesTransactionPayment

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

    suspend fun getTransactionItems(transactionId: Long): List<SalesTransactionItem>
}
