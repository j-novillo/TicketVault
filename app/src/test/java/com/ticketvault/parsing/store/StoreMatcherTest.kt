package com.ticketvault.parsing.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoreMatcherTest {

    private val matcher = StoreMatcher()
    private val candidates = listOf(
        StoreCandidate(storeId = 1L, patterns = listOf("MERCADONA")),
        StoreCandidate(storeId = 2L, patterns = listOf("CARREFOUR")),
        StoreCandidate(storeId = 3L, patterns = listOf("DIA RETAIL", "SUPERMERCADOS DIA")),
        StoreCandidate(storeId = 4L, patterns = listOf("ALDI")),
        StoreCandidate(storeId = 5L, patterns = listOf("El Corte Inglés")),
    )

    @Test
    fun match_findsStoreInHeaderIgnoringCaseAndPunctuation() {
        val text = """
            Mercadona, S.A.  A-46103834
            C/ Mayor 12, Valencia
            1 LECHE ENTERA 0,95
        """.trimIndent()

        assertEquals(1L, matcher.match(text, candidates)?.storeId)
    }

    @Test
    fun match_ignoresAccentsInPatternsAndText() {
        val text = "EL CORTE INGLÉS, S.A.\nHipercor Campo de las Naciones"

        assertEquals(5L, matcher.match(text, candidates)?.storeId)
    }

    @Test
    fun match_toleratesWordsSplitByOcrForLongPatterns() {
        assertEquals(1L, matcher.match("MERCA DONA S A\nTOTAL 3,20", candidates)?.storeId)
    }

    @Test
    fun match_requiresWordBoundariesForShortPatterns() {
        val text = "CALDO DE POLLO 1,20\nESCALDIA 2,00"

        assertNull(matcher.match(text, candidates))
    }

    @Test
    fun match_prefersEarliestHeaderLine() {
        val text = """
            CARREFOUR EXPRESS
            Producto MERCADONA
        """.trimIndent()

        assertEquals(2L, matcher.match(text, candidates)?.storeId)
    }

    @Test
    fun match_ignoresStoresMentionedOutsideTheHeader() {
        val header = (1..StoreMatcher.HEADER_LINES).joinToString("\n") { "LINEA $it 1,00" }
        val text = "$header\nGRACIAS POR COMPRAR EN MERCADONA"

        assertNull(matcher.match(text, candidates))
    }

    @Test
    fun match_withMultiWordPattern() {
        assertEquals(3L, matcher.match("DIA RETAIL ESPAÑA, S.A.U.\nTOTAL 5,00", candidates)?.storeId)
    }

    @Test
    fun match_returnsNullForUnknownStore() {
        assertNull(matcher.match("FRUTERIA PEPE\nPLATANOS 2,10", candidates))
    }

    @Test
    fun match_returnsNullForBlankText() {
        assertNull(matcher.match("   \n  ", candidates))
    }
}
