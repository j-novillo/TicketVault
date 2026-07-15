package com.ticketvault.data.repository

import com.ticketvault.data.local.dao.TicketDao
import com.ticketvault.domain.model.Ticket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TicketRepository @Inject constructor(
    private val ticketDao: TicketDao,
) {
    fun observeTickets(): Flow<List<Ticket>> =
        ticketDao.observeAll().map { entities ->
            entities.map { entity ->
                Ticket(
                    id = entity.id,
                    storeName = null,
                    date = entity.date,
                    total = entity.total,
                )
            }
        }
}
