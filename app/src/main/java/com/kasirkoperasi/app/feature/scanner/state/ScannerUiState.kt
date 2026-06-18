package com.kasirkoperasi.app.feature.scanner.state

data class ScannerUiState(
    val isScanning: Boolean = false,
    val scannedBarcode: String? = null,
    val errorMessage: String? = null,
)
