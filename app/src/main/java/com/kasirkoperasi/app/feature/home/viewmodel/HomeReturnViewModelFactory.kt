package com.kasirkoperasi.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.GetReturnedQuantityUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionItemsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.ReturnSalesTransactionItemUseCase

class HomeReturnViewModelFactory(
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
    private val getSalesTransactionItemsUseCase: GetSalesTransactionItemsUseCase,
    private val getReturnedQuantityUseCase: GetReturnedQuantityUseCase,
    private val returnSalesTransactionItemUseCase: ReturnSalesTransactionItemUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeReturnViewModel::class.java)) {
            return HomeReturnViewModel(
                getSalesTransactionsUseCase = getSalesTransactionsUseCase,
                getSalesTransactionItemsUseCase = getSalesTransactionItemsUseCase,
                getReturnedQuantityUseCase = getReturnedQuantityUseCase,
                returnSalesTransactionItemUseCase = returnSalesTransactionItemUseCase,
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
