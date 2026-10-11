package com.ticketvault.domain.model

import com.ticketvault.classification.ProductCategory
import com.ticketvault.data.local.entity.TicketStatus
import java.time.LocalDate
import java.time.LocalTime

data class Ticket(
    val id: Long = 0,
    val storeName: String?,
    val date: String?,
    val total: Double?,
    val status: TicketStatus,
)

/** Ticket ya escaneado, con lo necesario para extraer y revisar sus datos. */
data class CapturedTicket(
    val id: Long,
    val storeId: Long?,
    val rawOcrText: String?,
    val cleanedImagePath: String?,
    val status: TicketStatus,
)

/** Datos del ticket confirmados por el usuario en la pantalla de revisión. */
data class ReviewedTicket(
    val date: LocalDate?,
    val time: LocalTime?,
    val total: Double?,
    val items: List<ReviewedTicketItem>,
)

data class ReviewedTicketItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double?,
    val totalPrice: Double,
    val category: ProductCategory? = null,
    /** El usuario eligió la categoría a mano; se aprende para próximas clasificaciones. */
    val isCategoryCorrected: Boolean = false,
)
