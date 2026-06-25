package com.kasirkoperasi.app.feature.expense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.GetExpensesUseCase
import com.kasirkoperasi.app.domain.usecase.SaveExpenseUseCase

class ExpenseViewModelFactory(
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            return ExpenseViewModel(
                saveExpenseUseCase = saveExpenseUseCase,
                getExpensesUseCase = getExpensesUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
