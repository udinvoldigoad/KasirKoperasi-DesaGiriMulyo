package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.Expense
import com.kasirkoperasi.app.domain.repository.ExpenseRepository

class GetExpensesUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int = DEFAULT_LIMIT,
    ): List<Expense> {
        return expenseRepository.getExpensesBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
        )
    }

    private companion object {
        const val MIN_LIMIT = 1
        const val DEFAULT_LIMIT = 100
        const val MAX_LIMIT = 1_000
    }
}
