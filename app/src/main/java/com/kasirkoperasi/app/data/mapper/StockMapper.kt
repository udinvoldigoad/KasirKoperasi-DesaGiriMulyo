package com.kasirkoperasi.app.data.mapper

import com.kasirkoperasi.app.data.local.entity.StockMovementEntity
import com.kasirkoperasi.app.domain.model.StockMovement
import com.kasirkoperasi.app.domain.model.StockMovementType

fun StockMovementEntity.toDomain(): StockMovement = StockMovement(
    id = id,
    productId = productId,
    type = StockMovementType.valueOf(type),
    quantity = quantity,
    currentStock = currentStock,
    note = note,
    createdAtMillis = createdAtMillis,
)

fun StockMovement.toEntity(): StockMovementEntity = StockMovementEntity(
    id = id,
    productId = productId,
    type = type.name,
    quantity = quantity,
    currentStock = currentStock,
    note = note,
    createdAtMillis = createdAtMillis,
)
