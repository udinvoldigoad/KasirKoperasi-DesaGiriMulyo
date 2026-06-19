package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class GetSalesTransactionItemsUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(transactionId: Long): List<SalesTransactionItem> {
        return salesTransactionRepository.getTransactionItems(transactionId)
    }
}
