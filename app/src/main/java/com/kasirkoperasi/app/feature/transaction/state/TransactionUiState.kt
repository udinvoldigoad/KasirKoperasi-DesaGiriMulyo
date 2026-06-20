package com.kasirkoperasi.app.feature.transaction.state

import com.kasirkoperasi.app.domain.model.Product

data class TransactionUiState(
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val buyerName: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.Cash,
    val paidAmountText: String = "",
    val isSaving: Boolean = false,
    val completedItems: List<CartItem> = emptyList(),
    val completedTotalAmount: Long = 0L,
    val completedBuyerName: String = "",
    val completedPaymentMethod: String = PaymentMethod.Cash.label,
    val completedPaidAmount: Long = 0L,
    val completedChangeAmount: Long = 0L,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val totalAmount: Long
        get() = cartItems.sumOf { it.subtotal }

    val itemCount: Int
        get() = cartItems.sumOf { it.quantity }

    val paidAmount: Long
        get() = paidAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L

    val changeAmount: Long
        get() = (paidAmount - totalAmount).coerceAtLeast(0L)
}

data class CartItem(
    val product: Product,
    val quantity: Int,
) {
    val subtotal: Long
        get() = product.sellingPrice * quantity
}

enum class PaymentMethod(
    val label: String,
) {
    Cash("Cash"),
    Qris("QRIS"),
}
