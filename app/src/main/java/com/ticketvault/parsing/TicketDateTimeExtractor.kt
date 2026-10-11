package com.ticketvault.parsing

import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class TicketDateTime(
    val date: LocalDate?,
    val time: LocalTime?,
)

/**
 * Busca la primera fecha válida (dd/mm/aaaa, dd-mm-aa, dd.mm.aaaa) y la hora (hh:mm[:ss]),
 * priorizando la hora impresa en la misma línea que la fecha.
 */
class TicketDateTimeExtractor @Inject constructor() {

    fun extract(lines: List<String>): TicketDateTime {
        val dateLineIndex = lines.indexOfFirst { findDate(it) != null }
        val date = lines.getOrNull(dateLineIndex)?.let(::findDate)
        val time = lines.getOrNull(dateLineIndex)?.let(::findTime) ?: lines.firstNotNullOfOrNull(::findTime)
        return TicketDateTime(date = date, time = time)
    }

    private fun findDate(line: String): LocalDate? =
        DATE.findAll(line).firstNotNullOfOrNull { match ->
            val (day, month, year) = match.destructured
            val fullYear = if (year.length == 2) 2000 + year.toInt() else year.toInt()
            if (fullYear !in MIN_YEAR..MAX_YEAR) return@firstNotNullOfOrNull null
            try {
                LocalDate.of(fullYear, month.toInt(), day.toInt())
            } catch (e: DateTimeException) {
                null
            }
        }

    private fun findTime(line: String): LocalTime? =
        TIME.find(line)?.let { match ->
            LocalTime.of(match.groupValues[1].toInt(), match.groupValues[2].toInt())
        }

    private companion object {
        const val MIN_YEAR = 2000
        const val MAX_YEAR = 2099
        val DATE = Regex("""(?<![\d/.-])(\d{1,2})[/.-](\d{1,2})[/.-](\d{4}|\d{2})(?!\d)""")
        val TIME = Regex("""(?<![\d:])([01]?\d|2[0-3]):([0-5]\d)(?::[0-5]\d)?(?![\d:])""")
    }
}
