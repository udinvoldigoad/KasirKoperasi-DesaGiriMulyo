package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.ProductEntity
import com.kasirkoperasi.app.domain.model.Product

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    category = category,
    barcode = barcode,
    unit = unit,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    stockQuantity = stockQuantity,
    imageUri = imageUri,
    isActive = isActive,
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    category = category,
    barcode = barcode,
    unit = unit,
    purchasePrice = purchasePrice,
    sellingPrice = sellingPrice,
    stockQuantity = stockQuantity,
    imageUri = imageUri,
    isActive = isActive,
)
