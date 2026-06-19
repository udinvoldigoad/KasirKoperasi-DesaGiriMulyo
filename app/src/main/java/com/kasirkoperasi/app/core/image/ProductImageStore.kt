package com.kasirkoperasi.app.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ProductImageStore {
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
        val targetFile = File(directory, "product-${System.currentTimeMillis()}.jpg")

        context.contentResolver.openInputStream(sourceUri).use { input ->
            requireNotNull(input) { "Gagal membaca gambar produk" }
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }

        return Uri.fromFile(targetFile).toString()
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

    private const val CAMERA_DIRECTORY = "camera"
    private const val PRODUCT_IMAGE_DIRECTORY = "product_images"
    private const val DEFAULT_TARGET_SIZE = 512
}
