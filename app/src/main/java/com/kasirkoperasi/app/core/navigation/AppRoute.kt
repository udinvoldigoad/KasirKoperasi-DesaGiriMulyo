package com.kasirkoperasi.app.core.navigation

sealed class AppRoute(
    val route: String,
    val title: String,
) {
    data object Home : AppRoute("home", "Beranda")
    data object Transaction : AppRoute("transaction", "Transaksi")
    data object Product : AppRoute("product", "Barang")
    data object Stock : AppRoute("stock", "Stok")
    data object Report : AppRoute("report", "Laporan")
    data object History : AppRoute("history", "Riwayat")
    data object Printer : AppRoute("printer", "Printer")
    data object Scanner : AppRoute("scanner", "Scanner")
}
