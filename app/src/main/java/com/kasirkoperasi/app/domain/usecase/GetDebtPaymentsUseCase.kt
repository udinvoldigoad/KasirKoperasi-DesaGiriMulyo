package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.DebtPayment
import com.kasirkoperasi.app.domain.repository.DebtRepository

class GetDebtPaymentsUseCase(
    private val debtRepository: DebtRepository,
) {
    suspend operator fun invoke(
        startDateMillis: Long,
        endDateMillis: Long,
        limit: Int = DEFAULT_LIMIT,
    ): List<DebtPayment> {
        return debtRepository.getPaymentsBetween(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
            limit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
        )
    }

    private companion object {
        const val MIN_LIMIT = 1
        const val DEFAULT_LIMIT = 500
        const val MAX_LIMIT = 1_000
    }
}
