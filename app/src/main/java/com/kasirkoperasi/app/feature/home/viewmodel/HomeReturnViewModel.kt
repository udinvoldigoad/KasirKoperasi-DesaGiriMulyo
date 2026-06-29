package com.kasirkoperasi.app.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.model.SalesTransactionItem
import com.kasirkoperasi.app.domain.usecase.GetReturnedQuantityUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionItemsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.ReturnSalesTransactionItemUseCase
import com.kasirkoperasi.app.feature.home.state.HomeReturnUiState
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeReturnViewModel(
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
    private val getSalesTransactionItemsUseCase: GetSalesTransactionItemsUseCase,
    private val getReturnedQuantityUseCase: GetReturnedQuantityUseCase,
    private val returnSalesTransactionItemUseCase: ReturnSalesTransactionItemUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeReturnUiState())
    val uiState: StateFlow<HomeReturnUiState> = _uiState.asStateFlow()

    fun loadRecentTransactions() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedTransaction = null,
                    selectedItems = emptyList(),
                    selectedReturnItem = null,
                    returnedQuantity = 0,
                    successMessage = null,
                    errorMessage = null,
                )
            }

            runCatching {
                val (startDateMillis, endDateMillis) = lastSevenDaysRange()
                getSalesTransactionsUseCase(
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                    limit = RECENT_TRANSACTION_LIMIT,
                )
            }.onSuccess { transactions ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transactions = transactions,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Gagal memuat riwayat transaksi",
                    )
                }
            }
        }
    }

    fun selectTransaction(transaction: SalesTransaction) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedTransaction = transaction,
                    selectedItems = emptyList(),
                    selectedReturnItem = null,
                    returnedQuantity = 0,
                    successMessage = null,
                    errorMessage = null,
                )
            }

            runCatching {
                getSalesTransactionItemsUseCase(transaction.id)
            }.onSuccess { items ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedItems = items,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Gagal memuat barang transaksi",
                    )
                }
            }
        }
    }

    fun selectReturnItem(item: SalesTransactionItem) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedReturnItem = item,
                    returnedQuantity = 0,
                    successMessage = null,
                    errorMessage = null,
                )
            }

            runCatching {
                getReturnedQuantityUseCase(item.id)
            }.onSuccess { returnedQuantity ->
                _uiState.update {
                    it.copy(
                        returnedQuantity = returnedQuantity,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        errorMessage = throwable.message ?: "Gagal mengecek data retur",
                    )
                }
            }
        }
    }

    fun processSelectedReturn(onReturnSaved: () -> Unit = {}) {
        val currentState = _uiState.value
        val transaction = currentState.selectedTransaction
        val item = currentState.selectedReturnItem
        val remainingQuantity = currentState.remainingReturnQuantity
        val quantity = remainingQuantity

        if (transaction == null || item == null) {
            _uiState.update { it.copy(errorMessage = "Pilih barang yang akan diretur") }
            return
        }
        if (quantity <= 0) {
            _uiState.update { it.copy(errorMessage = "Barang ini sudah tidak memiliki sisa retur") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessingReturn = true,
                    successMessage = null,
                    errorMessage = null,
                )
            }

            runCatching {
                returnSalesTransactionItemUseCase(
                    transaction = transaction,
                    item = item,
                    quantity = quantity,
                )
                ReturnProcessResult(
                    items = getSalesTransactionItemsUseCase(transaction.id),
                    returnedQuantity = getReturnedQuantityUseCase(item.id),
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isProcessingReturn = false,
                        selectedItems = result.items,
                        returnedQuantity = result.returnedQuantity,
                        successMessage = "Retur ${item.productName} berhasil diproses",
                        errorMessage = null,
                    )
                }
                onReturnSaved()
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isProcessingReturn = false,
                        errorMessage = throwable.message ?: "Gagal memproses retur",
                    )
                }
            }
        }
    }

    fun backToTransactionList() {
        _uiState.update {
            it.copy(
                selectedTransaction = null,
                selectedItems = emptyList(),
                selectedReturnItem = null,
                returnedQuantity = 0,
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(
                selectedTransaction = null,
                selectedItems = emptyList(),
                selectedReturnItem = null,
                returnedQuantity = 0,
                isProcessingReturn = false,
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    private fun lastSevenDaysRange(): Pair<Long, Long> {
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val start = end.clone() as Calendar
        start.add(Calendar.DAY_OF_YEAR, -6)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        start.set(Calendar.MILLISECOND, 0)

        return start.timeInMillis to end.timeInMillis
    }

    private companion object {
        const val RECENT_TRANSACTION_LIMIT = 50
    }

    private data class ReturnProcessResult(
        val items: List<SalesTransactionItem>,
        val returnedQuantity: Int,
    )
}
