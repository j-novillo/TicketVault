package com.ticketvault.domain.usecase

import com.ticketvault.camera.ProcessedTicketImage
import com.ticketvault.camera.TicketImageStorage
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.ocr.OcrReadabilityEvaluator
import com.ticketvault.ocr.TextRecognitionGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecognizeTicketTextUseCaseTest {

    private val textRecognitionGateway = mockk<TextRecognitionGateway>()
    private val ticketRepository = mockk<TicketRepository>()
    private val ticketImageStorage = mockk<TicketImageStorage>()
    private val useCase = RecognizeTicketTextUseCase(
        textRecognitionGateway = textRecognitionGateway,
        readabilityEvaluator = OcrReadabilityEvaluator(),
        ticketRepository = ticketRepository,
        ticketImageStorage = ticketImageStorage,
    )
    private val image = ProcessedTicketImage(
        originalPath = "/tickets/original/a.jpg",
        cleanedPath = "/tickets/cleaned/b.jpg",
    )

    private val readableText = """
        MERCADONA, S.A.
        1 LECHE ENTERA      0,95
        TOTAL (€)           0,95
    """.trimIndent()

    @Test
    fun invoke_withReadableText_savesTicketAsOcrCompleted() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns
            Result.success("  $readableText\n")
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any()) } returns 7L

        val result = useCase(image)

        assertEquals(RecognizedTicket(ticketId = 7L, status = TicketStatus.OCR_COMPLETED), result.getOrNull())
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(
                originalImagePath = image.originalPath,
                cleanedImagePath = image.cleanedPath,
                rawOcrText = readableText,
                status = TicketStatus.OCR_COMPLETED,
            )
        }
    }

    @Test
    fun invoke_withIllegibleText_savesTicketAsNeedsManualReview() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns Result.success("~ . |")
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any()) } returns 3L

        val result = useCase(image)

        assertEquals(TicketStatus.NEEDS_MANUAL_REVIEW, result.getOrNull()?.status)
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(any(), any(), "~ . |", TicketStatus.NEEDS_MANUAL_REVIEW)
        }
    }

    @Test
    fun invoke_whenOcrFails_savesTicketWithoutTextAsNeedsManualReview() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns
            Result.failure(IllegalStateException("ocr failed"))
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any()) } returns 4L

        val result = useCase(image)

        assertEquals(TicketStatus.NEEDS_MANUAL_REVIEW, result.getOrNull()?.status)
        coVerify(exactly = 1) {
            ticketRepository.saveScannedTicket(any(), any(), null, TicketStatus.NEEDS_MANUAL_REVIEW)
        }
    }

    @Test
    fun invoke_whenSaveFails_deletesImagesAndReturnsSaveFailed() = runTest {
        coEvery { textRecognitionGateway.recognizeText(image.cleanedPath) } returns Result.success(readableText)
        coEvery { ticketRepository.saveScannedTicket(any(), any(), any(), any()) } throws
            IllegalStateException("db failed")
        coEvery { ticketImageStorage.deleteIfExists(any()) } returns Unit

        val result = useCase(image)

        assertEquals(ProcessImageError.SaveFailed, result.exceptionOrNull())
        coVerify(exactly = 1) { ticketImageStorage.deleteIfExists(image.originalPath) }
        coVerify(exactly = 1) { ticketImageStorage.deleteIfExists(image.cleanedPath) }
    }
}
