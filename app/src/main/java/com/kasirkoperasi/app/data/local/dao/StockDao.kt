package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.StockMovementEntity

@Dao
interface StockDao {
    @Query(
        """
        SELECT * FROM stock_movements
        WHERE (:productId IS NULL OR product_id = :productId)
        ORDER BY created_at_millis DESC
        """,
    )
    suspend fun getStockMovements(productId: Long?): List<StockMovementEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStockMovement(stockMovement: StockMovementEntity): Long
}
