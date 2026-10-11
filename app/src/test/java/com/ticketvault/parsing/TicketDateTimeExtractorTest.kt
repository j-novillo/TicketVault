package com.ticketvault.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TicketDateTimeExtractorTest {

    private val extractor = TicketDateTimeExtractor()

    @Test
    fun extract_prefersTimeOnTheDateLine() {
        val result = extractor.extract(listOf("CAJA 3 10:01", "15-08-25 20:47:13"))

        assertEquals(LocalDate.of(2025, 8, 15), result.date)
        assertEquals(LocalTime.of(20, 47), result.time)
    }

    @Test
    fun extract_skipsInvalidDates() {
        val result = extractor.extract(listOf("31/02/2026", "REF 2345-012-345678", "01/03/2026"))

        assertEquals(LocalDate.of(2026, 3, 1), result.date)
    }

    @Test
    fun extract_withoutDate_returnsFirstTime() {
        val result = extractor.extract(listOf("LECHE 0,95", "HORA 10:01"))

        assertNull(result.date)
        assertEquals(LocalTime.of(10, 1), result.time)
    }

    @Test
    fun extract_ignoresPricesAndPhoneNumbers() {
        val result = extractor.extract(listOf("TEL 963-12-34-56", "TOTAL 12.34"))

        assertNull(result.date)
        assertNull(result.time)
    }
}
