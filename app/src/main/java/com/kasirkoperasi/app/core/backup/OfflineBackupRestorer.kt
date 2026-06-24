package com.kasirkoperasi.app.core.backup

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import com.kasirkoperasi.app.data.local.database.DatabaseConfig
import com.kasirkoperasi.app.data.local.database.KasirDatabase
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class OfflineBackupRestorer(
    context: Context,
) {
    private val appContext = context.applicationContext

    fun restore(sourceUri: Uri): RestoreResult {
        val tempDirectory = File(appContext.cacheDir, RESTORE_DIRECTORY).apply {
            deleteRecursively()
            mkdirs()
        }

        try {
            unzipBackup(sourceUri, tempDirectory)

            val restoredDatabase = File(tempDirectory, "databases/${DatabaseConfig.DATABASE_NAME}")
            require(restoredDatabase.exists() && restoredDatabase.isFile) {
                "File backup tidak valid: database tidak ditemukan"
            }

            validateDatabase(restoredDatabase)
            KasirDatabase.closeInstance()
            replaceDatabase(tempDirectory)
            replaceDirectory(
                source = File(tempDirectory, "files/$PRODUCT_IMAGE_DIRECTORY"),
                target = File(appContext.filesDir, PRODUCT_IMAGE_DIRECTORY),
            )
            replaceDirectory(
                source = File(tempDirectory, "files/$STORE_PROFILE_DIRECTORY"),
                target = File(appContext.filesDir, STORE_PROFILE_DIRECTORY),
            )
            replaceDirectory(
                source = File(tempDirectory, SHARED_PREFS_DIRECTORY),
                target = File(appContext.applicationInfo.dataDir, SHARED_PREFS_DIRECTORY),
            )

            return RestoreResult(
                restoredFileCount = tempDirectory.walkTopDown().count { it.isFile },
            )
        } finally {
            tempDirectory.deleteRecursively()
        }
    }

    private fun unzipBackup(sourceUri: Uri, targetDirectory: File) {
        val targetRoot = targetDirectory.canonicalFile
        val targetRootPath = targetRoot.path + File.separator

        appContext.contentResolver.openInputStream(sourceUri).use { rawInput ->
            requireNotNull(rawInput) { "File backup tidak bisa dibuka" }

            ZipInputStream(BufferedInputStream(rawInput)).use { zip ->
                generateSequence { zip.nextEntry }
                    .forEach { entry ->
                        if (entry.isDirectory) {
                            zip.closeEntry()
                            return@forEach
                        }

                        val outputFile = File(targetRoot, entry.name).canonicalFile
                        require(outputFile.path.startsWith(targetRootPath)) {
                            "File backup tidak valid"
                        }

                        outputFile.parentFile?.mkdirs()
                        FileOutputStream(outputFile).use { output ->
                            zip.copyTo(output)
                        }
                        zip.closeEntry()
                    }
            }
        }
    }

    private fun validateDatabase(databaseFile: File) {
        SQLiteDatabase.openDatabase(
            databaseFile.path,
            null,
            SQLiteDatabase.OPEN_READONLY,
        ).use { database ->
            val version = database.version
            require(version in 1..DatabaseConfig.DATABASE_VERSION) {
                "Versi database backup belum didukung"
            }

            REQUIRED_TABLES.forEach { tableName ->
                require(database.hasTable(tableName)) {
                    "File backup tidak valid: tabel $tableName tidak ditemukan"
                }
            }
        }
    }

    private fun SQLiteDatabase.hasTable(tableName: String): Boolean {
        rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
            arrayOf(tableName),
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    private fun replaceDatabase(tempDirectory: File) {
        val databaseFiles = listOf(
            DatabaseConfig.DATABASE_NAME,
            "${DatabaseConfig.DATABASE_NAME}-wal",
            "${DatabaseConfig.DATABASE_NAME}-shm",
        )

        databaseFiles.forEach { fileName ->
            val targetFile = appContext.getDatabasePath(fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }

        databaseFiles.forEach { fileName ->
            val sourceFile = File(tempDirectory, "databases/$fileName")
            val targetFile = appContext.getDatabasePath(fileName)
            if (sourceFile.exists() && sourceFile.isFile) {
                targetFile.parentFile?.mkdirs()
                sourceFile.copyTo(targetFile, overwrite = true)
            }
        }
    }

    private fun replaceDirectory(source: File, target: File) {
        if (target.exists()) {
            target.deleteRecursively()
        }
        target.mkdirs()

        if (!source.exists() || !source.isDirectory) return

        source.walkTopDown()
            .filter { file -> file.isFile }
            .forEach { file ->
                val targetFile = File(target, file.relativeTo(source).path)
                targetFile.parentFile?.mkdirs()
                FileInputStream(file).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
    }

    private companion object {
        const val RESTORE_DIRECTORY = "restore"
        const val PRODUCT_IMAGE_DIRECTORY = "product_images"
        const val STORE_PROFILE_DIRECTORY = "store_profile"
        const val SHARED_PREFS_DIRECTORY = "shared_prefs"

        val REQUIRED_TABLES = listOf(
            "products",
            "stock_movements",
            "sales_transactions",
            "sales_transaction_items",
            "debt_payments",
        )
    }
}

data class RestoreResult(
    val restoredFileCount: Int,
)
