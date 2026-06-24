# KasirKoperasi Desa Girimulyo

KasirKoperasi adalah aplikasi kasir kebutuhan pertanian berbasis Android untuk membantu koperasi desa mencatat penjualan, mengelola barang, stok, hutang, laporan, barcode, foto produk, backup data, dan pencetakan struk thermal.

Proyek ini dibuat sebagai bagian dari kegiatan **KKN Rekognisi di Desa Girimulyo** dengan fokus penerapan teknologi sederhana, offline, dan praktis untuk mendukung operasional **koperasi desa**.

## Tujuan

Aplikasi ini dirancang agar kasir koperasi dapat bekerja lebih cepat dan rapi tanpa bergantung pada koneksi internet.

Fokus utama aplikasi:

- Mencatat transaksi penjualan harian.
- Mengelola data produk kebutuhan pertanian.
- Mengelola stok barang dan stok menipis.
- Mendukung transaksi Cash, QRIS, dan Hutang.
- Mencatat pelunasan hutang pembeli.
- Menyediakan laporan dan export PDF untuk pembukuan.
- Mendukung barcode scanner untuk mempercepat input barang.
- Mendukung printer thermal Bluetooth untuk mencetak struk.
- Menyimpan data secara offline di perangkat.
- Menyediakan backup dan restore data lokal.

## Fitur Utama

### Beranda

- Ringkasan penjualan hari ini.
- Ringkasan profit hari ini.
- Indikator stok menipis yang bisa dibuka menjadi daftar barang.
- Menu cepat 3 kolom untuk Produk, Scan Barcode, Transaksi, Riwayat, Laporan, dan Setting.
- Tombol informasi di kanan atas berisi panduan penggunaan aplikasi.
- Logo dan nama toko mengikuti pengaturan aplikasi.

### Data Barang dan Stok

- Tambah produk baru.
- Edit foto produk dari kamera atau galeri.
- Edit nama produk, kategori, satuan, harga beli, harga jual, stok, dan barcode.
- Stok masuk melalui panel edit barang.
- Format harga otomatis menggunakan titik ribuan.
- Tampilkan foto produk di halaman barang, transaksi, detail transaksi, dan panel stok menipis.
- Filter kategori produk.
- Urutkan stok dari terendah atau tertinggi.
- Produk stok kosong dibuat abu-abu di halaman transaksi dan tidak bisa masuk keranjang.

### Import Produk CSV

- Import data produk massal dari file CSV.
- Cocok untuk input banyak barang awal.
- Format kolom:

```csv
kode,nama,kategori,harga_beli,harga_jual,stok,satuan
0001,Pupuk Urea 5kg,Pupuk,55000,65000,20,sak
```

Catatan:

- Barcode angka 1 sampai 4 digit akan dinormalisasi menjadi 4 digit.
- Contoh: `1` menjadi `0001`.
- Produk dengan barcode duplikat akan dilewati.

### Transaksi

- Pilih barang secara manual.
- Scan barcode produk.
- Barang hasil scan langsung masuk keranjang jika barcode terdaftar.
- Keranjang muncul sebagai tombol mengambang setelah barang dipilih.
- Panel keranjang bisa digunakan untuk mengubah jumlah barang.
- Metode pembayaran Cash, QRIS, dan Hutang.
- Nama pembeli dapat diisi agar tampil di struk.
- Untuk metode Hutang, nama pembeli wajib diisi.
- Format uang otomatis menggunakan titik ribuan.
- Modal transaksi berhasil menampilkan daftar barang, qty, harga, dan total.
- Tombol print tersedia setelah transaksi berhasil.

### Hutang dan Pelunasan

- Pembeli bisa membayar sebagian atau belum membayar sama sekali.
- Sisa pembayaran dicatat sebagai hutang.
- Hutang tersimpan per pembeli.
- Pelunasan hutang dapat dicatat dari laporan.
- Laporan menampilkan total hutang, daftar transaksi hutang, pembayaran hutang, dan sisa hutang per pembeli.

