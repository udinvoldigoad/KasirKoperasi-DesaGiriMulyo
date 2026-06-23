package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.DebtPayment
import com.kasirkoperasi.app.domain.repository.DebtRepository

class RecordDebtPaymentUseCase(
    private val debtRepository: DebtRepository,
) {
    suspend operator fun invoke(payment: DebtPayment): Long {
        return debtRepository.recordPayment(payment)
    }
}
