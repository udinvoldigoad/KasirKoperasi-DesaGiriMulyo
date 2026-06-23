package com.kasirkoperasi.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.model.DebtPayment
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.domain.usecase.ExportTransactionReportPdfUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtCustomerDetailUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtCustomersUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase
import com.kasirkoperasi.app.domain.usecase.RecordDebtPaymentUseCase
import com.kasirkoperasi.app.feature.report.state.DebtPaymentMethod
import com.kasirkoperasi.app.feature.report.state.ReportDailySalesPoint
import com.kasirkoperasi.app.feature.report.state.ReportExportRange
import com.kasirkoperasi.app.feature.report.state.ReportUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val getSimpleReportUseCase: GetSimpleReportUseCase,
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
    private val exportTransactionReportPdfUseCase: ExportTransactionReportPdfUseCase,
    private val getDebtCustomerDetailUseCase: GetDebtCustomerDetailUseCase,
    private val getDebtCustomersUseCase: GetDebtCustomersUseCase,
    private val recordDebtPaymentUseCase: RecordDebtPaymentUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadTodaySummary()
    }

    fun loadTodaySummary() {
        val (startDateMillis, endDateMillis) = todayRangeMillis()
        val (monthStartMillis, monthEndMillis) = ReportExportRange.CurrentMonth.toMillisRange()
        val sevenDayRanges = sevenDayRangesMillis()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    exportErrorMessage = null,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                val todaySummary = getSimpleReportUseCase(
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
                )
                val monthlySummary = getSimpleReportUseCase(
                    startDateMillis = monthStartMillis,
                    endDateMillis = monthEndMillis,
                )
                val sevenDaySales = sevenDayRanges.map { range ->
                    val summary = getSimpleReportUseCase(
                        startDateMillis = range.startDateMillis,
                        endDateMillis = range.endDateMillis,
                    )
                    ReportDailySalesPoint(
                        label = range.label,
                        totalSales = summary.totalSales,
                    )
                }
                val debtCustomers = getDebtCustomersUseCase()

                ReportDashboardData(
                    todaySummary = todaySummary,
                    monthlySummary = monthlySummary,
                    sevenDaySales = sevenDaySales,
                    debtCustomers = debtCustomers,
                )
            }.onSuccess { data ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = data.todaySummary,
                        monthlySummary = data.monthlySummary,
                        sevenDaySales = data.sevenDaySales,
                        debtCustomers = data.debtCustomers,
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
                    startDateMillis = startDateMillis,
                    endDateMillis = endDateMillis,
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

    fun recordDebtPayment(
        buyerName: String,
        buyerContact: String,
        amountText: String,
        method: DebtPaymentMethod,
    ) {
        val amount = amountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        if (buyerName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nama pembeli hutang tidak valid") }
            return
        }
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Nominal pelunasan harus diisi") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRecordingDebtPayment = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                recordDebtPaymentUseCase(
                    DebtPayment(
                        buyerName = buyerName,
                        buyerContact = buyerContact,
                        paymentMethod = method.label,
                        amount = amount,
                        note = "Pelunasan hutang",
                    ),
                )
                val (monthStartMillis, monthEndMillis) = ReportExportRange.CurrentMonth.toMillisRange()
                val selectedDetail = _uiState.value.selectedDebtCustomerDetail
                DebtPaymentResult(
                    monthlySummary = getSimpleReportUseCase(
                        startDateMillis = monthStartMillis,
                        endDateMillis = monthEndMillis,
                    ),
                    debtCustomers = getDebtCustomersUseCase(),
                    selectedDebtCustomerDetail = selectedDetail?.let {
                        getDebtCustomerDetailUseCase(
                            buyerName = it.summary.buyerName,
                            buyerContact = it.summary.buyerContact,
                        )
                    },
                )
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isRecordingDebtPayment = false,
                        monthlySummary = result.monthlySummary,
                        debtCustomers = result.debtCustomers,
                        selectedDebtCustomerDetail = result.selectedDebtCustomerDetail,
                        debtPaymentSuccessSignal = it.debtPaymentSuccessSignal + 1,
                        successMessage = "Pembayaran hutang berhasil dicatat",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRecordingDebtPayment = false,
                        errorMessage = throwable.message ?: "Gagal mencatat pembayaran hutang",
                    )
                }
            }
        }
    }

    fun openDebtCustomerDetail(customer: com.kasirkoperasi.app.domain.model.DebtCustomerSummary) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDebtDetailLoading = true,
                    debtDetailErrorMessage = null,
                )
            }

            runCatching {
                getDebtCustomerDetailUseCase(
                    buyerName = customer.buyerName,
                    buyerContact = customer.buyerContact,
                )
            }.onSuccess { detail ->
                _uiState.update {
                    it.copy(
                        isDebtDetailLoading = false,
                        selectedDebtCustomerDetail = detail,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isDebtDetailLoading = false,
                        debtDetailErrorMessage = throwable.message ?: "Gagal memuat detail hutang",
                    )
                }
            }
        }
    }

    fun dismissDebtCustomerDetail() {
        _uiState.update {
            it.copy(
                selectedDebtCustomerDetail = null,
                isDebtDetailLoading = false,
                debtDetailErrorMessage = null,
            )
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

    private fun sevenDayRangesMillis(): List<DayRangeMillis> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, -6)
        }

        return (0 until 7).map { dayOffset ->
            val dayStart = (start.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val dayEnd = (dayStart.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            DayRangeMillis(
                label = "${dayStart.get(Calendar.DAY_OF_MONTH)}/${dayStart.get(Calendar.MONTH) + 1}",
                startDateMillis = dayStart.timeInMillis,
                endDateMillis = dayEnd.timeInMillis,
            )
        }
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
                val calendar = Calendar.getInstance()
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("id-ID"))
                    .format(Date(calendar.timeInMillis))
                "1-$daysInMonth $monthLabel ($daysInMonth hari)"
            }
        }
    }

    private companion object {
        const val EXPORT_TRANSACTION_LIMIT = 1_000
    }
}

private data class ReportDashboardData(
    val todaySummary: ReportSummary,
    val monthlySummary: ReportSummary,
    val sevenDaySales: List<ReportDailySalesPoint>,
    val debtCustomers: List<com.kasirkoperasi.app.domain.model.DebtCustomerSummary>,
)

private data class DebtPaymentResult(
    val monthlySummary: ReportSummary,
    val debtCustomers: List<com.kasirkoperasi.app.domain.model.DebtCustomerSummary>,
    val selectedDebtCustomerDetail: com.kasirkoperasi.app.domain.model.DebtCustomerDetail?,
)

private data class DayRangeMillis(
    val label: String,
    val startDateMillis: Long,
    val endDateMillis: Long,
)
