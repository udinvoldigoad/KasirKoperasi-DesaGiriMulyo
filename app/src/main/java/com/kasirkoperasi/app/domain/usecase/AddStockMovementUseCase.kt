package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.StockMovement
import com.kasirkoperasi.app.domain.repository.StockRepository

class AddStockMovementUseCase(
    private val stockRepository: StockRepository,
) {
    suspend operator fun invoke(stockMovement: StockMovement): Long {
        return stockRepository.addStockMovement(stockMovement)
    }
}
