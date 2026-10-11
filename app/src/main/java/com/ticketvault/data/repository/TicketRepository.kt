package com.ticketvault.data.repository

import com.ticketvault.data.local.dao.TicketDao
import com.ticketvault.data.local.entity.TicketEntity
import com.ticketvault.data.local.entity.TicketItemEntity
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.domain.model.CapturedTicket
import com.ticketvault.domain.model.ReviewedTicket
import com.ticketvault.domain.model.Ticket
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.format.DateTimeFormatter
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

    suspend fun getCapturedTicket(ticketId: Long): CapturedTicket? =
        ticketDao.getById(ticketId)?.let { ticket ->
            CapturedTicket(
                id = ticket.id,
                storeId = ticket.storeId,
                rawOcrText = ticket.rawOcrText,
                cleanedImagePath = ticket.cleanedImageUri,
                status = ticket.status,
            )
        }

    suspend fun updateStore(ticketId: Long, storeId: Long) = ticketDao.updateStore(ticketId, storeId)

    /** Fecha en ISO (`yyyy-MM-dd`) y hora `HH:mm`, para poder agregarlas con SQL en las métricas. */
    suspend fun saveReviewedTicket(ticketId: Long, reviewed: ReviewedTicket) {
        val ticket = checkNotNull(ticketDao.getById(ticketId)) { "Ticket $ticketId not found" }
        ticketDao.replaceTicketData(
            ticket = ticket.copy(
                date = reviewed.date?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                time = reviewed.time?.format(TIME_FORMAT),
                total = reviewed.total,
                status = TicketStatus.REVIEWED,
            ),
            items = reviewed.items.map { item ->
                TicketItemEntity(
                    ticketId = ticketId,
                    productName = item.name,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    totalPrice = item.totalPrice,
                    category = item.category?.category,
                    subcategory = item.category?.subcategory,
                )
            },
        )
    }

    private companion object {
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}
