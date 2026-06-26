package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>

    suspend fun getProductsIncludingInactive(): List<Product>

    suspend fun getProductByBarcode(barcode: String): Product?

    suspend fun getProductByBarcodeIncludingInactive(barcode: String): Product?

    suspend fun saveProduct(product: Product): Long

    suspend fun updateProductMasterFromImport(product: Product)

    suspend fun updateProductWithStockIn(product: Product, stockInQuantity: Int)

    suspend fun deactivateProduct(productId: Long)
}
