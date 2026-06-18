package com.kasirkoperasi.app.data.repository

import com.kasirkoperasi.app.data.local.dao.ReportDao
import com.kasirkoperasi.app.data.mapper.toDomain
import com.kasirkoperasi.app.domain.model.ReportSummary
import com.kasirkoperasi.app.domain.repository.ReportRepository

class ReportRepositoryImpl(
    private val reportDao: ReportDao,
) : ReportRepository {
    override suspend fun getSummary(startDateMillis: Long, endDateMillis: Long): ReportSummary {
        return reportDao.getSummary(startDateMillis, endDateMillis).toDomain()
    }
}
