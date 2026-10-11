package com.ticketvault.domain.usecase

import com.ticketvault.camera.ProcessedTicketImage
import com.ticketvault.camera.TicketImageStorage
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.ocr.OcrReadabilityEvaluator
import com.ticketvault.ocr.TextRecognitionGateway
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class RecognizedTicket(
    val ticketId: Long,
    val status: TicketStatus,
    val store: Store?,
)

class RecognizeTicketTextUseCase @Inject constructor(
    private val textRecognitionGateway: TextRecognitionGateway,
    private val readabilityEvaluator: OcrReadabilityEvaluator,
    private val ticketRepository: TicketRepository,
    private val storeRepository: StoreRepository,
    private val ticketImageStorage: TicketImageStorage,
) {
    /**
     * Un fallo del OCR no descarta el ticket: se guarda como
     * [TicketStatus.NEEDS_MANUAL_REVIEW] para no perder las imágenes.
     * Tampoco un fallo al detectar el comercio: el usuario puede asignarlo a mano.
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

        val store = rawText?.let { detectStore(it) }

        return try {
            val ticketId = ticketRepository.saveScannedTicket(
                originalImagePath = image.originalPath,
                cleanedImagePath = image.cleanedPath,
                rawOcrText = rawText,
                status = status,
                storeId = store?.id,
            )
            Result.success(RecognizedTicket(ticketId = ticketId, status = status, store = store))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ticketImageStorage.deleteIfExists(image.originalPath)
            ticketImageStorage.deleteIfExists(image.cleanedPath)
            Result.failure(ProcessImageError.SaveFailed)
        }
    }

    private suspend fun detectStore(rawText: String): Store? =
        try {
            storeRepository.detectStore(rawText)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
}
