package com.kasirkoperasi.app.feature.report.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kasirkoperasi.app.domain.usecase.ExportTransactionReportPdfUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtCustomerDetailUseCase
import com.kasirkoperasi.app.domain.usecase.GetDebtCustomersUseCase
import com.kasirkoperasi.app.domain.usecase.GetSalesTransactionsUseCase
import com.kasirkoperasi.app.domain.usecase.GetSimpleReportUseCase
import com.kasirkoperasi.app.domain.usecase.RecordDebtPaymentUseCase

class ReportViewModelFactory(
    private val getSimpleReportUseCase: GetSimpleReportUseCase,
    private val getSalesTransactionsUseCase: GetSalesTransactionsUseCase,
    private val exportTransactionReportPdfUseCase: ExportTransactionReportPdfUseCase,
    private val getDebtCustomerDetailUseCase: GetDebtCustomerDetailUseCase,
    private val getDebtCustomersUseCase: GetDebtCustomersUseCase,
    private val recordDebtPaymentUseCase: RecordDebtPaymentUseCase,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            return ReportViewModel(
                getSimpleReportUseCase = getSimpleReportUseCase,
                getSalesTransactionsUseCase = getSalesTransactionsUseCase,
                exportTransactionReportPdfUseCase = exportTransactionReportPdfUseCase,
                getDebtCustomerDetailUseCase = getDebtCustomerDetailUseCase,
                getDebtCustomersUseCase = getDebtCustomersUseCase,
                recordDebtPaymentUseCase = recordDebtPaymentUseCase,
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
