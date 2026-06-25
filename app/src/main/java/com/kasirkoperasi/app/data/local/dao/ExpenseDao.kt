package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.ExpenseEntity

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Query(
        """
        SELECT * FROM expenses
        WHERE created_at_millis BETWEEN :startDateMillis AND :endDateMillis
        ORDER BY created_at_millis DESC
        LIMIT :limit
        """,
    )
    suspend fun getExpensesBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<ExpenseEntity>
}
