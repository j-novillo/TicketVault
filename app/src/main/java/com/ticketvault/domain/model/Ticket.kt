package com.ticketvault.domain.model

data class Ticket(
    val id: Long = 0,
    val storeName: String?,
    val date: String,
    val total: Double,
)
