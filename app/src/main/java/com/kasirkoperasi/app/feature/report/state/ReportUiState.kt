package com.kasirkoperasi.app.feature.report.state

import android.net.Uri
import com.kasirkoperasi.app.domain.model.ReportSummary

data class ReportUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val summary: ReportSummary = ReportSummary(),
    val exportedPdfUri: Uri? = null,
    val errorMessage: String? = null,
    val exportErrorMessage: String? = null,
)

enum class ReportExportRange(
    val title: String,
    val description: String,
) {
    Today(
        title = "Hari Ini",
        description = "Export pencatatan transaksi hari ini.",
    ),
    SevenDays(
        title = "7 Hari",
        description = "Export pencatatan 7 hari terakhir.",
    ),
    CurrentMonth(
        title = "1 Bulan",
        description = "Export pencatatan bulan berjalan.",
    ),
}
