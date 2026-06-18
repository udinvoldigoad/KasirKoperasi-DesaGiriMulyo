package com.kasirkoperasi.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["barcode"], unique = true),
    ],
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "category")
    val category: String = "Obat",
    @ColumnInfo(name = "barcode")
    val barcode: String? = null,
    @ColumnInfo(name = "unit")
    val unit: String = "pcs",
    @ColumnInfo(name = "purchase_price")
    val purchasePrice: Long = 0L,
    @ColumnInfo(name = "selling_price")
    val sellingPrice: Long = 0L,
    @ColumnInfo(name = "stock_quantity")
    val stockQuantity: Int = 0,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at_millis")
    val updatedAtMillis: Long = System.currentTimeMillis(),
)
