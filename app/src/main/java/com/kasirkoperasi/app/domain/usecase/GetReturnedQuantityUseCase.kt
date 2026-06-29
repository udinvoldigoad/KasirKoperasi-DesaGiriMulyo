package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.repository.SalesTransactionRepository

class GetReturnedQuantityUseCase(
    private val salesTransactionRepository: SalesTransactionRepository,
) {
    suspend operator fun invoke(transactionItemId: Long): Int {
        return salesTransactionRepository.getReturnedQuantity(transactionItemId)
    }
}
