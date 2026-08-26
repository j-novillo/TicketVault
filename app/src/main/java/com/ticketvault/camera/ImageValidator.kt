package com.ticketvault.camera

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
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
        val sizeBytes = querySizeBytes(uri)
        if (sizeBytes != null && sizeBytes > MAX_SIZE_BYTES) {
            throw ImageValidationError.TooLarge
        }

        val bounds = ImageUriReader.decodeBounds(context, uri)
            ?: throw ImageValidationError.Unreadable

        val mimeType = resolveMimeType(uri, bounds)
        if (mimeType != null && mimeType !in ALLOWED_MIME_TYPES) {
            throw ImageValidationError.UnsupportedFormat
        }

        if (bounds.outWidth < MIN_DIMENSION_PX || bounds.outHeight < MIN_DIMENSION_PX) {
            throw ImageValidationError.TooSmall
        }
    }

    private fun resolveMimeType(uri: Uri, bounds: BitmapFactory.Options): String? {
        context.contentResolver.getType(uri)?.let { return it }

        bounds.outMimeType?.let { return it }

        uri.lastPathSegment
            ?.substringAfterLast('.', missingDelimiterValue = "")
            ?.takeIf { it.isNotBlank() }
            ?.lowercase()
            ?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
            ?.let { return it }

        return null
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
