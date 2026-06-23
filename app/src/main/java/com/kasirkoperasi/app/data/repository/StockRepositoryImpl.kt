package com.kasirkoperasi.app.data.repository

import com.kasirkoperasi.app.data.local.dao.StockDao
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.data.mapper.toEntity
import com.kasirkoperasi.app.domain.model.StockMovement
import com.kasirkoperasi.app.domain.repository.StockRepository

class StockRepositoryImpl(
    private val stockDao: StockDao,
) : StockRepository {
    override suspend fun getStockMovements(productId: Long?): List<StockMovement> {
        return stockDao.getStockMovements(productId).map { it.toDomain() }
    }

    override suspend fun getStockMovementsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<StockMovement> {
        return stockDao.getStockMovementsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit,
        ).map { it.toDomain() }
    }

    override suspend fun addStockMovement(stockMovement: StockMovement): Long {
        return stockDao.insertStockMovement(stockMovement.toEntity())
    }
}
