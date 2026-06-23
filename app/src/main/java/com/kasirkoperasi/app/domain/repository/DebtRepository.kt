package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.DebtCustomerSummary
import com.kasirkoperasi.app.domain.model.DebtCustomerDetail
import com.kasirkoperasi.app.domain.model.DebtPayment

interface DebtRepository {
    suspend fun recordPayment(payment: DebtPayment): Long

    suspend fun getPaymentsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<DebtPayment>

    suspend fun getOutstandingCustomers(): List<DebtCustomerSummary>

    suspend fun getCustomerDetail(
        buyerName: String,
        buyerContact: String,
    ): DebtCustomerDetail
}
