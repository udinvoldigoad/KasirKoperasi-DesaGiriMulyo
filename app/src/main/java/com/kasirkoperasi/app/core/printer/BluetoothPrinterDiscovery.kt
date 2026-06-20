package com.kasirkoperasi.app.core.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.kasirkoperasi.app.core.common.AppResult

object BluetoothPrinterDiscovery {
    fun hasBluetoothConnectPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT,
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getPairedDevices(context: Context): AppResult<List<BluetoothPrinterDevice>> {
        if (!hasBluetoothConnectPermission(context)) {
            return AppResult.Error("Izin Bluetooth belum diberikan")
        }

        return runCatching {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter ?: error("Bluetooth tidak tersedia di perangkat ini")

            require(adapter.isEnabled) { "Bluetooth belum aktif" }

            adapter.bondedDevices
                .map { device ->
                    BluetoothPrinterDevice(
                        name = device.name?.takeIf { it.isNotBlank() } ?: "Perangkat tanpa nama",
                        address = device.address,
                    )
                }
                .sortedWith(
                    compareByDescending<BluetoothPrinterDevice> {
                        it.name.contains("POS", ignoreCase = true) ||
                            it.name.contains("PRINTER", ignoreCase = true)
                    }.thenBy { it.name.lowercase() },
                )
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.message ?: "Gagal membaca perangkat Bluetooth", it) },
        )
    }
}
