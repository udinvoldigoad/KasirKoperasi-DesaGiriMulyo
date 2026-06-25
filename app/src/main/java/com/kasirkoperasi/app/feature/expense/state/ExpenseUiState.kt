package com.kasirkoperasi.app.feature.expense.state

import com.kasirkoperasi.app.domain.model.Expense

data class ExpenseUiState(
    val title: String = "",
    val amountText: String = "",
    val note: String = "",
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val totalExpense: Long
        get() = expenses.sumOf { it.amount }

    val amount: Long
        get() = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
}
