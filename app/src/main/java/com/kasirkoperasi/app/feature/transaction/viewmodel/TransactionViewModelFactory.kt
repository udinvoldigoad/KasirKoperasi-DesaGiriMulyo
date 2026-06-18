package com.kasirkoperasi.app.feature.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.CompleteSalesTransactionUseCase

class TransactionViewModelFactory(
    private val completeSalesTransactionUseCase: CompleteSalesTransactionUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(
                completeSalesTransactionUseCase = completeSalesTransactionUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
