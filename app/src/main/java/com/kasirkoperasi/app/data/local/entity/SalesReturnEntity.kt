package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_returns",
    foreignKeys = [
        ForeignKey(
            entity = SalesTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SalesTransactionItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_item_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["transaction_item_id"]),
        Index(value = ["product_id"]),
        Index(value = ["created_at_millis"]),
    ],
)
data class SalesReturnEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    @ColumnInfo(name = "transaction_item_id")
    val transactionItemId: Long,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "refund_amount")
    val refundAmount: Long,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
)
