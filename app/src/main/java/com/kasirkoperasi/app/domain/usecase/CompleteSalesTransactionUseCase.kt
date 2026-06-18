package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesTransactionDraftItem
import com.kasirkoperasi.app.domain.model.SalesTransactionPayment
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class CompleteSalesTransactionUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(
        items: List<SalesTransactionDraftItem>,
        payment: SalesTransactionPayment,
    ): Long {
        return salesTransactionRepository.completeTransaction(items, payment)
    }
}
