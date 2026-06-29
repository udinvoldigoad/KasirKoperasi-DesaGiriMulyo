package com.kasirkoperasi.app.feature.home.state

import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem

data class HomeReturnUiState(
    val isLoading: Boolean = false,
    val transactions: List<SalesTransaction> = emptyList(),
    val selectedTransaction: SalesTransaction? = null,
    val selectedItems: List<SalesTransactionItem> = emptyList(),
    val selectedReturnItem: SalesTransactionItem? = null,
    val returnedQuantity: Int = 0,
    val isProcessingReturn: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    val remainingReturnQuantity: Int
        get() = ((selectedReturnItem?.quantity ?: 0) - returnedQuantity).coerceAtLeast(0)
}
