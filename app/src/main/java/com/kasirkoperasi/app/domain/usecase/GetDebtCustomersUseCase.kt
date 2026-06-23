package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.DebtCustomerSummary
import com.kasirkoperasi.app.domain.repository.DebtRepository

class GetDebtCustomersUseCase(
    private val debtRepository: DebtRepository,
) {
    suspend operator fun invoke(): List<DebtCustomerSummary> {
        return debtRepository.getOutstandingCustomers()
    }
}
