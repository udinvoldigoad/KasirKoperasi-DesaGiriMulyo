package com.kasirkoperasi.app.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ProductImageStore {
    data class CleanupResult(
        val deletedCount: Int,
        val freedBytes: Long,
    )

    fun createCameraImageUri(context: Context): Uri {
        val directory = File(context.cacheDir, CAMERA_DIRECTORY).apply {
            mkdirs()
        }
        val file = File.createTempFile("product-camera-", ".jpg", directory)

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    fun persistImage(context: Context, sourceUri: Uri): String {
        val directory = File(context.filesDir, PRODUCT_IMAGE_DIRECTORY).apply {
            mkdirs()
        }
        val targetFile = File(directory, "product-${System.currentTimeMillis()}.webp")

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(sourceUri).use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = bounds.calculateInSampleSize(SAVED_IMAGE_MAX_SIZE, SAVED_IMAGE_MAX_SIZE)
        }
        val decodedBitmap = context.contentResolver.openInputStream(sourceUri).use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: error("Gagal membaca gambar produk")

        val outputBitmap = decodedBitmap.scaleDown(SAVED_IMAGE_MAX_SIZE)
        FileOutputStream(targetFile).use { output ->
            outputBitmap.compress(Bitmap.CompressFormat.WEBP, WEBP_QUALITY, output)
        }

        if (outputBitmap != decodedBitmap) {
            outputBitmap.recycle()
        }
        decodedBitmap.recycle()
        clearCameraCache(context)

        return Uri.fromFile(targetFile).toString()
    }

    fun deleteImage(imageUri: String?) {
        if (imageUri.isNullOrBlank()) return

        runCatching {
            val uri = Uri.parse(imageUri)
            if (uri.scheme != "file") return

            val file = File(requireNotNull(uri.path))
            if (file.parentFile?.name == PRODUCT_IMAGE_DIRECTORY && file.exists()) {
                file.delete()
            }
        }
    }

    fun loadBitmap(context: Context, imageUri: String, targetSize: Int = DEFAULT_TARGET_SIZE): Bitmap? {
        return runCatching {
            val uri = Uri.parse(imageUri)
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            context.contentResolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input, null, bounds)
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = bounds.calculateInSampleSize(targetSize, targetSize)
            }

            context.contentResolver.openInputStream(uri).use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            }
        }.getOrNull()
    }

    fun cleanupUnusedImages(
        context: Context,
        activeImageUris: Set<String>,
    ): CleanupResult {
        val directory = File(context.filesDir, PRODUCT_IMAGE_DIRECTORY)
        val cameraCleanup = clearCameraCache(context)
        if (!directory.exists()) return cameraCleanup

        var deletedCount = 0
        var freedBytes = 0L
        val activeFiles = activeImageUris
            .mapNotNull { imageUri -> imageUri.toOwnedProductImageFileOrNull() }
            .map { it.canonicalPath }
            .toSet()

        directory.listFiles()
            .orEmpty()
            .filter { file -> file.isFile && file.canonicalPath !in activeFiles }
            .forEach { file ->
                val fileSize = file.length()
                if (file.delete()) {
                    deletedCount += 1
                    freedBytes += fileSize
                }
            }

        return CleanupResult(
            deletedCount = deletedCount + cameraCleanup.deletedCount,
            freedBytes = freedBytes + cameraCleanup.freedBytes,
        )
    }

    private fun clearCameraCache(context: Context): CleanupResult {
        val directory = File(context.cacheDir, CAMERA_DIRECTORY)
        if (!directory.exists()) return CleanupResult(deletedCount = 0, freedBytes = 0L)

        var deletedCount = 0
        var freedBytes = 0L
        directory.listFiles()
            .orEmpty()
            .filter { it.isFile }
            .forEach { file ->
                val fileSize = file.length()
                if (file.delete()) {
                    deletedCount += 1
                    freedBytes += fileSize
                }
            }

        return CleanupResult(
            deletedCount = deletedCount,
            freedBytes = freedBytes,
        )
    }

    private fun BitmapFactory.Options.calculateInSampleSize(
        requestedWidth: Int,
        requestedHeight: Int,
    ): Int {
        val height = outHeight
        val width = outWidth
        var sampleSize = 1

        if (height > requestedHeight || width > requestedWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2

            while (halfHeight / sampleSize >= requestedHeight && halfWidth / sampleSize >= requestedWidth) {
                sampleSize *= 2
            }
        }

        return sampleSize.coerceAtLeast(1)
    }

    private fun Bitmap.scaleDown(maxSize: Int): Bitmap {
        val largestSide = maxOf(width, height)
        if (largestSide <= maxSize) return this

        val scale = maxSize.toFloat() / largestSide
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(this, targetWidth, targetHeight, true)
    }

    private fun String.toOwnedProductImageFileOrNull(): File? {
        val uri = Uri.parse(this)
        if (uri.scheme != "file") return null

        val file = File(uri.path ?: return null)
        return file.takeIf {
            it.parentFile?.name == PRODUCT_IMAGE_DIRECTORY
        }
    }

    private const val CAMERA_DIRECTORY = "camera"
    private const val PRODUCT_IMAGE_DIRECTORY = "product_images"
    private const val DEFAULT_TARGET_SIZE = 512
    private const val SAVED_IMAGE_MAX_SIZE = 512
    private const val WEBP_QUALITY = 75
}
