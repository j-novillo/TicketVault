package com.ticketvault.parsing

import java.time.LocalDate
import java.time.LocalTime

data class ParsedTicket(
    val date: LocalDate?,
    val time: LocalTime?,
    val items: List<ParsedTicketItem>,
    val total: Double?,
) {
    companion object {
        val EMPTY = ParsedTicket(date = null, time = null, items = emptyList(), total = null)
    }
}

data class ParsedTicketItem(
    val name: String,
    val quantity: Double,
    val unitPrice: Double?,
    val totalPrice: Double,
)

interface TicketParser {
    fun parse(rawOcrText: String, template: ParsingTemplate): ParsedTicket
}
