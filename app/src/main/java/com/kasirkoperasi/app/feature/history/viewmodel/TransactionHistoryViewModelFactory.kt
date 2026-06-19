package com.kasirkoperasi.app.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase

class TransactionHistoryViewModelFactory(
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionHistoryViewModel::class.java)) {
            return TransactionHistoryViewModel(
                getSalesTransactionsUseCase = getSalesTransactionsUseCase,
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
