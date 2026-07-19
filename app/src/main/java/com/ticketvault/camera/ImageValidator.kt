package com.ticketvault.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class ImageValidationError : Exception() {
    data object UnsupportedFormat : ImageValidationError()
    data object TooLarge : ImageValidationError()
    data object TooSmall : ImageValidationError()
    data object Unreadable : ImageValidationError()
}

@Singleton
class ImageValidator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun validate(uri: Uri): Result<Unit> = runCatching {
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType !in ALLOWED_MIME_TYPES) {
            throw ImageValidationError.UnsupportedFormat
        }

        val sizeBytes = querySizeBytes(uri)
        if (sizeBytes != null && sizeBytes > MAX_SIZE_BYTES) {
            throw ImageValidationError.TooLarge
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: throw ImageValidationError.Unreadable

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw ImageValidationError.Unreadable
        }

        if (bounds.outWidth < MIN_DIMENSION_PX || bounds.outHeight < MIN_DIMENSION_PX) {
            throw ImageValidationError.TooSmall
        }
    }

    private fun querySizeBytes(uri: Uri): Long? {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.SIZE),
            null,
            null,
            null,
        )?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && cursor.moveToFirst() && !cursor.isNull(sizeIndex)) {
                val size = cursor.getLong(sizeIndex)
                if (size >= 0) return size
            }
        }

        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
            val length = descriptor.length
            if (length >= 0) return length
        }

        return null
    }

    companion object {
        private val ALLOWED_MIME_TYPES = setOf(
            "image/jpeg",
            "image/png",
            "image/webp",
        )
        private const val MAX_SIZE_BYTES = 10L * 1024L * 1024L
        private const val MIN_DIMENSION_PX = 200
    }
}
