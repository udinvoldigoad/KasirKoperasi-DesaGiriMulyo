package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.DebtCustomerDetail
import com.kasirkoperasi.app.domain.repository.DebtRepository

class GetDebtCustomerDetailUseCase(
    private val debtRepository: DebtRepository,
) {
    suspend operator fun invoke(
        buyerName: String,
        buyerContact: String,
    ): DebtCustomerDetail {
        return debtRepository.getCustomerDetail(
            buyerName = buyerName,
            buyerContact = buyerContact,
        )
    }
}
