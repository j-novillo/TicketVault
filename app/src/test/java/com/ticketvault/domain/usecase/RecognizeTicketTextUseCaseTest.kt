package com.ticketvault.domain.usecase

import com.ticketvault.camera.ProcessedTicketImage
import com.ticketvault.camera.TicketImageStorage
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.ocr.OcrReadabilityEvaluator
import com.ticketvault.ocr.TextRecognitionGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecognizeTicketTextUseCaseTest {

    private val textRecognitionGateway = mockk<TextRecognitionGateway>()
    private val ticketRepository = mockk<TicketRepository>()
    private val storeRepository = mockk<StoreRepository>()
    private val ticketImageStorage = mockk<TicketImageStorage>()
    private val useCase = RecognizeTicketTextUseCase(
        textRecognitionGateway = textRecognitionGateway,
        readabilityEvaluator = OcrReadabilityEvaluator(),
        ticketRepository = ticketRepository,
        storeRepository = storeRepository,
        ticketImageStorage = ticketImageStorage,
    )
    private val image = ProcessedTicketImage(
        originalPath = "/tickets/original/a.jpg",
        cleanedPath = "/tickets/cleaned/b.jpg",
    )
    private val mercadona = Store(id = 1L, name = "Mercadona")

    private val readableText = """
        MERCADONA, S.A.
        1 LECHE ENTERA      0,95
        TOTAL (€)           0,95
    """.trimIndent()

    @Test
    fun invoke_withReadableText_savesTicketAsOcrCompletedWithDetectedStore() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns
            Result.success("  $readableText\n")
        coEvery { storeRepository.detectStore(readableText) } returns mercadona
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any(), any()) } returns 7L

        val result = useCase(image)

        assertEquals(
            RecognizedTicket(ticketId = 7L, status = TicketStatus.OCR_COMPLETED, store = mercadona),
            result.getOrNull(),
        )
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(
                originalImagePath = image.originalPath,
                cleanedImagePath = image.cleanedPath,
                rawOcrText = readableText,
                status = TicketStatus.OCR_COMPLETED,
                storeId = mercadona.id,
            )
        }
    }

    @Test
    fun invoke_whenStoreNotDetected_savesTicketWithoutStore() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns Result.success(readableText)
        coEvery { storeRepository.detectStore(any()) } returns null
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any(), any()) } returns 8L

        val result = useCase(image)

        assertNull(result.getOrNull()?.store)
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(any(), any(), readableText, TicketStatus.OCR_COMPLETED, null)
        }
    }

    @Test
    fun invoke_whenStoreDetectionFails_stillSavesTicketWithoutStore() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns Result.success(readableText)
        coEvery { storeRepository.detectStore(any()) } throws IllegalStateException("assets missing")
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any(), any()) } returns 9L

        val result = useCase(image)

        assertEquals(9L, result.getOrNull()?.ticketId)
        assertNull(result.getOrNull()?.store)
    }

    @Test
    fun invoke_withIllegibleText_savesTicketAsNeedsManualReview() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns Result.success("~ . |")
        coEvery { storeRepository.detectStore(any()) } returns null
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any(), any()) } returns 3L

        val result = useCase(image)

        assertEquals(TicketStatus.NEEDS_MANUAL_REVIEW, result.getOrNull()?.status)
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(any(), any(), "~ . |", TicketStatus.NEEDS_MANUAL_REVIEW, null)
        }
    }

    @Test
    fun invoke_whenOcrFails_savesTicketWithoutTextAsNeedsManualReview() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns
            Result.failure(IllegalStateException("ocr failed"))
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any(), any()) } returns 4L

        val result = useCase(image)

        assertEquals(TicketStatus.NEEDS_MANUAL_REVIEW, result.getOrNull()?.status)
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(any(), any(), null, TicketStatus.NEEDS_MANUAL_REVIEW, null)
        }
        coVerify(exactly = 0) { storeRepository.detectStore(any()) }
    }

    @Test
    fun invoke_whenSaveFails_deletesImagesAndReturnsSaveFailed() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns Result.success(readableText)
        coEvery { storeRepository.detectStore(any()) } returns mercadona
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any(), any()) } throws
            IllegalStateException("db failed")
        coEvery { ticketImageStorage.deleteIfExists(any()) } returns Unit

        val result = useCase(image)

        assertEquals(ProcessImageError.SaveFailed, result.exceptionOrNull())
        coVerify(exactly = 1) { ticketImageStorage.deleteIfExists(image.originalPath) }
        coVerify(exactly = 1) { ticketImageStorage.deleteIfExists(image.cleanedPath) }
    }
}
