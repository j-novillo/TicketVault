package com.ticketvault.parsing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class RuleBasedTicketParserTest {

    private val parser = RuleBasedTicketParser(TicketDateTimeExtractor())

    private val mercadona = ParsingTemplate(
        id = "mercadona",
        leadingQuantity = true,
        itemsStartPatterns = listOf("""^DESCRIPCION\b"""),
    )
    private val lidl = ParsingTemplate(
        id = "lidl",
        leadingQuantity = false,
        itemsStartPatterns = listOf("""^EUR$"""),
    )

    @Test
    fun parse_mercadonaTicket_extractsItemsWithQuantitiesWeightsAndTotal() {
        val text = """
            MERCADONA, S.A. A-46103834
            C/ MAYOR 12 46001 VALENCIA
            TELEFONO: 963 123 456
            11/10/2026 14:32 OP: 123456
            FACTURA SIMPLIFICADA: 2345-012-345678
            Descripción P. Unit Importe
            1 LECHE ENTERA 0,95
            2 YOGUR GRIEGO 0,60 1,20
            1 PLATANO
            1,234 kg 1,99 €/kg 2,46
            3 AGUA MINERAL 1,5L 0,35 1,05
            TOTAL (€) 5,66
            TARJETA BANCARIA 5,66
            IVA BASE IMPONIBLE (€) CUOTA (€)
            10% 5,15 0,51
        """.trimIndent()

        val result = parser.parse(text, mercadona)

        assertEquals(
            listOf(
                ParsedTicketItem("LECHE ENTERA", quantity = 1.0, unitPrice = 0.95, totalPrice = 0.95),
                ParsedTicketItem("YOGUR GRIEGO", quantity = 2.0, unitPrice = 0.60, totalPrice = 1.20),
                ParsedTicketItem("PLATANO", quantity = 1.234, unitPrice = 1.99, totalPrice = 2.46),
                ParsedTicketItem("AGUA MINERAL 1,5L", quantity = 3.0, unitPrice = 0.35, totalPrice = 1.05),
            ),
            result.items,
        )
        assertEquals(5.66, result.total!!, 0.001)
        assertEquals(LocalDate.of(2026, 10, 11), result.date)
        assertEquals(LocalTime.of(14, 32), result.time)
    }

    @Test
    fun parse_lidlTicket_appliesQuantityLineToPreviousItemAndKeepsDiscounts() {
        val text = """
            LIDL SUPERMERCADOS S.A.U.
            C/ ALCALA 100 MADRID
            EUR
            Leche semidesnatada 0,79 A
            Yogur natural 1,56 A
            2 x 0,78
            4 quesos pizza 2,99 B
            Descuento Lidl Plus -0,30 A
            Total 5,04
            Entregado 10,00
            Cambio 4,96
            11.10.26 18:05
        """.trimIndent()

        val result = parser.parse(text, lidl)

        assertEquals(
            listOf(
                ParsedTicketItem("Leche semidesnatada", quantity = 1.0, unitPrice = 0.79, totalPrice = 0.79),
                ParsedTicketItem("Yogur natural", quantity = 2.0, unitPrice = 0.78, totalPrice = 1.56),
                ParsedTicketItem("4 quesos pizza", quantity = 1.0, unitPrice = 2.99, totalPrice = 2.99),
                ParsedTicketItem("Descuento Lidl Plus", quantity = 1.0, unitPrice = -0.30, totalPrice = -0.30),
            ),
            result.items,
        )
        assertEquals(5.04, result.total!!, 0.001)
        assertEquals(LocalDate.of(2026, 10, 11), result.date)
        assertEquals(LocalTime.of(18, 5), result.time)
    }

    @Test
    fun parse_genericTicket_joinsSplitLinesAndQuantityLineBeforeItem() {
        val text = """
            FRUTERIA PEPE
            NIF 12345678Z
            Fecha: 05/03/2026 Hora: 09:15
            MANZANA GOLDEN
            2,40
            3 x 0,50
            PAN INTEGRAL 1,50
            TOTAL A PAGAR
            3,90
        """.trimIndent()

        val result = parser.parse(text, ParsingTemplate.GENERIC)

        assertEquals(
            listOf(
                ParsedTicketItem("MANZANA GOLDEN", quantity = 1.0, unitPrice = 2.40, totalPrice = 2.40),
                ParsedTicketItem("PAN INTEGRAL", quantity = 3.0, unitPrice = 0.50, totalPrice = 1.50),
            ),
            result.items,
        )
        assertEquals(3.90, result.total!!, 0.001)
        assertEquals(LocalDate.of(2026, 3, 5), result.date)
        assertEquals(LocalTime.of(9, 15), result.time)
    }

    @Test
    fun parse_skipsSummaryTaxAndPaymentLines() {
        val text = """
            CONSUM
            ARROZ 1,10
            YOGUR 0% M.G. 1,20
            ACEITE OLIVA 4,90
            TOTAL ARTICULOS: 3
            B 10% 6,55 0,65
            SUBTOTAL 7,20
            TOTAL 7,20
            EFECTIVO 10,00
        """.trimIndent()

        val result = parser.parse(text, ParsingTemplate.GENERIC)

        assertEquals(listOf("ARROZ", "YOGUR 0% M.G", "ACEITE OLIVA"), result.items.map { it.name })
        assertEquals(7.20, result.total!!, 0.001)
    }

    @Test
    fun parse_inlineQuantityAndUnitPrice() {
        val text = """
            PAN BARRA 3 X 0,45 1,35
            TOMATE 0,856 KG X 2,10 1,80
            TOTAL 3,15
        """.trimIndent()

        val result = parser.parse(text, ParsingTemplate.GENERIC)

        assertEquals(
            listOf(
                ParsedTicketItem("PAN BARRA", quantity = 3.0, unitPrice = 0.45, totalPrice = 1.35),
                ParsedTicketItem("TOMATE", quantity = 0.856, unitPrice = 2.10, totalPrice = 1.80),
            ),
            result.items,
        )
    }

    @Test
    fun parse_withoutTotalLine_returnsItemsAndNullTotal() {
        val result = parser.parse("CAFE MOLIDO 3,25\nAZUCAR 1,10", ParsingTemplate.GENERIC)

        assertEquals(listOf("CAFE MOLIDO", "AZUCAR"), result.items.map { it.name })
        assertNull(result.total)
        assertNull(result.date)
    }

    @Test
    fun parse_blankText_returnsEmptyTicket() {
        assertEquals(ParsedTicket.EMPTY, parser.parse("  \n ", ParsingTemplate.GENERIC))
    }
}
