package com.ticketvault.ocr

import org.junit.Assert.assertEquals
import org.junit.Test

class OcrLineAssemblerTest {

    @Test
    fun assemble_joinsColumnsOfTheSameRowLeftToRight() {
        val lines = listOf(
            OcrTextLine("LECHE ENTERA", left = 10, top = 100, right = 200, bottom = 130),
            OcrTextLine("PAN", left = 10, top = 140, right = 60, bottom = 170),
            OcrTextLine("MERCADONA", left = 100, top = 20, right = 300, bottom = 60),
            OcrTextLine("0,95", left = 400, top = 98, right = 460, bottom = 128),
            OcrTextLine("1,20", left = 400, top = 143, right = 460, bottom = 171),
        )

        assertEquals("MERCADONA\nLECHE ENTERA 0,95\nPAN 1,20", OcrLineAssembler.assemble(lines))
    }

    @Test
    fun assemble_ignoresBlankLines() {
        val lines = listOf(
            OcrTextLine(" ", left = 0, top = 0, right = 10, bottom = 10),
            OcrTextLine("TOTAL", left = 0, top = 50, right = 100, bottom = 80),
        )

        assertEquals("TOTAL", OcrLineAssembler.assemble(lines))
    }
}
