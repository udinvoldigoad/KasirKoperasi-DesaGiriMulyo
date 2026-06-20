package com.kasirkoperasi.app.core.printer

import android.content.Context

data class BluetoothPrinterDevice(
    val name: String,
    val address: String,
)

data class PrinterConnection(
    val name: String,
    val address: String?,
)

object PrinterConnectionStore {
    const val DEFAULT_PRINTER_NAME = "IDY01POS-58B"

    fun load(context: Context): PrinterConnection? {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val name = preferences.getString(KEY_PRINTER_NAME, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
        val address = preferences.getString(KEY_PRINTER_ADDRESS, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        return if (name == null && address == null) {
            null
        } else {
            PrinterConnection(
                name = name ?: DEFAULT_PRINTER_NAME,
                address = address,
            )
        }
    }

    fun loadOrDefault(context: Context): PrinterConnection {
        return load(context) ?: PrinterConnection(
            name = DEFAULT_PRINTER_NAME,
            address = null,
        )
    }

    fun save(context: Context, device: BluetoothPrinterDevice): PrinterConnection {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PRINTER_NAME, device.name)
            .putString(KEY_PRINTER_ADDRESS, device.address)
            .apply()

        return PrinterConnection(
            name = device.name,
            address = device.address,
        )
    }

    private const val PREFERENCES_NAME = "printer_connection"
    private const val KEY_PRINTER_NAME = "printer_name"
    private const val KEY_PRINTER_ADDRESS = "printer_address"
}
