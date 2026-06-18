package com.kasirkoperasi.app.domain.model

data class StockMovement(
    val id: Long = 0L,
    val productId: Long,
    val type: StockMovementType,
    val quantity: Int,
    val currentStock: Int,
    val note: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
)

enum class StockMovementType {
    IN,
    OUT,
    ADJUSTMENT,
}
