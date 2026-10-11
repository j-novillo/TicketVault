package com.ticketvault.domain.usecase

import com.ticketvault.camera.ProcessedTicketImage
import com.ticketvault.camera.TicketImageStorage
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.ocr.OcrReadabilityEvaluator
import com.ticketvault.ocr.TextRecognitionGateway
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class RecognizedTicket(
    val ticketId: Long,
    val status: TicketStatus,
)

class RecognizeTicketTextUseCase @Inject constructor(
    private val textRecognitionGateway: TextRecognitionGateway,
    private val readabilityEvaluator: OcrReadabilityEvaluator,
    private val ticketRepository: TicketRepository,
    private val ticketImageStorage: TicketImageStorage,
) {
    /**
     * Un fallo del OCR no descarta el ticket: se guarda como
     * [TicketStatus.NEEDS_MANUAL_REVIEW] para no perder las imágenes.
     */
    suspend operator fun invoke(image: ProcessedTicketImage): Result<RecognizedTicket> {
        val rawText = textRecognitionGateway.recognizeText(image.cleanedPath)
            .getOrNull()
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

        val status = if (rawText != null && readabilityEvaluator.isReadable(rawText)) {
            TicketStatus.OCR_COMPLETED
        } else {
            TicketStatus.NEEDS_MANUAL_REVIEW
        }

        return try {
            val ticketId = ticketRepository.saveScannedTicket(
                originalImagePath = image.originalPath,
                cleanedImagePath = image.cleanedPath,
                rawOcrText = rawText,
                status = status,
            )
            Result.success(RecognizedTicket(ticketId = ticketId, status = status))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ticketImageStorage.deleteIfExists(image.originalPath)
            ticketImageStorage.deleteIfExists(image.cleanedPath)
            Result.failure(ProcessImageError.SaveFailed)
        }
    }
}
