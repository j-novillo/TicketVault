package com.ticketvault.parsing

/**
 * Motor de reglas y plantillas por comercio.
 * Implementación pendiente (HU-2.2, HU-2.3).
 */
data class ParsedTicket(
    val storeName: String?,
    val date: String?,
    val items: List<ParsedTicketItem>,
    val total: Double?,
)

data class ParsedTicketItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double?,
    val totalPrice: Double,
)

interface TicketParser {
    fun parse(rawOcrText: String): ParsedTicket
}
