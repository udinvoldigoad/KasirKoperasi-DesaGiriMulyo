package com.kasirkoperasi.app.feature.history.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.SalesTransaction
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionItemsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.feature.history.state.TransactionHistoryRange
import com.kasirkoperasi.app.feature.history.state.TransactionHistoryUiState
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionHistoryViewModel(
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
    private val getSalesTransactionItemsUseCase: GetSalesTransactionItemsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    fun selectRange(range: TransactionHistoryRange) {
        loadTransactions(range)
    }

    fun loadTransactions(
        range: TransactionHistoryRange = _uiState.value.selectedRange,
    ) {
        val (startDateMillis, endDateMillis) = range.toMillisRange()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    selectedRange = range,
                    selectedTransaction = null,
                    selectedTransactionItems = emptyList(),
                    detailErrorMessage = null,
                    errorMessage = null,
                )
            }

            runCatching {
                getSalesTransactionsUseCase(
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
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

    fun openTransactionDetail(transaction: SalesTransaction) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedTransaction = transaction,
                    selectedTransactionItems = emptyList(),
                    isDetailLoading = true,
                    detailErrorMessage = null,
                )
            }

            runCatching {
                getSalesTransactionItemsUseCase(transaction.id)
            }.onSuccess { items ->
                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        selectedTransactionItems = items,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        detailErrorMessage = throwable.message ?: "Gagal memuat detail transaksi",
                    )
                }
            }
        }
    }

    fun dismissTransactionDetail() {
        _uiState.update {
            it.copy(
                selectedTransaction = null,
                selectedTransactionItems = emptyList(),
                isDetailLoading = false,
                detailErrorMessage = null,
            )
        }
    }

    private fun TransactionHistoryRange.toMillisRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -(daySpan - 1))
        }

        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return start.timeInMillis to end.timeInMillis
    }
}
