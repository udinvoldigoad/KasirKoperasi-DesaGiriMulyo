package com.kasirkoperasi.app.core.printer

import com.kasirkoperasi.app.core.common.AppResult

interface ThermalPrinterClient {
    suspend fun connect(deviceAddress: String): AppResult<Unit>

    suspend fun printReceipt(lines: List<String>): AppResult<Unit>

    suspend fun disconnect()
}
