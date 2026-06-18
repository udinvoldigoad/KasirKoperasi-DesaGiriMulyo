package com.kasirkoperasi.app.domain.model

data class Product(
    val id: Long = 0L,
    val name: String,
    val category: String = ProductCategory.DEFAULT,
    val barcode: String? = null,
    val unit: String = "pcs",
    val purchasePrice: Long = 0L,
    val sellingPrice: Long = 0L,
    val stockQuantity: Int = 0,
    val isActive: Boolean = true,
)
