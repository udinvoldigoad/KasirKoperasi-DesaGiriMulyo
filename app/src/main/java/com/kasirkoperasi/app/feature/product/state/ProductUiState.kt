package com.kasirkoperasi.app.feature.product.state

import com.kasirkoperasi.app.domain.model.Product

data class ProductUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val imageUriToDelete: String? = null,
)
