package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.StockMovement

interface StockRepository {
    suspend fun getStockMovements(productId: Long? = null): List<StockMovement>

    suspend fun getStockMovementsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<StockMovement>

    suspend fun addStockMovement(stockMovement: StockMovement): Long
}
