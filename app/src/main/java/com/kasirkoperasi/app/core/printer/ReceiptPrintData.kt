package com.kasirkoperasi.app.core.printer

data class ReceiptPrintData(
    val storeName: String,
    val buyerName: String,
    val paymentMethod: String,
    val paidPaymentMethod: String,
    val paidAmount: Long,
    val changeAmount: Long,
    val debtAmount: Long,
    val totalAmount: Long,
    val items: List<ReceiptPrintItem>,
    val printedAtMillis: Long = System.currentTimeMillis(),
)

data class ReceiptPrintItem(
    val name: String,
    val quantity: Int,
    val unit: String,
    val sellingPrice: Long,
    val subtotal: Long,
)
