package com.ticketvault.data.repository

import com.ticketvault.data.local.dao.TicketDao
import com.ticketvault.data.local.entity.TicketEntity
import com.ticketvault.data.local.entity.TicketStatus
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
        ticketDao.observeAllWithStoreName().map { rows ->
            rows.map { row ->
                Ticket(
                    id = row.ticket.id,
                    storeName = row.storeName,
                    date = row.ticket.date,
                    total = row.ticket.total,
                    status = row.ticket.status,
                )
            }
        }

    suspend fun saveScannedTicket(
        originalImagePath: String,
        cleanedImagePath: String,
        rawOcrText: String?,
        status: TicketStatus,
        storeId: Long?,
    ): Long = ticketDao.insert(
        TicketEntity(
            storeId = storeId,
            capturedAt = System.currentTimeMillis(),
            date = null,
            time = null,
            total = null,
            originalImageUri = originalImagePath,
            cleanedImageUri = cleanedImagePath,
            rawOcrText = rawOcrText,
            status = status,
        ),
    )

    suspend fun getRawOcrText(ticketId: Long): String? = ticketDao.getById(ticketId)?.rawOcrText

    suspend fun updateStore(ticketId: Long, storeId: Long) = ticketDao.updateStore(ticketId, storeId)
}
