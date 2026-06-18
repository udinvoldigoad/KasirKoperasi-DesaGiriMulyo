package com.kasirkoperasi.app.domain.model

data class SalesTransaction(
    val id: Long = 0L,
    val transactionNumber: String,
    val buyerName: String,
    val paymentMethod: String,
    val totalAmount: Long,
    val totalProfit: Long,
    val paidAmount: Long,
    val changeAmount: Long,
    val debtAmount: Long,
    val itemCount: Int,
    val createdAtMillis: Long,
)

data class SalesTransactionItem(
    val id: Long = 0L,
    val transactionId: Long,
    val productId: Long,
    val productName: String,
    val category: String,
    val unit: String,
    val purchasePrice: Long,
    val sellingPrice: Long,
    val quantity: Int,
    val subtotal: Long,
    val profit: Long,
)

data class SalesTransactionDraftItem(
    val productId: Long,
    val quantity: Int,
)

data class SalesTransactionPayment(
    val buyerName: String,
    val paymentMethod: String,
    val paidAmount: Long,
)
