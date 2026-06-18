package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class GetSalesTransactionsUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(
        startDateMillis: Long,
        endDateMillis: Long,
    ): List<SalesTransaction> {
        return salesTransactionRepository.getTransactionsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )
    }
}
