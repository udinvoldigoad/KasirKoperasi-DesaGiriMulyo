package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.ReportSummaryEntity

@Dao
interface ReportDao {
    @Query(
        """
        SELECT
            COALESCE(SUM(total_amount), 0) AS total_sales,
            COALESCE(SUM(total_profit), 0) AS total_profit,
            COALESCE(SUM(debt_amount), 0) AS total_debt,
            COALESCE(SUM(item_count), 0) AS sold_item_count,
            (
                SELECT COUNT(*)
                FROM products
                WHERE stock_quantity <= 5 AND is_active = 1
            ) AS low_stock_item_count
        FROM sales_transactions
        WHERE created_at_millis BETWEEN :startDateMillis AND :endDateMillis
        """,
    )
    suspend fun getSummary(startDateMillis: Long, endDateMillis: Long): ReportSummaryEntity
}
