package com.ticketvault.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

interface TicketImageCleaner {
    suspend fun enhance(sourceUri: Uri): Result<Bitmap>
}

@Singleton
class ColorMatrixTicketImageCleaner @Inject constructor(
    @ApplicationContext private val context: Context,
) : TicketImageCleaner {

    override suspend fun enhance(sourceUri: Uri): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
            val original = decodeScaledBitmap(sourceUri)
                ?: error("No se pudo decodificar la imagen")
            applyEnhancement(original).also { enhanced ->
                if (enhanced !== original) {
                    original.recycle()
                }
            }
        }
    }

    private fun decodeScaledBitmap(sourceUri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: return null

        val sampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, MAX_LONG_SIDE_PX)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        }
    }

    private fun applyEnhancement(source: Bitmap): Bitmap = enhanceBitmap(source)

    /**
     * Applies grayscale + contrast/brightness. Visible for unit tests (Robolectric).
     */
    internal fun enhanceBitmap(source: Bitmap): Bitmap {
        val scaled = scaleIfNeeded(source)
        val output = Bitmap.createBitmap(scaled.width, scaled.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(enhancementMatrix())
        }
        canvas.drawBitmap(scaled, 0f, 0f, paint)
        if (scaled !== source) {
            scaled.recycle()
        }
        return output
    }

    private fun scaleIfNeeded(source: Bitmap): Bitmap {
        val longSide = max(source.width, source.height)
        if (longSide <= MAX_LONG_SIDE_PX) return source

        val scale = MAX_LONG_SIDE_PX.toFloat() / longSide.toFloat()
        val targetWidth = (source.width * scale).roundToInt().coerceAtLeast(1)
        val targetHeight = (source.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
    }

    private fun enhancementMatrix(): ColorMatrix {
        val grayscale = ColorMatrix().apply { setSaturation(0f) }
        val contrastBrightness = ColorMatrix(
            floatArrayOf(
                CONTRAST, 0f, 0f, 0f, BRIGHTNESS,
                0f, CONTRAST, 0f, 0f, BRIGHTNESS,
                0f, 0f, CONTRAST, 0f, BRIGHTNESS,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
        grayscale.postConcat(contrastBrightness)
        return grayscale
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxLongSide: Int): Int {
        var sampleSize = 1
        var longSide = max(width, height)
        while (longSide / (sampleSize * 2) >= maxLongSide) {
            sampleSize *= 2
        }
        return sampleSize
    }

    companion object {
        private const val MAX_LONG_SIDE_PX = 2000
        private const val CONTRAST = 1.25f
        private const val BRIGHTNESS = 12f
    }
}
