package com.ticketvault.ocr

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrReadabilityEvaluatorTest {

    private val evaluator = OcrReadabilityEvaluator()

    @Test
    fun isReadable_withTypicalTicket_returnsTrue() {
        val text = """
            MERCADONA, S.A.
            C/ Mayor 12, Madrid
            1 LECHE ENTERA      0,95
            2 PAN BARRA         1,30
            TOTAL (€)           2,25
        """.trimIndent()

        assertTrue(evaluator.isReadable(text))
    }

    @Test
    fun isReadable_withBlankText_returnsFalse() {
        assertFalse(evaluator.isReadable("   \n  "))
    }

    @Test
    fun isReadable_withTooFewLines_returnsFalse() {
        assertFalse(evaluator.isReadable("MERCADONA SUPERMERCADO 12,50"))
    }

    @Test
    fun isReadable_withoutAnyPrice_returnsFalse() {
        val text = """
            MERCADONA S.A.
            GRACIAS POR SU VISITA
            VUELVA PRONTO
        """.trimIndent()

        assertFalse(evaluator.isReadable(text))
    }

    @Test
    fun isReadable_withGarbageCharacters_returnsFalse() {
        val text = """
            ~ ' . ,
            | - _ 1,00
            . . : ;
        """.trimIndent()

        assertFalse(evaluator.isReadable(text))
    }
}
