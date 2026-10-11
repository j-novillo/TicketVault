package com.ticketvault.domain.usecase

import com.ticketvault.data.repository.ProductClassificationRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.ReviewedTicket
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Guarda los datos revisados del ticket y sustituye sus artículos en una sola transacción.
 * Las categorías corregidas a mano se recuerdan para clasificar igual ese artículo en el futuro.
 */
class SaveReviewedTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val classificationRepository: ProductClassificationRepository,
) {
    suspend operator fun invoke(ticketId: Long, reviewed: ReviewedTicket): Result<Unit> =
        try {
            ticketRepository.saveReviewedTicket(ticketId, reviewed)
            val corrections = reviewed.items
                .filter { it.isCategoryCorrected }
                .mapNotNull { item -> item.category?.let { item.name to it } }
                .toMap()
            classificationRepository.learnCorrections(corrections)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
