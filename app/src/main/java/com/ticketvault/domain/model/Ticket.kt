package com.ticketvault.domain.model

import com.ticketvault.data.local.entity.TicketStatus

data class Ticket(
    val id: Long = 0,
    val storeName: String?,
    val date: String?,
    val total: Double?,
    val status: TicketStatus,
)
