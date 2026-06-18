package com.kasirkoperasi.app.core.scanner

data class BarcodeScanResult(
    val value: String,
    val format: String? = null,
)

interface BarcodeScannerClient {
    fun parse(rawValue: String, format: String? = null): BarcodeScanResult?
}
