package com.ticketvault.camera

import android.content.Context
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
    private val ticketsDir: File
        get() = File(context.filesDir, TICKETS_DIR).also { it.mkdirs() }

    suspend fun saveFromUri(sourceUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val destination = File(ticketsDir, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destination.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("No se pudo leer la imagen escaneada")
            destination.absolutePath
        }
    }

    companion object {
        private const val TICKETS_DIR = "tickets"
    }
}
