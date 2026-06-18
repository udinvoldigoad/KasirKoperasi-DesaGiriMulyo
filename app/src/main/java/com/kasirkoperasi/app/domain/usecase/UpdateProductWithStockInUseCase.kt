package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.repository.ProductRepository

class UpdateProductWithStockInUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(product: Product, stockInQuantity: Int) {
        productRepository.updateProductWithStockIn(product, stockInQuantity)
    }
}
