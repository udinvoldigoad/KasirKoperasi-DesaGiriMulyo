package com.kasirkoperasi.app.feature.settings.state

import com.kasirkoperasi.app.core.settings.DEFAULT_STORE_NAME
import com.kasirkoperasi.app.core.printer.BluetoothPrinterDevice

data class SettingsUiState(
    val storeName: String = DEFAULT_STORE_NAME,
    val logoUri: String? = null,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val isLoadingPrinters: Boolean = false,
    val isTestingPrinter: Boolean = false,
    val pairedPrinters: List<BluetoothPrinterDevice> = emptyList(),
    val selectedPrinterName: String? = null,
    val selectedPrinterAddress: String? = null,
    val importCompletedSignal: Int = 0,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
