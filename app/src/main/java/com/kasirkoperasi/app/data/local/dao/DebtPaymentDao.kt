package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.DebtPaymentEntity

@Dao
interface DebtPaymentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertDebtPayment(payment: DebtPaymentEntity): Long

    @Query(
        """
        SELECT * FROM debt_payments
        WHERE created_at_millis BETWEEN :startDateMillis AND :endDateMillis
        ORDER BY created_at_millis DESC
        LIMIT :limit
        """,
    )
    suspend fun getPaymentsBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<DebtPaymentEntity>

    @Query(
        """
        SELECT * FROM debt_payments
        ORDER BY created_at_millis DESC
        """,
    )
    suspend fun getAllPayments(): List<DebtPaymentEntity>
}
