package com.kasirkoperasi.app.data.repository

import androidx.room.withTransaction
import com.kasirkoperasi.app.data.local.dao.ProductDao
import com.kasirkoperasi.app.data.local.dao.StockDao
import com.kasirkoperasi.app.data.local.database.KasirDatabase
import com.kasirkoperasi.app.data.local.entity.StockMovementEntity
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.data.mapper.toEntity
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.ProductCategory
import com.kasirkoperasi.app.domain.model.StockMovementType
import com.kasirkoperasi.app.domain.repository.ProductRepository

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val stockDao: StockDao,
    private val database: KasirDatabase,
) : ProductRepository {
    override suspend fun getProducts(): List<Product> {
        return productDao.getAllProducts().map { it.toDomain() }
    }

    override suspend fun getProductsIncludingInactive(): List<Product> {
        return productDao.getAllProductsIncludingInactive().map { it.toDomain() }
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toDomain()
    }

    override suspend fun getProductByBarcodeIncludingInactive(barcode: String): Product? {
        return productDao.getProductByBarcodeIncludingInactive(barcode)?.toDomain()
    }

    override suspend fun saveProduct(product: Product): Long {
        return productDao.insertProduct(
            product.copy(category = ProductCategory.normalize(product.category)).toEntity(),
        )
    }

    override suspend fun updateProductMasterFromImport(product: Product) {
        require(product.id > 0L) { "Produk tidak valid" }
        productDao.updateProductMasterFromImport(
            productId = product.id,
            name = product.name,
            category = ProductCategory.normalize(product.category),
            unit = product.unit,
            purchasePrice = product.purchasePrice,
            sellingPrice = product.sellingPrice,
        )
    }

    override suspend fun updateProductWithStockIn(product: Product, stockInQuantity: Int) {
        require(product.id > 0L) { "Produk tidak valid" }
        require(stockInQuantity >= 0) { "Stok masuk tidak boleh minus" }

        database.withTransaction {
            val currentProduct = productDao.getProductById(product.id)
                ?: error("Produk tidak ditemukan")
            val updatedStock = currentProduct.stockQuantity + stockInQuantity
            val updatedAtMillis = System.currentTimeMillis()

            productDao.updateProductDetails(
                productId = product.id,
                name = product.name,
                category = ProductCategory.normalize(product.category),
                purchasePrice = product.purchasePrice,
                sellingPrice = product.sellingPrice,
                stockQuantity = updatedStock,
                imageUri = product.imageUri,
                updatedAtMillis = updatedAtMillis,
            )

            if (stockInQuantity > 0) {
                stockDao.insertStockMovement(
                    StockMovementEntity(
                        productId = product.id,
                        type = StockMovementType.IN.name,
                        quantity = stockInQuantity,
                        currentStock = updatedStock,
                        note = "Stok masuk dari edit barang",
                        createdAtMillis = updatedAtMillis,
                    ),
                )
            }
        }
    }

    override suspend fun deactivateProduct(productId: Long) {
        require(productId > 0L) { "Produk tidak valid" }
        productDao.deactivateProduct(productId)
    }
}
