package com.kasirkoperasi.app.feature.report.state

import com.kasirkoperasi.app.domain.model.ReportSummary

data class ReportUiState(
    val isLoading: Boolean = false,
    val summary: ReportSummary = ReportSummary(),
    val errorMessage: String? = null,
)
