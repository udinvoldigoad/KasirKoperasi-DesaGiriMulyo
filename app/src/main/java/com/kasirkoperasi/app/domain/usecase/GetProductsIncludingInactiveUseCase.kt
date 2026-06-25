package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.repository.ProductRepository

class GetProductsIncludingInactiveUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(): List<Product> {
        return productRepository.getProductsIncludingInactive()
    }
}
