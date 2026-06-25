package com.kasirkoperasi.app.core.backup

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.net.Uri
import com.kasirkoperasi.app.data.local.database.DatabaseConfig
import com.kasirkoperasi.app.data.local.database.KasirDatabase
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class OfflineBackupExporter(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun export(): BackupExportResult {
        checkpointDatabase()

        val timestamp = BACKUP_FILE_DATE_FORMAT.format(Date())
        val fileName = "kasir-koperasi-backup-$timestamp.zip"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportToPublicDownloads(fileName, timestamp)
        } else {
            exportToAppDownloads(fileName, timestamp)
        }
    }

    private fun exportToPublicDownloads(
        fileName: String,
        timestamp: String,
    ): BackupExportResult {
        val resolver = appContext.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, ZIP_MIME_TYPE)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/KasirKoperasi")
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Gagal membuat file backup di folder Download")

        return try {
            resolver.openOutputStream(uri)?.use { output ->
                writeBackupZip(output, timestamp)
            } ?: error("Gagal menulis file backup")

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            BackupExportResult(
                fileName = fileName,
                uri = uri,
                locationText = "Download/KasirKoperasi/$fileName",
            )
        } catch (throwable: Throwable) {
            resolver.delete(uri, null, null)
            throw throwable
        }
    }

    private fun exportToAppDownloads(
        fileName: String,
        timestamp: String,
    ): BackupExportResult {
        val backupDirectory = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: File(appContext.filesDir, BACKUP_DIRECTORY)
        backupDirectory.mkdirs()
        val backupFile = File(backupDirectory, fileName)

        FileOutputStream(backupFile).use { output ->
            writeBackupZip(output, timestamp)
        }

        return BackupExportResult(
            fileName = fileName,
            uri = Uri.fromFile(backupFile),
            locationText = backupFile.absolutePath,
        )
    }

    private fun writeBackupZip(
        outputStream: OutputStream,
        timestamp: String,
    ) {
        ZipOutputStream(BufferedOutputStream(outputStream)).use { zip ->
            zip.addTextEntry(
                entryName = "README_BACKUP.txt",
                text = buildReadme(timestamp),
            )
            zip.addFileIfExists(
                source = appContext.getDatabasePath(DatabaseConfig.DATABASE_NAME),
                entryName = "databases/${DatabaseConfig.DATABASE_NAME}",
            )
            zip.addFileIfExists(
                source = appContext.getDatabasePath("${DatabaseConfig.DATABASE_NAME}-wal"),
                entryName = "databases/${DatabaseConfig.DATABASE_NAME}-wal",
            )
            zip.addFileIfExists(
                source = appContext.getDatabasePath("${DatabaseConfig.DATABASE_NAME}-shm"),
                entryName = "databases/${DatabaseConfig.DATABASE_NAME}-shm",
            )
            zip.addDirectoryIfExists(
                source = File(appContext.filesDir, PRODUCT_IMAGE_DIRECTORY),
                entryPrefix = "files/$PRODUCT_IMAGE_DIRECTORY",
            )
            zip.addDirectoryIfExists(
                source = File(appContext.filesDir, STORE_PROFILE_DIRECTORY),
                entryPrefix = "files/$STORE_PROFILE_DIRECTORY",
            )
            zip.addDirectoryIfExists(
                source = File(appContext.applicationInfo.dataDir, SHARED_PREFS_DIRECTORY),
                entryPrefix = SHARED_PREFS_DIRECTORY,
            )
        }
    }

    private fun checkpointDatabase() {
        val database = KasirDatabase.getInstance(appContext)
        database.openHelper.writableDatabase
            .query("PRAGMA wal_checkpoint(FULL)")
            .use { cursor ->
                while (cursor.moveToNext()) {
                    // Drain the cursor so SQLite completes the checkpoint before files are copied.
                }
            }
    }

    private fun buildReadme(timestamp: String): String {
        return """
            Backup KasirKoperasi
            Dibuat: $timestamp

            Isi backup:
            - databases/${DatabaseConfig.DATABASE_NAME}: data produk, stok, transaksi, hutang, pengeluaran, laporan
            - files/$PRODUCT_IMAGE_DIRECTORY: foto produk
            - files/$STORE_PROFILE_DIRECTORY: logo koperasi
            - $SHARED_PREFS_DIRECTORY: nama toko dan pilihan printer

            Simpan file ZIP ini di tempat aman, misalnya Google Drive, flashdisk OTG, atau laptop.
            Restore file ini lewat Pengaturan > Data Produk > Restore Data.
            Jangan edit isi ZIP secara manual.
        """.trimIndent()
    }

    private fun ZipOutputStream.addTextEntry(entryName: String, text: String) {
        putNextEntry(ZipEntry(entryName).apply { time = System.currentTimeMillis() })
        write(text.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun ZipOutputStream.addFileIfExists(source: File, entryName: String) {
        if (!source.exists() || !source.isFile) return

        putNextEntry(ZipEntry(entryName).apply { time = source.lastModified() })
        FileInputStream(source).use { input ->
            input.copyTo(this)
        }
        closeEntry()
    }

    private fun ZipOutputStream.addDirectoryIfExists(source: File, entryPrefix: String) {
        if (!source.exists() || !source.isDirectory) return

        source.walkTopDown()
            .filter { file -> file.isFile }
            .forEach { file ->
                val relativePath = file.relativeTo(source)
                    .invariantSeparatorsPath
                addFileIfExists(
                    source = file,
                    entryName = "$entryPrefix/$relativePath",
                )
            }
    }

    private companion object {
        const val BACKUP_DIRECTORY = "backups"
        const val PRODUCT_IMAGE_DIRECTORY = "product_images"
        const val STORE_PROFILE_DIRECTORY = "store_profile"
        const val SHARED_PREFS_DIRECTORY = "shared_prefs"
        const val ZIP_MIME_TYPE = "application/zip"

        val BACKUP_FILE_DATE_FORMAT = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
    }
}

data class BackupExportResult(
    val fileName: String,
    val uri: Uri,
    val locationText: String,
)
