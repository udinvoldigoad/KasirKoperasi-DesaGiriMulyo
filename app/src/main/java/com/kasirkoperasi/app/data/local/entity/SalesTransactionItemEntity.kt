package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales_transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = SalesTransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["transaction_id"]),
        Index(value = ["product_id"]),
    ],
)
data class SalesTransactionItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "product_name")
    val productName: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "unit")
    val unit: String,
    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Long,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Long,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "subtotal")
    val subtotal: Long,
    @ColumnInfo(name = "profit")
    val profit: Long,
)
