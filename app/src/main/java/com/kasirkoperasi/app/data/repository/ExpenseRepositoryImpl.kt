package com.kasirkoperasi.app.data.repository

import com.kasirkoperasi.app.data.local.dao.ExpenseDao
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.data.mapper.toEntity
import com.kasirkoperasi.app.domain.model.Expense
import com.kasirkoperasi.app.domain.repository.ExpenseRepository

class ExpenseRepositoryImpl(
    private val expenseDao: ExpenseDao,
) : ExpenseRepository {
    override suspend fun saveExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun getExpensesBetween(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int,
    ): List<Expense> {
        return expenseDao.getExpensesBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit,
        ).map { it.toDomain() }
    }
}
