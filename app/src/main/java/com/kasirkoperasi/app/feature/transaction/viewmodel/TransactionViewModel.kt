package com.kasirkoperasi.app.feature.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.SalesTransactionDraftItem
import com.kasirkoperasi.app.domain.model.SalesTransactionPayment
import com.kasirkoperasi.app.domain.usecase.CompleteSalesTransactionUseCase
import com.kasirkoperasi.app.feature.transaction.state.CartItem
import com.kasirkoperasi.app.feature.transaction.state.DebtInitialPaymentMethod
import com.kasirkoperasi.app.feature.transaction.state.PaymentMethod
import com.kasirkoperasi.app.feature.transaction.state.TransactionUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val completeSalesTransactionUseCase: CompleteSalesTransactionUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun addProduct(product: Product) {
        if (product.stockQuantity <= 0) {
            _uiState.update { it.copy(errorMessage = "Stok ${product.name} kosong") }
            return
        }

        val currentState = _uiState.value
        val existingItem = currentState.cartItems.firstOrNull { it.product.id == product.id }
        if (existingItem != null && existingItem.quantity >= product.stockQuantity) {
            _uiState.update { it.copy(errorMessage = "Stok ${product.name} tidak cukup") }
            return
        }

        val updatedCart = if (existingItem == null) {
            currentState.cartItems + CartItem(product = product, quantity = 1)
        } else {
            currentState.cartItems.map {
                if (it.product.id == product.id) {
                    it.copy(quantity = it.quantity + 1)
                } else {
                    it
                }
            }
        }

        _uiState.update {
            it.copy(
                cartItems = updatedCart,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun addProductByBarcode(rawBarcode: String, products: List<Product>) {
        val barcode = rawBarcode.toFourDigitBarcodeOrNull()
        if (barcode == null) {
            _uiState.update {
                it.copy(
                    searchQuery = "",
                    errorMessage = "Barcode tidak valid. Gunakan kode 4 angka.",
                    successMessage = null,
                )
            }
            return
        }

        val matchedProduct = products.firstOrNull { product ->
            product.barcode.toFourDigitBarcodeOrNull() == barcode
        }

        if (matchedProduct == null) {
            _uiState.update {
                it.copy(
                    searchQuery = barcode,
                    errorMessage = "Barang dengan barcode $barcode belum terdaftar",
                    successMessage = null,
                )
            }
            return
        }

        addProduct(matchedProduct)
        _uiState.update {
            it.copy(searchQuery = "")
        }
    }

    fun increaseQuantity(productId: Long) {
        val currentState = _uiState.value
        val selectedItem = currentState.cartItems.firstOrNull { it.product.id == productId } ?: return
        if (selectedItem.quantity >= selectedItem.product.stockQuantity) {
            _uiState.update { it.copy(errorMessage = "Stok ${selectedItem.product.name} tidak cukup") }
            return
        }

        val updatedCart = currentState.cartItems.map { item ->
            if (item.product.id == productId) {
                item.copy(quantity = item.quantity + 1)
            } else {
                item
            }
        }

        _uiState.update {
            it.copy(
                cartItems = updatedCart,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun decreaseQuantity(productId: Long) {
        _uiState.update { currentState ->
            val updatedCart = currentState.cartItems.mapNotNull { item ->
                if (item.product.id != productId) {
                    item
                } else if (item.quantity <= 1) {
                    null
                } else {
                    item.copy(quantity = item.quantity - 1)
                }
            }

            currentState.copy(
                cartItems = updatedCart,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun removeItem(productId: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                cartItems = currentState.cartItems.filterNot { it.product.id == productId },
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updateBuyerName(name: String) {
        _uiState.update {
            it.copy(
                buyerName = name,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updateBuyerContact(contact: String) {
        _uiState.update {
            it.copy(
                buyerContact = contact,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update {
            it.copy(
                selectedPaymentMethod = method,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun selectDebtInitialPaymentMethod(method: DebtInitialPaymentMethod) {
        _uiState.update {
            it.copy(
                selectedDebtInitialPaymentMethod = method,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updatePaidAmount(value: String) {
        _uiState.update {
            val typedAmount = value.filter { char -> char.isDigit() }.toLongOrNull() ?: 0L

            it.copy(
                paidAmountText = typedAmount.toPaymentText(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun useExactAmount() {
        _uiState.update {
            it.copy(
                paidAmountText = it.totalAmount.toPaymentText(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun completeTransaction() {
        val currentState = _uiState.value
        val currentCart = currentState.cartItems
        if (currentCart.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Keranjang masih kosong") }
            return
        }

        if (currentState.selectedPaymentMethod == PaymentMethod.Cash &&
            currentState.paidAmount < currentState.totalAmount
        ) {
            _uiState.update { it.copy(errorMessage = "Uang dibayarkan masih kurang") }
            return
        }

        if (currentState.selectedPaymentMethod == PaymentMethod.Debt &&
            currentState.buyerName.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Nama pembeli wajib diisi untuk transaksi hutang") }
            return
        }

        if (currentState.selectedPaymentMethod == PaymentMethod.Debt &&
            currentState.paidAmount >= currentState.totalAmount
        ) {
            _uiState.update { it.copy(errorMessage = "Transaksi sudah lunas. Gunakan metode Cash atau QRIS.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            val draftItems = currentCart.map {
                SalesTransactionDraftItem(
                    productId = it.product.id,
                    quantity = it.quantity,
                )
            }
            val paidAmount = if (currentState.selectedPaymentMethod == PaymentMethod.Qris) {
                currentState.totalAmount
            } else {
                currentState.paidAmount
            }
            val paidPaymentMethod = when {
                currentState.selectedPaymentMethod == PaymentMethod.Cash -> PaymentMethod.Cash.label
                currentState.selectedPaymentMethod == PaymentMethod.Qris -> PaymentMethod.Qris.label
                currentState.selectedPaymentMethod == PaymentMethod.Debt && paidAmount > 0L -> {
                    currentState.selectedDebtInitialPaymentMethod.label
                }
                else -> ""
            }

            runCatching {
                completeSalesTransactionUseCase(
                    items = draftItems,
                    payment = SalesTransactionPayment(
                        buyerName = currentState.buyerName,
                        buyerContact = if (currentState.selectedPaymentMethod == PaymentMethod.Debt) {
                            currentState.buyerContact
                        } else {
                            ""
                        },
                        paymentMethod = currentState.selectedPaymentMethod.label,
                        paidAmount = paidAmount,
                        paidPaymentMethod = paidPaymentMethod,
                    ),
                )
            }.onSuccess {
                val completedPaidAmount = paidAmount
                val completedChangeAmount = if (currentState.selectedPaymentMethod == PaymentMethod.Cash) {
                    (completedPaidAmount - currentState.totalAmount).coerceAtLeast(0L)
                } else {
                    0L
                }
                val completedDebtAmount = if (currentState.selectedPaymentMethod == PaymentMethod.Debt) {
                    (currentState.totalAmount - completedPaidAmount).coerceAtLeast(0L)
                } else {
                    0L
                }

                _uiState.update {
                    it.copy(
                        cartItems = emptyList(),
                        buyerName = "",
                        buyerContact = "",
                        selectedPaymentMethod = PaymentMethod.Cash,
                        paidAmountText = "",
                        isSaving = false,
                        completedItems = currentCart,
                        completedTotalAmount = currentState.totalAmount,
                        completedBuyerName = currentState.buyerName.trim(),
                        completedPaymentMethod = currentState.selectedPaymentMethod.label,
                        completedPaidPaymentMethod = paidPaymentMethod,
                        completedPaidAmount = completedPaidAmount,
                        completedChangeAmount = completedChangeAmount,
                        completedDebtAmount = completedDebtAmount,
                        successMessage = "Transaksi berhasil disimpan",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Gagal menyimpan transaksi",
                    )
                }
            }
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                completedItems = emptyList(),
                completedTotalAmount = 0L,
                completedBuyerName = "",
                completedPaymentMethod = PaymentMethod.Cash.label,
                completedPaidPaymentMethod = PaymentMethod.Cash.label,
                completedPaidAmount = 0L,
                completedChangeAmount = 0L,
                completedDebtAmount = 0L,
                successMessage = null,
            )
        }
    }

    private fun Long.toPaymentText(): String {
        if (this <= 0L) return ""

        return toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    }

    private fun String?.toFourDigitBarcodeOrNull(): String? {
        val rawCode = this?.trim().orEmpty()
        if (rawCode.isBlank()) return null
        if (!rawCode.all { it.isDigit() }) return null
        if (rawCode.length > 4) return null

        return rawCode.padStart(4, '0')
    }
}
