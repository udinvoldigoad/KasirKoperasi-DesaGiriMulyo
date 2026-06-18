package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.repository.ProductRepository

class SaveProductUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(product: Product): Long {
        return productRepository.saveProduct(product)
    }
}
