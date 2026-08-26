package com.ticketvault.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream

internal object ImageUriReader {

    fun decodeBounds(context: Context, uri: Uri): BitmapFactory.Options? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        return if (decodeIntoBounds(context, uri, bounds)) bounds else null
    }

    fun decodeBitmap(
        context: Context,
        uri: Uri,
        options: BitmapFactory.Options = BitmapFactory.Options(),
    ): Bitmap? {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, options)
                ?.let { return it }
        }

        return openBufferedInputStream(context, uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, options)
        }
    }

    fun openInputStream(context: Context, uri: Uri): InputStream? {
        openBufferedInputStream(context, uri)?.let { return it }

        if (uri.scheme == "file") {
            uri.path?.let { path ->
                val file = File(path)
                if (file.exists()) return file.inputStream()
            }
        }

        return null
    }

    fun copyTo(context: Context, uri: Uri, destination: java.io.OutputStream) {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { input ->
                input.copyTo(destination)
                return
            }
        }

        openBufferedInputStream(context, uri)?.use { input ->
            input.copyTo(destination)
            return
        }

        if (uri.scheme == "file") {
            uri.path?.let { path ->
                File(path).takeIf { it.exists() }?.inputStream()?.use { input ->
                    input.copyTo(destination)
                    return
                }
            }
        }

        error("No se pudo leer la imagen")
    }

    private fun decodeIntoBounds(
        context: Context,
        uri: Uri,
        bounds: BitmapFactory.Options,
    ): Boolean {
        context.contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
            BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor, null, bounds)
            if (bounds.outWidth > 0 && bounds.outHeight > 0) return true
        }

        openBufferedInputStream(context, uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }

        return bounds.outWidth > 0 && bounds.outHeight > 0
    }

    private fun openBufferedInputStream(context: Context, uri: Uri): InputStream? {
        return try {
            context.contentResolver.openInputStream(uri)?.let(::BufferedInputStream)
        } catch (_: Exception) {
            null
        }
    }
}
