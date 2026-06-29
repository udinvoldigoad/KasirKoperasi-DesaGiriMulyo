package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo

data class SalesReturnSummaryEntity(
    @ColumnInfo(name = "transaction_item_id")
    val transactionItemId: Long,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "refund_amount")
    val refundAmount: Long,
)

data class SalesReturnTransactionSummaryEntity(
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "refund_amount")
    val refundAmount: Long,
)

data class SalesReturnReportEntity(
    val id: Long,
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    @ColumnInfo(name = "transaction_item_id")
    val transactionItemId: Long,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "product_name")
    val productName: String,
    val unit: String,
    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Long,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Long,
    val quantity: Int,
    @ColumnInfo(name = "refund_amount")
    val refundAmount: Long,
    @ColumnInfo(name = "transaction_number")
    val transactionNumber: String,
    @ColumnInfo(name = "buyer_name")
    val buyerName: String,
    @ColumnInfo(name = "buyer_contact")
    val buyerContact: String,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    @ColumnInfo(name = "paid_payment_method")
    val paidPaymentMethod: String,
    @ColumnInfo(name = "paid_amount")
    val transactionPaidAmount: Long,
    @ColumnInfo(name = "change_amount")
    val transactionChangeAmount: Long,
    @ColumnInfo(name = "debt_amount")
    val transactionDebtAmount: Long,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
)
