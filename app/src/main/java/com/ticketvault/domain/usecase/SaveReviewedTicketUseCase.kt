package com.ticketvault.domain.usecase

import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.ReviewedTicket
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** Guarda los datos revisados del ticket y sustituye sus artículos en una sola transacción. */
class SaveReviewedTicketUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
) {
    suspend operator fun invoke(ticketId: Long, reviewed: ReviewedTicket): Result<Unit> =
        try {
            ticketRepository.saveReviewedTicket(ticketId, reviewed)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
