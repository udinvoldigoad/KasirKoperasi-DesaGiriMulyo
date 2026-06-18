package com.kasirkoperasi.app.feature.stock.state

import com.kasirkoperasi.app.domain.model.StockMovement

data class StockUiState(
    val isLoading: Boolean = false,
    val stockMovements: List<StockMovement> = emptyList(),
    val errorMessage: String? = null,
)
