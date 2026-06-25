package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.Expense

interface ExpenseRepository {
    suspend fun saveExpense(expense: Expense): Long

    suspend fun getExpensesBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<Expense>
}
