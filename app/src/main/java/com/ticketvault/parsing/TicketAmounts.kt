package com.ticketvault.parsing

import kotlin.math.abs
import kotlin.math.roundToLong

internal object TicketAmounts {

    /**
     * Importe con dos decimales y signo opcional ("-0,30" o "0,30-"). Los límites
     * descartan trozos de fechas ("11.10.2026") y pesos con tres decimales ("1,234").
     */
    const val PATTERN = """(?<![\d.,])-?\d{1,4}[.,]\d{2}-?(?!\d|[.,]\d)"""

    private val REGEX = Regex(PATTERN)
    private const val TOLERANCE = 0.02

    fun findAll(text: String): List<Double> = REGEX.findAll(text).map { parse(it.value) }.toList()

    fun parse(token: String): Double {
        val negative = token.startsWith('-') || token.endsWith('-')
        val value = token.trim('-').replace(',', '.').toDouble()
        return if (negative) -value else value
    }

    fun round2(value: Double): Double = (value * 100).roundToLong() / 100.0

    fun round3(value: Double): Double = (value * 1000).roundToLong() / 1000.0

    fun approxEquals(a: Double, b: Double): Boolean = abs(a - b) <= TOLERANCE
}
