package com.kasirkoperasi.app.core.settings

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object StoreProfileStore {
    fun load(context: Context): StoreProfile {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        val storeName = preferences.getString(KEY_STORE_NAME, DEFAULT_STORE_NAME)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: DEFAULT_STORE_NAME
        val logoUri = preferences.getString(KEY_LOGO_URI, null)
            ?.takeIf { it.isNotBlank() }

        return StoreProfile(
            storeName = storeName,
            logoUri = logoUri,
        )
    }

    fun saveStoreName(context: Context, storeName: String): StoreProfile {
        val normalizedName = storeName.trim().ifEmpty { DEFAULT_STORE_NAME }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_STORE_NAME, normalizedName)
            .apply()

        return load(context)
    }

    fun saveLogo(context: Context, sourceUri: Uri): StoreProfile {
        val oldLogoUri = load(context).logoUri
        val directory = File(context.filesDir, LOGO_DIRECTORY).apply {
            mkdirs()
        }
        val targetFile = File(directory, "store-logo-${System.currentTimeMillis()}.jpg")

        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(sourceUri).use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = bounds.calculateInSampleSize(SAVED_LOGO_MAX_SIZE, SAVED_LOGO_MAX_SIZE)
        }
        val decodedBitmap = context.contentResolver.openInputStream(sourceUri).use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: error("Gagal membaca logo koperasi")

        val outputBitmap = decodedBitmap.scaleDown(SAVED_LOGO_MAX_SIZE)
        FileOutputStream(targetFile).use { output ->
            outputBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
        }

        if (outputBitmap != decodedBitmap) {
            outputBitmap.recycle()
        }
        decodedBitmap.recycle()

        val logoUri = Uri.fromFile(targetFile).toString()
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOGO_URI, logoUri)
            .apply()

        deleteStoredLogo(oldLogoUri)
        return load(context)
    }

    fun loadLogoBitmap(context: Context, logoUri: String?, targetSize: Int = DEFAULT_LOGO_TARGET_SIZE): Bitmap? {
        if (logoUri.isNullOrBlank()) return null

        return runCatching {
            val uri = Uri.parse(logoUri)
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

    private fun deleteStoredLogo(logoUri: String?) {
        if (logoUri.isNullOrBlank()) return

        runCatching {
            val uri = Uri.parse(logoUri)
            if (uri.scheme != "file") return

            val file = File(requireNotNull(uri.path))
            if (file.parentFile?.name == LOGO_DIRECTORY && file.exists()) {
                file.delete()
            }
        }
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

    private const val PREFERENCES_NAME = "store_profile"
    private const val KEY_STORE_NAME = "store_name"
    private const val KEY_LOGO_URI = "logo_uri"
    private const val LOGO_DIRECTORY = "store_profile"
    private const val DEFAULT_LOGO_TARGET_SIZE = 160
    private const val SAVED_LOGO_MAX_SIZE = 512
    private const val JPEG_QUALITY = 85
}
