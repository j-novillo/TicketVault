package com.ticketvault.camera

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ColorMatrixTicketImageCleanerTest {

    private val cleaner = ColorMatrixTicketImageCleaner(RuntimeEnvironment.getApplication())

    @Test
    fun enhanceBitmap_returnsSameSizeBitmap() {
        val source = Bitmap.createBitmap(12, 8, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(200, 40, 40))
        }

        val result = cleaner.enhanceBitmap(source)

        assertEquals(12, result.width)
        assertEquals(8, result.height)
        assertTrue(!result.isRecycled)
        // Output is a new bitmap (enhancement path ran).
        assertTrue(result !== source)
    }

    @Test
    fun enhanceBitmap_downscalesWhenLongerThanMaxSide() {
        val source = Bitmap.createBitmap(2400, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        }

        val result = cleaner.enhanceBitmap(source)

        assertTrue(result.width <= 2000)
        assertTrue(result.height <= 2000)
        assertEquals(2000, maxOf(result.width, result.height))
    }
}
