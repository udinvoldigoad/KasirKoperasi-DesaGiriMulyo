# KasirKoperasi Desa Girimulyo

KasirKoperasi adalah aplikasi kasir kebutuhan pertanian berbasis Android yang dikembangkan untuk membantu proses pencatatan penjualan, pengelolaan barang, stok, laporan, barcode, dan pencetakan struk pada koperasi desa.

Proyek ini dibuat sebagai bagian dari kegiatan **KKN Rekognisi di Desa Girimulyo** dengan fokus penerapan teknologi sederhana dan praktis untuk mendukung operasional **koperasi desa**.

## Tujuan

Aplikasi ini dirancang agar koperasi desa dapat melakukan pencatatan transaksi secara lebih rapi, cepat, dan tetap bisa berjalan tanpa koneksi internet.

Fokus utama aplikasi:

- Memudahkan kasir dalam mencatat transaksi penjualan.
- Membantu pengelolaan data produk kebutuhan pertanian.
- Menyediakan pencatatan stok barang.
- Menyediakan laporan transaksi sederhana.
- Mendukung barcode untuk mempercepat pemilihan barang.
- Mendukung printer thermal Bluetooth untuk mencetak struk.
- Tetap dapat digunakan secara offline.

## Fitur Utama

### Beranda

- Ringkasan penjualan hari ini.
- Ringkasan profit hari ini.
- Indikator stok menipis.
- Akses cepat ke transaksi, barang, laporan, barcode, riwayat, dan pengaturan.

### Data Barang

- Tambah produk baru.
- Edit data produk.
- Tambah stok melalui panel edit barang.
- Hapus/nonaktifkan produk.
- Simpan foto produk dari kamera atau galeri.
- Tampilkan foto produk pada halaman barang dan transaksi.
- Filter produk berdasarkan kategori.

### Import Produk CSV

- Import data produk massal dari file CSV.
- Format kolom:

```csv
kode,nama,kategori,harga_beli,harga_jual,stok,satuan
0001,Pupuk Urea 5kg,Pupuk,55000,65000,20,sak
```

- Barcode angka 1 sampai 4 digit akan dinormalisasi menjadi 4 digit.
- Contoh: `1` menjadi `0001`.
- Produk dengan barcode duplikat akan dilewati.

### Transaksi

- Pilih barang secara manual.
- Scan barcode produk.
- Barang hasil scan langsung masuk ke keranjang jika barcode terdaftar.
- Panel keranjang untuk mengubah jumlah barang.
- Pembayaran Cash dan QRIS.
- Format uang otomatis dengan titik.
- Modal transaksi berhasil.
- Rincian barang, total, uang dibayar, dan kembalian.

### Barcode

- Barcode produk menggunakan skema kode 4 angka.
- Contoh:

```text
0001
0002
0003
```

- Barcode dapat ditempel di rak barang.
- Scanner kamera membaca barcode dan mencocokkannya dengan data produk.

### Laporan dan Riwayat

- Ringkasan laporan penjualan.
- Riwayat transaksi.
- Detail riwayat transaksi beserta daftar barang.
- Export laporan transaksi ke PDF.
- Pilihan periode export:
  - Hari ini
  - 7 hari
  - 1 bulan

### Printer Thermal

- Koneksi printer thermal Bluetooth dari halaman Pengaturan.
- Menampilkan daftar perangkat Bluetooth yang sudah dipairing.
- Pilih printer dan simpan pilihan printer.
- Test print.
- Print struk setelah transaksi berhasil.
- Printer target yang digunakan saat pengembangan:

```text
IDY01POS-58B
```

## Pengaturan Aplikasi

Halaman Pengaturan berisi:

- Edit logo koperasi.
- Edit nama toko/koperasi.
- Koneksi printer Bluetooth.
- Test print.
- Import CSV produk.
- Tombol backup data masih disiapkan untuk pengembangan berikutnya.

Nama toko dan logo digunakan untuk identitas aplikasi dan dokumen/struk.

## Penyimpanan Data

Aplikasi menggunakan pendekatan **offline-first**.

Data utama disimpan di perangkat Android menggunakan:

- Room Database
- SQLite lokal
- Internal storage untuk gambar produk dan logo

Artinya aplikasi tetap dapat digunakan tanpa internet. Data produk, stok, transaksi, dan laporan tersimpan di perangkat yang digunakan.

## Teknologi

- Kotlin
- Android
- Jetpack Compose
- Material 3
- MVVM
- Repository Pattern
- Room Database
- SQLite
- ML Kit / Google Code Scanner
- Bluetooth ESC/POS thermal printer
- PDF export lokal

## Struktur Project

Struktur utama kode aplikasi:

```text
app/src/main/java/com/kasirkoperasi/app
```

Folder penting:

```text
core/
```

Berisi komponen umum seperti navigasi, UI reusable, penyimpanan gambar, barcode, PDF, printer, dan pengaturan lokal.

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

## Cara Menggunakan Printer

1. Nyalakan printer thermal.
2. Pair printer dari pengaturan Bluetooth HP.
3. Buka aplikasi.
4. Masuk ke halaman Pengaturan.
5. Tekan `Muat Perangkat`.
6. Pilih printer.
7. Tekan `Test Print`.
8. Setelah transaksi berhasil, tekan tombol `Print`.

Catatan: beberapa printer thermal Bluetooth hanya tersambung saat aplikasi mengirim data print, sehingga lampu indikator bisa tetap berkedip saat standby.

## Status Pengembangan

Fitur yang sudah tersedia:

- Dashboard beranda.
- Kelola produk.
- Foto produk.
- Import CSV produk.
- Barcode scanner.
- Keranjang dan transaksi.
- Pembayaran Cash dan QRIS.
- Riwayat transaksi.
- Detail transaksi.
- Laporan dan export PDF.
- Pengaturan nama toko dan logo.
- Koneksi printer Bluetooth.
- Print struk transaksi.

Fitur yang masih dapat dikembangkan:

- Backup dan restore data.
- Export data lengkap.
- Halaman stok lebih detail.
- Cetak barcode produk.
- Sinkronisasi database online jika dibutuhkan.
- Manajemen multi perangkat.

## Konteks Proyek

Proyek ini dikembangkan untuk mendukung digitalisasi sederhana pada koperasi desa dalam kegiatan **KKN Rekognisi Desa Girimulyo**.

Aplikasi ini diharapkan dapat membantu koperasi desa dalam mencatat penjualan kebutuhan pertanian secara lebih tertata, mengurangi pencatatan manual, dan mempermudah pembuatan laporan sederhana.
