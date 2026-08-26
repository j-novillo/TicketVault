package com.ticketvault.camera

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketImageStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val originalDir: File
        get() = File(context.filesDir, ORIGINAL_DIR).also { it.mkdirs() }

    private val cleanedDir: File
        get() = File(context.filesDir, CLEANED_DIR).also { it.mkdirs() }

    suspend fun saveOriginalFromUri(sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val destination = File(originalDir, "${UUID.randomUUID()}.jpg")
            destination.outputStream().use { output ->
                ImageUriReader.copyTo(context, sourceUri, output)
            }
            destination.absolutePath
        }
    }

    suspend fun saveCleanedBitmap(bitmap: Bitmap): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val destination = File(cleanedDir, "${UUID.randomUUID()}.jpg")
            destination.outputStream().use { output ->
                val compressed = bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
                if (!compressed) error("No se pudo guardar la imagen limpia")
            }
            destination.absolutePath
        }
    }

    suspend fun deleteIfExists(path: String): Unit = withContext(Dispatchers.IO) {
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    companion object {
        private const val ORIGINAL_DIR = "tickets/original"
        private const val CLEANED_DIR = "tickets/cleaned"
        private const val JPEG_QUALITY = 90
    }
}
