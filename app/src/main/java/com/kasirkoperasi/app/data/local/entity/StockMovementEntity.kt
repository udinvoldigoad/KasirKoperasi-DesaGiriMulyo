package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_movements",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["product_id"]),
    ],
)
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "product_id")
    val productId: Long,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "quantity")
    val quantity: Int,
    @ColumnInfo(name = "current_stock")
    val currentStock: Int,
    @ColumnInfo(name = "note")
    val note: String? = null,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
)
