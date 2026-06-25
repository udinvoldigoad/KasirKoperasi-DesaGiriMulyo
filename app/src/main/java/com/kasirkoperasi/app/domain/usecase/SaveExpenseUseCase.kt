package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.Expense
import com.kasirkoperasi.app.domain.repository.ExpenseRepository

class SaveExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(expense: Expense): Long {
        require(expense.title.isNotBlank()) { "Nama pengeluaran wajib diisi" }
        require(expense.amount > 0L) { "Nominal pengeluaran wajib lebih dari 0" }

        return expenseRepository.saveExpense(expense)
    }
}
