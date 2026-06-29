package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesReturnSummary
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class GetSalesReturnSummariesUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(transactionItemIds: List<Long>): Map<Long, SalesReturnSummary> {
        return salesTransactionRepository.getReturnSummaries(transactionItemIds)
    }
}
