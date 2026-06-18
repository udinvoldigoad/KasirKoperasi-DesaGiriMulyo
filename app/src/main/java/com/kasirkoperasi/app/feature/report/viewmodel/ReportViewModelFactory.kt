package com.kasirkoperasi.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase

class ReportViewModelFactory(
    private val getSimpleReportUseCase: GetSimpleReportUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(
                getSimpleReportUseCase = getSimpleReportUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
