package com.kasirkoperasi.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.usecase.ExportTransactionReportPdfUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase
import com.kasirkoperasi.app.feature.report.state.ReportExportRange
import com.kasirkoperasi.app.feature.report.state.ReportUiState
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val getSimpleReportUseCase: GetSimpleReportUseCase,
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
    private val exportTransactionReportPdfUseCase: ExportTransactionReportPdfUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadTodaySummary()
    }

    fun loadTodaySummary() {
        val (startDateMillis, endDateMillis) = todayRangeMillis()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    exportErrorMessage = null,
                    errorMessage = null,
                )
            }

            runCatching {
                getSimpleReportUseCase(
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                )
            }.onSuccess { summary ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Gagal memuat ringkasan",
                    )
                }
            }
        }
    }

    fun exportReportPdf(range: ReportExportRange) {
        val (startDateMillis, endDateMillis) = range.toMillisRange()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportedPdfUri = null,
                    exportErrorMessage = null,
                )
            }

            runCatching {
                val transactions = getSalesTransactionsUseCase(
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                    limit = EXPORT_TRANSACTION_LIMIT,
                )
                exportTransactionReportPdfUseCase(
                    periodLabel = range.toPeriodLabel(),
                    transactions = transactions,
                )
            }.onSuccess { uri ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportedPdfUri = uri,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportErrorMessage = throwable.message ?: "Gagal membuat PDF",
                    )
                }
            }
        }
    }

    fun clearExportResult() {
        _uiState.update {
            it.copy(
                exportedPdfUri = null,
                exportErrorMessage = null,
            )
        }
    }

    private fun todayRangeMillis(): Pair<Long, Long> {
        return ReportExportRange.Today.toMillisRange()
    }

    private fun ReportExportRange.toMillisRange(): Pair<Long, Long> {
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

        when (this) {
            ReportExportRange.Today -> Unit
            ReportExportRange.SevenDays -> start.add(Calendar.DAY_OF_YEAR, -6)
            ReportExportRange.CurrentMonth -> {
                start.set(Calendar.DAY_OF_MONTH, 1)
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
            }
        }

        return start.timeInMillis to end.timeInMillis
    }

    private fun ReportExportRange.toPeriodLabel(): String {
        return when (this) {
            ReportExportRange.Today -> "Hari Ini"
            ReportExportRange.SevenDays -> "7 Hari Terakhir"
            ReportExportRange.CurrentMonth -> {
                val daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)
                "1 Bulan ($daysInMonth hari)"
            }
        }
    }

    private companion object {
        const val EXPORT_TRANSACTION_LIMIT = 1_000
    }
}
