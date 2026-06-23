package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class GetDebtTransactionsUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int = DEFAULT_LIMIT,
    ): List<SalesTransaction> {
        return salesTransactionRepository.getDebtTransactionsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
        )
    }

    private companion object {
        const val MIN_LIMIT = 1
        const val DEFAULT_LIMIT = 500
        const val MAX_LIMIT = 1_000
    }
}
