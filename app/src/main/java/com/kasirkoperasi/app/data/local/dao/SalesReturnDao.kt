package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.SalesReturnReportEntity
import com.kasirkoperasi.app.data.local.entity.SalesReturnEntity
import com.kasirkoperasi.app.data.local.entity.SalesReturnSummaryEntity
import com.kasirkoperasi.app.data.local.entity.SalesReturnTransactionSummaryEntity

@Dao
interface SalesReturnDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReturn(returnEntity: SalesReturnEntity): Long

    @Query(
        """
        SELECT COALESCE(SUM(quantity), 0)
        FROM sales_returns
        WHERE transaction_item_id = :transactionItemId
        """,
    )
    suspend fun getReturnedQuantity(transactionItemId: Long): Int

    @Query(
        """
        SELECT
            transaction_item_id,
            COALESCE(SUM(quantity), 0) AS quantity,
            COALESCE(SUM(refund_amount), 0) AS refund_amount
        FROM sales_returns
        WHERE transaction_item_id IN (:transactionItemIds)
        GROUP BY transaction_item_id
        """,
    )
    suspend fun getReturnSummariesByItemIds(
        transactionItemIds: List<Long>,
    ): List<SalesReturnSummaryEntity>

    @Query(
        """
        SELECT
            transaction_id,
            COALESCE(SUM(quantity), 0) AS quantity,
            COALESCE(SUM(refund_amount), 0) AS refund_amount
        FROM sales_returns
        WHERE transaction_id IN (:transactionIds)
        GROUP BY transaction_id
        """,
    )
    suspend fun getReturnSummariesByTransactionIds(
        transactionIds: List<Long>,
    ): List<SalesReturnTransactionSummaryEntity>

    @Query(
        """
        SELECT
            sr.id AS id,
            sr.transaction_id AS transaction_id,
            sr.transaction_item_id AS transaction_item_id,
            sr.product_id AS product_id,
            sr.product_name AS product_name,
            sti.unit AS unit,
            sti.purchase_price AS purchase_price,
            sti.selling_price AS selling_price,
            sr.quantity AS quantity,
            sr.refund_amount AS refund_amount,
            st.transaction_number AS transaction_number,
            st.buyer_name AS buyer_name,
            st.buyer_contact AS buyer_contact,
            st.payment_method AS payment_method,
            st.paid_payment_method AS paid_payment_method,
            st.paid_amount AS paid_amount,
            st.change_amount AS change_amount,
            st.debt_amount AS debt_amount,
            sr.created_at_millis AS created_at_millis
        FROM sales_returns sr
        INNER JOIN sales_transaction_items sti ON sti.id = sr.transaction_item_id
        INNER JOIN sales_transactions st ON st.id = sr.transaction_id
        WHERE sr.created_at_millis BETWEEN :startDateMillis AND :endDateMillis
        ORDER BY sr.created_at_millis DESC
        LIMIT :limit
        """,
    )
    suspend fun getReturnsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<SalesReturnReportEntity>
}
