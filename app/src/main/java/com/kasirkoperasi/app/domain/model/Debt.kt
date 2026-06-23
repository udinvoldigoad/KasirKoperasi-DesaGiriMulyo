package com.kasirkoperasi.app.domain.model

data class DebtPayment(
    val id: Long = 0L,
    val buyerName: String,
    val buyerContact: String = "",
    val paymentMethod: String,
    val amount: Long,
    val note: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

data class DebtCustomerSummary(
    val buyerName: String,
    val buyerContact: String = "",
    val totalDebt: Long,
    val totalPaid: Long,
) {
    val remainingDebt: Long
        get() = (totalDebt - totalPaid).coerceAtLeast(0L)
}

data class DebtCustomerDetail(
    val summary: DebtCustomerSummary,
    val transactions: List<SalesTransaction>,
    val payments: List<DebtPayment>,
)
