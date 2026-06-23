package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.StockMovement
import com.kasirkoperasi.app.domain.repository.StockRepository

class GetStockMovementsUseCase(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int = DEFAULT_LIMIT,
    ): List<StockMovement> {
        return stockRepository.getStockMovementsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
        )
    }

    private companion object {
        const val MIN_LIMIT = 1
        const val DEFAULT_LIMIT = 1_000
        const val MAX_LIMIT = 2_000
    }
}
