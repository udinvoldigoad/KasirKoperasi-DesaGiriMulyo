package com.kasirkoperasi.app.feature.transaction.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.Product
import com.kasirkoperasi.app.domain.model.SalesTransactionDraftItem
import com.kasirkoperasi.app.domain.model.SalesTransactionPayment
import com.kasirkoperasi.app.domain.usecase.CompleteSalesTransactionUseCase
import com.kasirkoperasi.app.feature.transaction.state.CartItem
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

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update {
            val paidAmount = if (method == PaymentMethod.Debt) {
                it.paidAmount.coerceAtMost((it.totalAmount - 1).coerceAtLeast(0L))
            } else {
                it.paidAmount
            }

            it.copy(
                selectedPaymentMethod = method,
                paidAmountText = paidAmount.toPaymentText(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updatePaidAmount(value: String) {
        _uiState.update {
            val typedAmount = value.filter { char -> char.isDigit() }.toLongOrNull() ?: 0L
            val paidAmount = if (it.selectedPaymentMethod == PaymentMethod.Debt) {
                typedAmount.coerceAtMost((it.totalAmount - 1).coerceAtLeast(0L))
            } else {
                typedAmount
            }

            it.copy(
                paidAmountText = paidAmount.toPaymentText(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun useExactAmount() {
        _uiState.update {
            if (it.selectedPaymentMethod == PaymentMethod.Debt) {
                return@update it.copy(errorMessage = null, successMessage = null)
            }

            it.copy(
                paidAmountText = it.totalAmount.toString(),
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
            currentState.paidAmount >= currentState.totalAmount
        ) {
            _uiState.update { it.copy(errorMessage = "Nominal hutang harus kurang dari total") }
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

            runCatching {
                completeSalesTransactionUseCase(
                    items = draftItems,
                    payment = SalesTransactionPayment(
                        buyerName = currentState.buyerName,
                        paymentMethod = currentState.selectedPaymentMethod.label,
                        paidAmount = currentState.paidAmount,
                    ),
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        cartItems = emptyList(),
                        buyerName = "",
                        selectedPaymentMethod = PaymentMethod.Cash,
                        paidAmountText = "",
                        isSaving = false,
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
                successMessage = null,
            )
        }
    }

    private fun Long.toPaymentText(): String {
        return if (this <= 0L) "" else toString()
    }
}
