package com.kasirkoperasi.app.feature.printer.state

data class PrinterUiState(
    val isConnected: Boolean = false,
    val deviceName: String? = null,
    val errorMessage: String? = null,
)
