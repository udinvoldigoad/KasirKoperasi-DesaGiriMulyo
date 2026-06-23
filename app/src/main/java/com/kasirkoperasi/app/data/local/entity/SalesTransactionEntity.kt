package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_transactions",
    indices = [
        Index(value = ["transaction_number"], unique = true),
        Index(value = ["created_at_millis"]),
    ],
)
data class SalesTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "transaction_number")
    val transactionNumber: String,
    @ColumnInfo(name = "buyer_name")
    val buyerName: String,
    @ColumnInfo(name = "buyer_contact")
    val buyerContact: String = "",
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    @ColumnInfo(name = "paid_payment_method")
    val paidPaymentMethod: String = "",
    @ColumnInfo(name = "total_amount")
    val totalAmount: Long,
    @ColumnInfo(name = "total_profit")
    val totalProfit: Long,
    @ColumnInfo(name = "paid_amount")
    val paidAmount: Long,
    @ColumnInfo(name = "change_amount")
    val changeAmount: Long,
    @ColumnInfo(name = "debt_amount")
    val debtAmount: Long = 0L,
    @ColumnInfo(name = "item_count")
    val itemCount: Int,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
)
