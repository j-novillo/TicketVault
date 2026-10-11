package com.ticketvault.parsing.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HeaderSignatureExtractorTest {

    private val extractor = HeaderSignatureExtractor()

    @Test
    fun extract_returnsFirstDistinctiveHeaderLineNormalized() {
        val text = """
            Fruteria Pepe, S.L.
            C/ Mayor 12
            PLATANOS 2,10
        """.trimIndent()

        assertEquals("FRUTERIA PEPE S L", extractor.extract(text))
    }

    @Test
    fun extract_skipsGenericLinesAddressesAndNumbers() {
        val text = """
            FACTURA SIMPLIFICADA
            12/03/2026 10:15
            C/ Mayor 12
            CIF B12345678
            Supermercados Los Robles
        """.trimIndent()

        assertEquals("SUPERMERCADOS LOS ROBLES", extractor.extract(text))
    }

    @Test
    fun extract_returnsNullWhenNothingDistinctive() {
        assertNull(extractor.extract("12/03/2026\n0,95\nTICKET"))
    }
}
