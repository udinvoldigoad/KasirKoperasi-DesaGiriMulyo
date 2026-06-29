package com.kasirkoperasi.app.domain.model

data class SalesReturn(
    val id: Long = 0L,
    val transactionId: Long,
    val transactionItemId: Long,
    val productId: Long,
    val productName: String,
    val unit: String,
    val purchasePrice: Long,
    val sellingPrice: Long,
    val quantity: Int,
    val refundAmount: Long,
    val transactionNumber: String,
    val buyerName: String,
    val buyerContact: String,
    val paymentMethod: String,
    val paidPaymentMethod: String,
    val transactionPaidAmount: Long,
    val transactionChangeAmount: Long,
    val transactionDebtAmount: Long,
    val createdAtMillis: Long,
) {
    val profitReduction: Long
        get() = (sellingPrice - purchasePrice) * quantity
}

data class SalesReturnSummary(
    val transactionItemId: Long,
    val quantity: Int,
    val refundAmount: Long,
)
