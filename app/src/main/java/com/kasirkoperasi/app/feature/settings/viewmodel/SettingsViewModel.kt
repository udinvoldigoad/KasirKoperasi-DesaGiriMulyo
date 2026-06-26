package com.kasirkoperasi.app.feature.settings.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasirkoperasi.app.core.backup.OfflineBackupExporter
import com.kasirkoperasi.app.core.backup.OfflineBackupRestorer
import com.kasirkoperasi.app.core.common.AppResult
import com.kasirkoperasi.app.core.image.ProductImageStore
import com.kasirkoperasi.app.core.printer.BluetoothEscPosPrinter
import com.kasirkoperasi.app.core.printer.BluetoothPrinterDevice
import com.kasirkoperasi.app.core.printer.BluetoothPrinterDiscovery
import com.kasirkoperasi.app.core.printer.PrinterConnection
import com.kasirkoperasi.app.core.printer.PrinterConnectionStore
import com.kasirkoperasi.app.core.settings.StoreProfile
import com.kasirkoperasi.app.core.settings.StoreProfileStore
import com.kasirkoperasi.app.domain.usecase.ImportProductsCsvUseCase
import com.kasirkoperasi.app.domain.usecase.ProductCsvImportResult
import com.kasirkoperasi.app.feature.settings.state.SettingsUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(
    context: Context,
    private val importProductsCsvUseCase: ImportProductsCsvUseCase,
) : ViewModel() {
    private val appContext = context.applicationContext
    private val offlineBackupExporter = OfflineBackupExporter(appContext)
    private val offlineBackupRestorer = OfflineBackupRestorer(appContext)
    private val _uiState = MutableStateFlow(StoreProfileStore.load(appContext).toUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun saveStoreName(storeName: String) {
        val normalizedName = storeName.trim()
        if (normalizedName.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage = "Nama toko tidak boleh kosong",
                    successMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    isImporting = false,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    StoreProfileStore.saveStoreName(appContext, normalizedName)
                }
            }.onSuccess { profile ->
                _uiState.update {
                    profile.toUiState(
                        successMessage = "Nama toko berhasil disimpan",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isImporting = false,
                        errorMessage = throwable.message ?: "Gagal menyimpan nama toko",
                    )
                }
            }
        }
    }

    fun saveLogo(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    isImporting = false,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    StoreProfileStore.saveLogo(appContext, sourceUri)
                }
            }.onSuccess { profile ->
                _uiState.update {
                    profile.toUiState(
                        successMessage = "Logo koperasi berhasil diperbarui",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isImporting = false,
                        errorMessage = throwable.message ?: "Gagal menyimpan logo koperasi",
                    )
                }
            }
        }
    }

    fun importProductsCsv(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isImporting = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                val csvText = withContext(Dispatchers.IO) {
                    appContext.contentResolver.openInputStream(sourceUri)?.bufferedReader().use { reader ->
                        requireNotNull(reader) { "File CSV tidak bisa dibuka" }
                        reader.readText()
                    }
                }

                importProductsCsvUseCase(csvText)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        successMessage = if (result.importedCount > 0) result.toMessage() else null,
                        errorMessage = if (result.importedCount == 0) result.toMessage() else null,
                        importCompletedSignal = if (result.importedCount > 0) {
                            it.importCompletedSignal + 1
                        } else {
                            it.importCompletedSignal
                        },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = throwable.message ?: "Gagal import CSV",
                    )
                }
            }
        }
    }

    fun cleanupProductImages(activeImageUris: Set<String>) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCleaningImages = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    ProductImageStore.cleanupUnusedImages(
                        context = appContext,
                        activeImageUris = activeImageUris,
                    )
                }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isCleaningImages = false,
                        successMessage = if (result.deletedCount > 0) {
                            "Pembersihan selesai: ${result.deletedCount} file dihapus, ${result.freedBytes.toReadableSize()} dikosongkan"
                        } else {
                            "Tidak ada foto produk lama yang perlu dibersihkan"
                        },
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isCleaningImages = false,
                        errorMessage = throwable.message ?: "Gagal membersihkan foto produk",
                    )
                }
            }
        }
    }

    fun backupData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBackingUp = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    offlineBackupExporter.export()
                }
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(
                        isBackingUp = false,
                        successMessage = "Backup tersimpan: ${result.locationText}",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isBackingUp = false,
                        errorMessage = throwable.message ?: "Gagal membuat backup data",
                    )
                }
            }
        }
    }

    fun restoreData(sourceUri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRestoring = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    offlineBackupRestorer.restore(sourceUri)
                }
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        successMessage = "Restore data berhasil. Aplikasi akan dimuat ulang.",
                        restoreCompletedSignal = it.restoreCompletedSignal + 1,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isRestoring = false,
                        errorMessage = throwable.message ?: "Gagal restore data",
                    )
                }
            }
        }
    }

    fun loadPairedPrinters() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingPrinters = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            when (val result = withContext(Dispatchers.IO) { BluetoothPrinterDiscovery.getPairedDevices(appContext) }) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingPrinters = false,
                            pairedPrinters = result.data,
                            errorMessage = if (result.data.isEmpty()) {
                                "Belum ada perangkat Bluetooth yang dipairing"
                            } else {
                                null
                            },
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingPrinters = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun selectPrinter(device: BluetoothPrinterDevice) {
        val connection = PrinterConnectionStore.save(appContext, device)
        _uiState.update {
            it.copy(
                selectedPrinterName = connection.name,
                selectedPrinterAddress = connection.address,
                successMessage = "Printer ${connection.name} dipilih",
                errorMessage = null,
            )
        }
    }

    fun testPrintSelectedPrinter() {
        val connection = _uiState.value.selectedPrinterConnectionOrNull()
        if (connection == null) {
            _uiState.update {
                it.copy(
                    errorMessage = "Pilih printer terlebih dahulu",
                    successMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTestingPrinter = true,
                    errorMessage = null,
                    successMessage = null,
                )
            }

            val printer = BluetoothEscPosPrinter(
                context = appContext,
                printerConnection = connection,
            )

            when (val result = printer.printTest(_uiState.value.storeName)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isTestingPrinter = false,
                            successMessage = "Test print berhasil",
                        )
                    }
                }

                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isTestingPrinter = false,
                            errorMessage = result.message,
                        )
                    }
                }
            }
        }
    }

    fun onPrinterPermissionDenied() {
        _uiState.update {
            it.copy(
                errorMessage = "Izin Bluetooth diperlukan untuk koneksi printer",
                successMessage = null,
            )
        }
    }

    fun clearMessage() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    private fun StoreProfile.toUiState(
        successMessage: String? = null,
        errorMessage: String? = null,
    ): SettingsUiState {
        val printerConnection = PrinterConnectionStore.load(appContext)

        return SettingsUiState(
            storeName = storeName,
            logoUri = logoUri,
            isSaving = false,
            isImporting = false,
            isCleaningImages = false,
            isBackingUp = false,
            isRestoring = false,
            selectedPrinterName = printerConnection?.name,
            selectedPrinterAddress = printerConnection?.address,
            successMessage = successMessage,
            errorMessage = errorMessage,
        )
    }

    private fun SettingsUiState.selectedPrinterConnectionOrNull(): PrinterConnection? {
        val printerName = selectedPrinterName?.takeIf { it.isNotBlank() } ?: return null
        return PrinterConnection(
            name = printerName,
            address = selectedPrinterAddress,
        )
    }

    private fun ProductCsvImportResult.toMessage(): String {
        val actionParts = buildList {
            if (addedCount > 0) add("$addedCount ditambahkan")
            if (updatedCount > 0) add("$updatedCount diperbarui")
        }.joinToString(", ")
            .ifBlank { "0 diproses" }
        val baseMessage = "Import selesai: $importedCount dari $totalRows produk diproses ($actionParts)"
        if (skippedCount == 0) return baseMessage

        val firstIssues = issues
            .take(3)
            .joinToString("; ") { "baris ${it.rowNumber}: ${it.reason}" }
        return "$baseMessage, $skippedCount dilewati. $firstIssues"
    }

    private fun Long.toReadableSize(): String {
        return when {
            this >= 1_048_576L -> "${this / 1_048_576L} MB"
            this >= 1_024L -> "${this / 1_024L} KB"
            else -> "$this byte"
        }
    }
}