### Barcode

- Barcode produk menggunakan kode 4 angka.
- Contoh:

```text
0001
0002
0003
```

- Barcode dapat ditempel di rak barang.
- Setiap variasi barang yang berbeda ukuran/jenis sebaiknya memiliki kode barcode sendiri.
- Scanner kamera membaca barcode dan mencocokkannya dengan data produk.
- Jika barcode ditemukan, produk langsung masuk keranjang.
- Jika barcode tidak ditemukan, aplikasi menampilkan pesan barang belum terdaftar.
- Tersedia fitur generate barcode massal untuk semua produk dalam format kertas A4.

### Laporan dan Riwayat

- Ringkasan penjualan.
- Profit.
- Item terjual.
- Stok menipis.
- Grafik penjualan 7 hari.
- Riwayat transaksi.
- Detail transaksi berisi daftar barang, qty, harga, subtotal, dan foto produk.
- Filter periode laporan.
- Export laporan PDF untuk:
  - Hari ini
  - 7 hari
  - Bulan berjalan

Isi PDF laporan:

- Rekapan transaksi.
- Daftar barang terjual.
- Total Cash.
- Total QRIS.
- Total Hutang.
- Daftar transaksi hutang.
- Daftar pembayaran hutang.
- Sisa hutang per pembeli.
- Laporan stok semua barang.

### Printer Thermal

- Koneksi printer thermal Bluetooth dari halaman Pengaturan.
- Menampilkan daftar perangkat Bluetooth yang sudah dipairing.
- Pilih printer dan simpan pilihan printer.
- Test print.
- Print struk setelah transaksi berhasil.
- Printer target saat pengembangan:

```text
IDY01POS-58B
```

Catatan: beberapa printer thermal Bluetooth hanya benar-benar tersambung saat aplikasi mengirim data print. Lampu indikator bisa tetap berkedip ketika standby.

### Backup dan Restore

- Backup data dari halaman Pengaturan.
- File backup otomatis tersimpan ke folder:

```text
Download/KasirKoperasi
```

- Backup berisi database, foto produk, logo toko, dan pengaturan lokal.
- Restore dapat mengambil file backup ZIP dan mengganti data aplikasi dengan data dari backup.
- Setelah restore berhasil, aplikasi akan restart agar data baru terbaca dengan benar.

## Pengaturan Aplikasi

Halaman Pengaturan berisi:

- Edit logo koperasi.
- Edit nama toko/koperasi.
- Koneksi printer Bluetooth.
- Test print.
- Import CSV produk.
- Generate barcode massal.
- Backup data.
- Restore data.

Nama toko dan logo digunakan di navbar aplikasi dan untuk kebutuhan struk/laporan.

## Penyimpanan Data

Aplikasi menggunakan pendekatan **offline-first**.

Data utama disimpan di perangkat Android menggunakan:

- Room Database.
- SQLite lokal.
- Internal storage untuk foto produk dan logo.
- Shared preferences untuk pengaturan sederhana.

Artinya aplikasi tetap dapat digunakan tanpa internet. Data produk, stok, transaksi, hutang, laporan, foto produk, logo, dan pengaturan tersimpan di perangkat yang digunakan.

Jika aplikasi hanya digunakan di satu HP, skema offline ini sudah cukup. Risiko utamanya adalah data ikut hilang jika HP rusak, hilang, atau aplikasi dihapus. Karena itu backup rutin sangat penting.

## Teknologi

- Kotlin
- Android
- Jetpack Compose
- Material 3
- MVVM
- Repository Pattern
- Room Database
- SQLite
- Google Code Scanner
- Bluetooth ESC/POS thermal printer
- PDF export lokal
- Offline backup/restore ZIP

## Struktur Project

Struktur utama kode aplikasi:

```text
app/src/main/java/com/kasirkoperasi/app
```

Folder penting:

```text
core/
```

Berisi komponen umum seperti navigasi, UI reusable, penyimpanan gambar, barcode, PDF, printer, backup, restore, dan pengaturan lokal.

