# KasirKoperasi

Aplikasi kasir kebutuhan pertanian berbasis Android.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Repository Pattern
- Room Database dengan SQLite
- Offline-first
- CameraX dan ML Kit untuk barcode scanner
- Bluetooth ESC/POS untuk thermal printer

## Tujuan Aplikasi

Aplikasi digunakan untuk toko kebutuhan pertanian di desa.
Aplikasi harus tetap dapat digunakan tanpa internet.

Fitur inti:
1. Kelola barang, harga, dan stok.
2. Transaksi kasir sederhana.
3. Cetak invoice ke thermal printer Bluetooth.
4. Scan barcode internal pada rak.
5. Riwayat transaksi dan stok.
6. export laporan dalam bentul excel
7. Backup data lokal.

## Aturan Pengembangan

- Utamakan penggunaan offline.
- Semua transaksi harus tersimpan dahulu di Room Database.
- Jangan membuat fitur yang membutuhkan backend atau internet tanpa persetujuan.
- Jangan mengubah dependency Gradle tanpa menjelaskan alasan terlebih dahulu.
- Gunakan bahasa Indonesia pada teks antarmuka.
- Gunakan nama class, function, dan variable dalam bahasa Inggris.
- Gunakan MVVM.
- Pisahkan UI, ViewModel, Repository, Database, dan Model.
- Jangan menghapus kode lama tanpa persetujuan.
- Sebelum mengubah banyak file, tampilkan rencana perubahan terlebih dahulu.

## Perintah Build Windows

Build debug:
.\gradlew.bat assembleDebug

Menjalankan unit test:
.\gradlew.bat test

Memeriksa project:
.\gradlew.bat lint