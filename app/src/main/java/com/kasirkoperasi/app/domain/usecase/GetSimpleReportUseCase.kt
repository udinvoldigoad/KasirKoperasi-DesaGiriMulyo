package com.kasirkoperasi.app.domain.usecase

import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.domain.repository.ReportRepository

class GetSimpleReportUseCase(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(startDateMillis: Long, endDateMillis: Long): ReportSummary {
        return reportRepository.getSummary(startDateMillis, endDateMillis)
    }
}
