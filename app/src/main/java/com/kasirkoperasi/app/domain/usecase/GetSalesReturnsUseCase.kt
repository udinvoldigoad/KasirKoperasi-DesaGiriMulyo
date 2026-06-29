package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesReturn
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class GetSalesReturnsUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int = DEFAULT_LIMIT,
    ): List<SalesReturn> {
        return salesTransactionRepository.getReturnsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit,
        )
    }

    private companion object {
        const val DEFAULT_LIMIT = 1_000
    }
}
