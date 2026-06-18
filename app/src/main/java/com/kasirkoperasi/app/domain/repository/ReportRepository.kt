package com.kasirkoperasi.app.domain.repository

import com.kasirkoperasi.app.domain.model.ReportSummary

interface ReportRepository {
    suspend fun getSummary(startDateMillis: Long, endDateMillis: Long): ReportSummary
}
