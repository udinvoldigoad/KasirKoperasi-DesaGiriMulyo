package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.repository.ProductRepository

class DeactivateProductUseCase(
    private val productRepository: ProductRepository,
) {
    suspend operator fun invoke(productId: Long) {
        productRepository.deactivateProduct(productId)
    }
}
