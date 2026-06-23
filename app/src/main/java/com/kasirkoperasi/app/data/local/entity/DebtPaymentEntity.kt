package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debt_payments",
    indices = [
        Index(value = ["buyer_name"]),
        Index(value = ["buyer_name", "buyer_contact"]),
        Index(value = ["created_at_millis"]),
    ],
)
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "buyer_name")
    val buyerName: String,
    @ColumnInfo(name = "buyer_contact")
    val buyerContact: String = "",
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String,
    @ColumnInfo(name = "amount")
    val amount: Long,
    @ColumnInfo(name = "note")
    val note: String? = null,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
)
