package com.kasirkoperasi.app.core.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import com.kasirkoperasi.app.core.common.AppResult
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BluetoothEscPosPrinter(
    private val context: Context,
    private val printerConnection: PrinterConnection? = null,
) {
    @SuppressLint("MissingPermission")
    suspend fun printReceipt(data: ReceiptPrintData): AppResult<Unit> = withContext(Dispatchers.IO) {
        printRaw(ReceiptEscPosFormatter.format(data))
    }

    @SuppressLint("MissingPermission")
    suspend fun printTest(storeName: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        printRaw(ReceiptEscPosFormatter.formatTestPrint(storeName))
    }

    @SuppressLint("MissingPermission")
    private fun printRaw(bytes: ByteArray): AppResult<Unit> {
        if (!BluetoothPrinterDiscovery.hasBluetoothConnectPermission(context)) {
            return AppResult.Error("Izin Bluetooth belum diberikan")
        }

        return runCatching {
            val activeConnection = printerConnection ?: PrinterConnectionStore.loadOrDefault(context)
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: error("Bluetooth tidak tersedia di perangkat ini")

            require(adapter.isEnabled) { "Bluetooth belum aktif" }

            val device = findPairedDevice(
                devices = adapter.bondedDevices,
                connection = activeConnection,
            ) ?: error("Printer ${activeConnection.name} belum dipairing di Bluetooth HP")

            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            try {
                socket.connect()
                val output = socket.outputStream
                output.write(bytes)
                output.flush()
                Thread.sleep(PRINTER_FLUSH_DELAY_MS)
            } finally {
                runCatching { socket.close() }
            }
        }.fold(
            onSuccess = { AppResult.Success(Unit) },
            onFailure = { throwable ->
                AppResult.Error(
                    message = throwable.message ?: "Gagal mencetak struk",
                    cause = throwable,
                )
            },
        )
    }

    @SuppressLint("MissingPermission")
    private fun findPairedDevice(
        devices: Set<android.bluetooth.BluetoothDevice>,
        connection: PrinterConnection,
    ): android.bluetooth.BluetoothDevice? {
        connection.address?.let { address ->
            devices.firstOrNull { device ->
                device.address.equals(address, ignoreCase = true)
            }?.let { return it }
        }

        return devices.firstOrNull { device ->
            device.name.equals(connection.name, ignoreCase = true)
        } ?: devices.firstOrNull { device ->
            device.name?.contains(connection.name, ignoreCase = true) == true
        }
    }

    private companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        const val PRINTER_FLUSH_DELAY_MS = 800L
    }
}
