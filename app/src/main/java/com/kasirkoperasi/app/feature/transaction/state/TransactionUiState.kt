package com.kasirkoperasi.app.feature.transaction.state

import com.kasirkoperasi.app.domain.model.Product

data class TransactionUiState(
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val buyerName: String = "",
    val buyerContact: String = "",
    val selectedPaymentMethod: PaymentMethod = PaymentMethod.Cash,
    val selectedDebtInitialPaymentMethod: DebtInitialPaymentMethod = DebtInitialPaymentMethod.Cash,
    val paidAmountText: String = "",
    val isSaving: Boolean = false,
    val completedItems: List<CartItem> = emptyList(),
    val completedTotalAmount: Long = 0L,
    val completedBuyerName: String = "",
    val completedPaymentMethod: String = PaymentMethod.Cash.label,
    val completedPaidPaymentMethod: String = PaymentMethod.Cash.label,
    val completedPaidAmount: Long = 0L,
    val completedChangeAmount: Long = 0L,
    val completedDebtAmount: Long = 0L,
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

    val debtAmount: Long
        get() = (totalAmount - paidAmount).coerceAtLeast(0L)
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
    Debt("Hutang"),
}

enum class DebtInitialPaymentMethod(
    val label: String,
) {
    Cash("Cash"),
    Qris("QRIS"),
}
