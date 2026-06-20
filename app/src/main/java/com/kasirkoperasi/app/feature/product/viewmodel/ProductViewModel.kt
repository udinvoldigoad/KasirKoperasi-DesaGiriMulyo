package com.kasirkoperasi.app.feature.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.ProductCategory
import com.kasirkoperasi.app.domain.usecase.DeactivateProductUseCase
import com.kasirkoperasi.app.domain.usecase.GetProductsUseCase
import com.kasirkoperasi.app.domain.usecase.SaveProductUseCase
import com.kasirkoperasi.app.domain.usecase.UpdateProductWithStockInUseCase
import com.kasirkoperasi.app.feature.product.state.ProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductViewModel(
    private val getProductsUseCase: GetProductsUseCase,
    private val saveProductUseCase: SaveProductUseCase,
    private val updateProductWithStockInUseCase: UpdateProductWithStockInUseCase,
    private val deactivateProductUseCase: DeactivateProductUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null,
                    imageUriToDelete = null,
                )
            }

            runCatching {
                getProductsUseCase()
            }.onSuccess { products ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        products = products,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Gagal memuat data barang",
                    )
                }
            }
        }
    }

    fun saveProduct(
        name: String,
        category: String,
        barcode: String,
        unit: String,
        purchasePrice: String,
        sellingPrice: String,
        stockQuantity: String,
        imageUri: String,
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Nama barang wajib diisi") }
            return
        }

        if (purchasePrice.hasNegativeSign() || sellingPrice.hasNegativeSign() || stockQuantity.hasNegativeSign()) {
            _uiState.update { it.copy(errorMessage = "Harga dan stok tidak boleh negatif") }
            return
        }

        val normalizedCategory = ProductCategory.normalize(category)
        val cleanUnit = unit.trim().ifEmpty { "pcs" }
        val cleanBarcodeInput = barcode.trim()
        val normalizedBarcode = cleanBarcodeInput.toFourDigitBarcodeOrNull()
        val parsedPurchasePrice = purchasePrice.onlyDigits().toLongOrNull() ?: 0L
        val parsedSellingPrice = sellingPrice.onlyDigits().toLongOrNull() ?: 0L
        val parsedStockQuantity = stockQuantity.onlyDigits().toIntOrNull() ?: 0

        if (cleanBarcodeInput.isNotBlank() && normalizedBarcode == null) {
            _uiState.update { it.copy(errorMessage = "Barcode harus angka maksimal 4 digit") }
            return
        }

        if (parsedSellingPrice <= 0L) {
            _uiState.update { it.copy(errorMessage = "Harga jual wajib lebih dari 0") }
            return
        }

        if (parsedPurchasePrice > parsedSellingPrice) {
            _uiState.update { it.copy(errorMessage = "Harga beli tidak boleh lebih besar dari harga jual") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    imageUriToDelete = null,
                )
            }

            val product = Product(
                name = cleanName,
                category = normalizedCategory,
                barcode = normalizedBarcode,
                unit = cleanUnit,
                purchasePrice = parsedPurchasePrice,
                sellingPrice = parsedSellingPrice,
                stockQuantity = parsedStockQuantity,
                imageUri = imageUri.ifBlank { null },
            )

            runCatching {
                saveProductUseCase(product)
                getProductsUseCase()
            }.onSuccess { products ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        products = products,
                        successMessage = "Barang berhasil disimpan",
                        imageUriToDelete = null,
                    )
                }
            }.onFailure {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        errorMessage = "Gagal menyimpan barang. Periksa barcode atau data yang diisi.",
                    )
                }
            }
        }
    }

    fun updateProductWithStockIn(
        product: Product,
        name: String,
        category: String,
        purchasePrice: String,
        sellingPrice: String,
        stockInQuantity: String,
        imageUri: String,
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Nama barang wajib diisi") }
            return
        }

        if (purchasePrice.hasNegativeSign() || sellingPrice.hasNegativeSign() || stockInQuantity.hasNegativeSign()) {
            _uiState.update { it.copy(errorMessage = "Harga dan stok masuk tidak boleh negatif") }
            return
        }

        val normalizedCategory = ProductCategory.normalize(category)
        val parsedPurchasePrice = purchasePrice.onlyDigits().toLongOrNull() ?: 0L
        val parsedSellingPrice = sellingPrice.onlyDigits().toLongOrNull() ?: 0L
        val parsedStockInQuantity = stockInQuantity.onlyDigits().toIntOrNull() ?: 0

        if (parsedSellingPrice <= 0L) {
            _uiState.update { it.copy(errorMessage = "Harga jual wajib lebih dari 0") }
            return
        }

        if (parsedPurchasePrice > parsedSellingPrice) {
            _uiState.update { it.copy(errorMessage = "Harga beli tidak boleh lebih besar dari harga jual") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    imageUriToDelete = null,
                )
            }

            val updatedProduct = product.copy(
                name = cleanName,
                category = normalizedCategory,
                purchasePrice = parsedPurchasePrice,
                sellingPrice = parsedSellingPrice,
                imageUri = imageUri.ifBlank { null },
            )
            val oldImageUri = product.imageUri
            val newImageUri = updatedProduct.imageUri
            val imageUriToDelete = oldImageUri
                ?.takeIf { it.isNotBlank() && it != newImageUri }

            runCatching {
                updateProductWithStockInUseCase(
                    product = updatedProduct,
                    stockInQuantity = parsedStockInQuantity,
                )
                getProductsUseCase()
            }.onSuccess { products ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        products = products,
                        successMessage = "Barang berhasil diperbarui",
                        imageUriToDelete = imageUriToDelete,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Gagal memperbarui barang",
                    )
                }
            }
        }
    }

    fun deleteProduct(product: Product) {
        if (product.id <= 0L) {
            _uiState.update { it.copy(errorMessage = "Produk tidak valid") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                    imageUriToDelete = null,
                )
            }

            runCatching {
                deactivateProductUseCase(product.id)
                getProductsUseCase()
            }.onSuccess { products ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        products = products,
                        successMessage = "Barang berhasil dihapus",
                        imageUriToDelete = product.imageUri,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Gagal menghapus barang",
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null,
                imageUriToDelete = null,
            )
        }
    }

    fun clearImageDeletionRequest() {
        _uiState.update {
            it.copy(imageUriToDelete = null)
        }
    }

    private fun String.onlyDigits(): String {
        return filter { it.isDigit() }
    }

    private fun String.hasNegativeSign(): Boolean {
        return trim().startsWith("-")
    }

    private fun String.toFourDigitBarcodeOrNull(): String? {
        val rawCode = trim()
        if (rawCode.isBlank()) return null
        if (!rawCode.all { it.isDigit() }) return null
        if (rawCode.length > 4) return null

        return rawCode.padStart(4, '0')
    }
}
