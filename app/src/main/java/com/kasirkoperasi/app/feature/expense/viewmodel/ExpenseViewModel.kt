package com.kasirkoperasi.app.feature.expense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.Expense
import com.kasirkoperasi.app.domain.usecase.GetExpensesUseCase
import com.kasirkoperasi.app.domain.usecase.SaveExpenseUseCase
import com.kasirkoperasi.app.feature.expense.state.ExpenseUiState
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val saveExpenseUseCase: SaveExpenseUseCase,
    private val getExpensesUseCase: GetExpensesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadTodayExpenses()
    }

    fun updateTitle(value: String) {
        _uiState.update {
            it.copy(
                title = value,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updateAmount(value: String) {
        _uiState.update {
            it.copy(
                amountText = value.filter { char -> char.isDigit() }.toLongOrNull()?.toPaymentText().orEmpty(),
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun updateNote(value: String) {
        _uiState.update {
            it.copy(
                note = value,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    fun saveExpense() {
        val currentState = _uiState.value
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama pengeluaran wajib diisi") }
            return
        }
        if (currentState.amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Nominal pengeluaran wajib diisi") }
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

            runCatching {
                saveExpenseUseCase(
                    Expense(
                        title = currentState.title.trim(),
                        amount = currentState.amount,
                        note = currentState.note.trim(),
                    ),
                )
                getTodayExpenses()
            }.onSuccess { expenses ->
                _uiState.update {
                    it.copy(
                        title = "",
                        amountText = "",
                        note = "",
                        expenses = expenses,
                        isSaving = false,
                        successMessage = "Pengeluaran berhasil dicatat",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Gagal menyimpan pengeluaran",
                    )
                }
            }
        }
    }

    fun loadTodayExpenses() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            runCatching {
                getTodayExpenses()
            }.onSuccess { expenses ->
                _uiState.update {
                    it.copy(
                        expenses = expenses,
                        isLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Gagal memuat pengeluaran",
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

    private suspend fun getTodayExpenses(): List<Expense> {
        val (startDateMillis, endDateMillis) = todayRangeMillis()
        return getExpensesUseCase(
            startDateMillis = startDateMillis,
            endDateMillis = endDateMillis,
        )
    }

    private fun todayRangeMillis(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return start.timeInMillis to end.timeInMillis
    }

    private fun Long.toPaymentText(): String {
        if (this <= 0L) return ""

        return toString()
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    }
}
