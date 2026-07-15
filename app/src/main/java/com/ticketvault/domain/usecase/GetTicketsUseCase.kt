package com.ticketvault.domain.usecase

import com.ticketvault.data.repository.TicketRepository
import javax.inject.Inject

class GetTicketsUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
) {
    operator fun invoke() = ticketRepository.observeTickets()
}
