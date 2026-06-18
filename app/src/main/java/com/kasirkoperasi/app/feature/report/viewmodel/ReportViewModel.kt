package com.kasirkoperasi.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase
import com.kasirkoperasi.app.feature.report.state.ReportUiState
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportViewModel(
    private val getSimpleReportUseCase: GetSimpleReportUseCase,
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
}
