package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.SalesTransactionEntity
import com.kasirkoperasi.app.data.local.entity.SalesTransactionItemEntity

@Dao
interface SalesTransactionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: SalesTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItems(items: List<SalesTransactionItemEntity>)

    @Query(
        """
        SELECT * FROM sales_transactions
        WHERE created_at_millis BETWEEN :startDateMillis AND :endDateMillis
        ORDER BY created_at_millis DESC
        LIMIT :limit
        """,
    )
    suspend fun getTransactionsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesTransactionEntity>

    @Query(
        """
        SELECT * FROM sales_transaction_items
        WHERE transaction_id = :transactionId
        ORDER BY id ASC
        """,
    )
    suspend fun getItems(transactionId: Long): List<SalesTransactionItemEntity>
}
