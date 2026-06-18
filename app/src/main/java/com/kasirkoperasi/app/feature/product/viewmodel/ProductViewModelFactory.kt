package com.kasirkoperasi.app.feature.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.DeactivateProductUseCase
import com.kasirkoperasi.app.domain.usecase.GetProductsUseCase
import com.kasirkoperasi.app.domain.usecase.SaveProductUseCase
import com.kasirkoperasi.app.domain.usecase.UpdateProductWithStockInUseCase

class ProductViewModelFactory(
    private val getProductsUseCase: GetProductsUseCase,
    private val saveProductUseCase: SaveProductUseCase,
    private val updateProductWithStockInUseCase: UpdateProductWithStockInUseCase,
    private val deactivateProductUseCase: DeactivateProductUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            return ProductViewModel(
                getProductsUseCase = getProductsUseCase,
                saveProductUseCase = saveProductUseCase,
                updateProductWithStockInUseCase = updateProductWithStockInUseCase,
                deactivateProductUseCase = deactivateProductUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
