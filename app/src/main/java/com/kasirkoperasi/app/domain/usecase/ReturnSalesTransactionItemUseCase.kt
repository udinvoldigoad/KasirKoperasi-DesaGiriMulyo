package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class ReturnSalesTransactionItemUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(
        transaction: SalesTransaction,
        item: SalesTransactionItem,
        quantity: Int,
    ): Long {
        return salesTransactionRepository.returnTransactionItem(
            transaction = transaction,
            item = item,
            quantity = quantity,
        )
    }
}
