package com.ticketvault.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.ticketvault.camera.ImageValidationError
import com.ticketvault.camera.ImageValidator
import com.ticketvault.camera.TicketImageCleaner
import com.ticketvault.camera.TicketImageStorage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcessScannedImageUseCaseTest {

    private val imageValidator = mockk<ImageValidator>()
    private val ticketImageStorage = mockk<TicketImageStorage>()
    private val ticketImageCleaner = mockk<TicketImageCleaner>()
    private val useCase = ProcessScannedImageUseCase(
        imageValidator = imageValidator,
        ticketImageStorage = ticketImageStorage,
        ticketImageCleaner = ticketImageCleaner,
    )
    private val sourceUri = mockk<Uri>()

    @Test
    fun invoke_whenValidationFails_returnsValidationError() = runTest {
        every { imageValidator.validate(sourceUri) } returns
            Result.failure(ImageValidationError.TooSmall)

        val result = useCase(sourceUri)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is ProcessImageError.Validation)
        coVerify(exactly = 0) { ticketImageStorage.saveOriginalFromUri(any()) }
    }

    @Test
    fun invoke_whenEnhanceFails_deletesOriginalAndReturnsEnhanceFailed() = runTest {
        every { imageValidator.validate(sourceUri) } returns Result.success(Unit)
        coEvery { ticketImageStorage.saveOriginalFromUri(sourceUri) } returns
            Result.success("/tickets/original/a.jpg")
        coEvery { ticketImageCleaner.enhance(sourceUri) } returns
            Result.failure(IllegalStateException("enhance failed"))
        coEvery { ticketImageStorage.deleteIfExists("/tickets/original/a.jpg") } returns Unit

        val result = useCase(sourceUri)

        assertEquals(ProcessImageError.EnhanceFailed, result.exceptionOrNull())
        coVerify(exactly = 1) { ticketImageStorage.deleteIfExists("/tickets/original/a.jpg") }
        coVerify(exactly = 0) { ticketImageStorage.saveCleanedBitmap(any()) }
    }

    @Test
    fun invoke_whenSaveCleanedFails_deletesOriginalAndReturnsSaveFailed() = runTest {
        val bitmap = mockk<Bitmap>(relaxed = true) {
            every { isRecycled } returns false
        }
        every { imageValidator.validate(sourceUri) } returns Result.success(Unit)
        coEvery { ticketImageStorage.saveOriginalFromUri(sourceUri) } returns
            Result.success("/tickets/original/a.jpg")
        coEvery { ticketImageCleaner.enhance(sourceUri) } returns Result.success(bitmap)
        coEvery { ticketImageStorage.saveCleanedBitmap(bitmap) } returns
            Result.failure(IllegalStateException("save cleaned failed"))
        coEvery { ticketImageStorage.deleteIfExists("/tickets/original/a.jpg") } returns Unit

        val result = useCase(sourceUri)

        assertEquals(ProcessImageError.SaveFailed, result.exceptionOrNull())
        coVerify(exactly = 1) { ticketImageStorage.deleteIfExists("/tickets/original/a.jpg") }
    }

    @Test
    fun invoke_whenSuccess_returnsBothPaths() = runTest {
        val bitmap = mockk<Bitmap>(relaxed = true) {
            every { isRecycled } returns false
        }
        every { imageValidator.validate(sourceUri) } returns Result.success(Unit)
        coEvery { ticketImageStorage.saveOriginalFromUri(sourceUri) } returns
            Result.success("/tickets/original/a.jpg")
        coEvery { ticketImageCleaner.enhance(sourceUri) } returns Result.success(bitmap)
        coEvery { ticketImageStorage.saveCleanedBitmap(bitmap) } returns
            Result.success("/tickets/cleaned/b.jpg")

        val result = useCase(sourceUri)

        assertTrue(result.isSuccess)
        assertEquals("/tickets/original/a.jpg", result.getOrNull()?.originalPath)
        assertEquals("/tickets/cleaned/b.jpg", result.getOrNull()?.cleanedPath)
        coVerify(exactly = 0) { ticketImageStorage.deleteIfExists(any()) }
    }
}