```text
data/
```

Berisi Room Database, DAO, entity, mapper, dan implementasi repository.

```text
domain/
```

Berisi model, repository contract, dan use case.

```text
feature/
```

Berisi fitur aplikasi berdasarkan halaman:

- home
- product
- transaction
- report
- history
- settings
- stock
- printer
- scanner

```text
di/
```

Berisi `AppContainer` untuk menyediakan dependency aplikasi.

## Cara Menjalankan Debug di Windows

Pastikan Android Studio, JDK, dan Android SDK sudah tersedia.

Build debug:

```powershell
.\gradlew.bat assembleDebug --console=plain
```

Install ke HP via USB debugging:

```powershell
.\gradlew.bat installDebug --console=plain
```

## Cara Build Release

Project sudah mendukung konfigurasi signing release melalui file lokal `keystore.properties`.

File yang tidak boleh di-commit:

- `keystore.properties`
- file `.jks`
- file `.keystore`

Template tersedia di:

```text
keystore.properties.example
```

Langkah umum:

1. Buat keystore release lokal.
2. Salin `keystore.properties.example` menjadi `keystore.properties`.
3. Isi password dan path keystore.
4. Jalankan build release.

```powershell
.\gradlew.bat assembleRelease --console=plain
```

Jika `keystore.properties` belum ada, Gradle hanya menghasilkan APK release unsigned. APK unsigned belum layak dibagikan sebagai rilis final.

## Cara Menggunakan Printer

1. Nyalakan printer thermal.
2. Pair printer dari pengaturan Bluetooth HP.
3. Buka aplikasi.
4. Masuk ke halaman Pengaturan.
5. Pilih menu koneksi printer.
6. Muat daftar perangkat Bluetooth.
7. Pilih printer.
8. Jalankan test print.
9. Setelah transaksi berhasil, tekan tombol Print.

## Checklist Uji Operasional

Sebelum aplikasi dipakai untuk operasional toko, tes alur berikut:

- Tambah produk dengan foto.
- Edit harga, stok, dan barcode.
- Import produk CSV.
- Scan barcode produk terdaftar.
- Scan barcode yang belum terdaftar.
- Transaksi Cash.
- Transaksi QRIS.
- Transaksi Hutang.
- Pelunasan hutang.
- Print struk.
- Export PDF laporan.
- Backup data.
- Restore data.
- Tutup dan buka ulang aplikasi.
- Restart HP dan cek data masih ada.

## Status Pengembangan

Fitur yang sudah tersedia:

- Dashboard beranda.
- Panduan penggunaan aplikasi.
- Kelola produk dan stok.
- Foto produk.
- Import CSV produk.
- Barcode scanner.
- Generate barcode massal.
- Keranjang dan transaksi.
- Pembayaran Cash, QRIS, dan Hutang.
- Pelunasan hutang.
- Riwayat transaksi.
- Detail transaksi dengan foto produk.
- Laporan dan export PDF.
- Grafik penjualan 7 hari.
- Pengaturan nama toko dan logo.
- Koneksi printer Bluetooth.
- Print struk transaksi.
- Backup dan restore data.
- Konfigurasi build release signed.

Fitur yang masih dapat dikembangkan:

- Sinkronisasi database online jika koperasi memakai lebih dari satu perangkat.
- Manajemen user/kasir.
- Role admin dan kasir.
- Audit log perubahan data.
- Stok opname lebih detail.
- Notifikasi jatuh tempo hutang.
- Dashboard analitik lebih lengkap.

## Konteks Proyek

Proyek ini dikembangkan untuk mendukung digitalisasi sederhana pada koperasi desa dalam kegiatan **KKN Rekognisi Desa Girimulyo**.

Aplikasi ini diharapkan dapat membantu koperasi desa mencatat penjualan kebutuhan pertanian secara lebih tertata, mengurangi pencatatan manual, mempermudah pembukuan, dan tetap dapat digunakan walaupun tidak ada koneksi internet.
