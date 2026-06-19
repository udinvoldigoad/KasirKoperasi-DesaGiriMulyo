package com.kasirkoperasi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kasirkoperasi.app.data.local.entity.ProductEntity

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE barcode = :barcode AND is_active = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :productId AND is_active = 1 LIMIT 1")
    suspend fun getProductById(productId: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: ProductEntity): Long

    @Query(
        """
        UPDATE products
        SET name = :name,
            category = :category,
            purchase_price = :purchasePrice,
            selling_price = :sellingPrice,
            stock_quantity = :stockQuantity,
            image_uri = :imageUri,
            updated_at_millis = :updatedAtMillis
        WHERE id = :productId
        """,
    )
    suspend fun updateProductDetails(
        productId: Long,
        name: String,
        category: String,
        purchasePrice: Long,
        sellingPrice: Long,
        stockQuantity: Int,
        imageUri: String?,
        updatedAtMillis: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE products
        SET stock_quantity = :stockQuantity,
            updated_at_millis = :updatedAtMillis
        WHERE id = :productId
        """,
    )
    suspend fun updateStockQuantity(
        productId: Long,
        stockQuantity: Int,
        updatedAtMillis: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE products
        SET is_active = 0,
            updated_at_millis = :updatedAtMillis
        WHERE id = :productId
        """,
    )
    suspend fun deactivateProduct(
        productId: Long,
        updatedAtMillis: Long = System.currentTimeMillis(),
    )
}
