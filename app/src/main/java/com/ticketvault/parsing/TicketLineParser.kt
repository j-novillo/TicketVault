package com.ticketvault.parsing

import com.ticketvault.parsing.TicketAmounts.PATTERN as AMOUNT

/** Línea de artículo: "[uds] nombre [uds x precio | precio unitario] importe". */
internal data class ItemLine(
    val name: String,
    val quantity: Double?,
    val unitPrice: Double?,
    val totalPrice: Double,
)

/** Línea sin nombre con unidades o peso: "2 x 0,78", "1,234 kg 1,99 €/kg 2,46". */
internal data class QuantityDetail(
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double?,
)

/** Línea con solo el nombre; el importe llega en la línea siguiente (artículos al peso, OCR partido). */
internal data class NameLine(
    val name: String,
    val quantity: Double?,
)

internal class TicketLineParser(private val leadingQuantity: Boolean) {

    fun parseDetail(line: String): QuantityDetail? {
        DETAIL.matchEntire(line)?.let { match ->
            return QuantityDetail(
                quantity = parseQuantity(match.groupValues[1]) ?: return null,
                unitPrice = TicketAmounts.parse(match.groupValues[2]),
                totalPrice = match.groupValues[3].takeIf { it.isNotEmpty() }?.let(TicketAmounts::parse),
            )
        }
        REVERSED_DETAIL.matchEntire(line)?.let { match ->
            return QuantityDetail(
                quantity = parseQuantity(match.groupValues[2]) ?: return null,
                unitPrice = TicketAmounts.parse(match.groupValues[1]),
                totalPrice = null,
            )
        }
        return null
    }

    fun parseItem(line: String): ItemLine? {
        val totalMatch = TRAILING_TOTAL.find(line) ?: return null
        val totalPrice = TicketAmounts.parse(totalMatch.groupValues[1])
        var rest = line.substring(0, totalMatch.range.first)
        var quantity: Double? = null
        var unitPrice: Double? = null

        val inline = INLINE_QUANTITY.find(rest)
        if (inline != null) {
            quantity = parseQuantity(inline.groupValues[1])
            unitPrice = TicketAmounts.parse(inline.groupValues[2])
            rest = rest.substring(0, inline.range.first)
        } else {
            TRAILING_UNIT_PRICE.find(rest)?.let { match ->
                unitPrice = TicketAmounts.parse(match.groupValues[1])
                rest = rest.substring(0, match.range.first)
            }
        }

        val (leading, nameText) = splitLeadingQuantity(rest)
        if (quantity == null) quantity = leading
        val name = cleanName(nameText, MIN_ITEM_NAME_LETTERS) ?: return null
        return ItemLine(name = name, quantity = quantity, unitPrice = unitPrice, totalPrice = totalPrice)
    }

    fun parsePriceOnly(line: String): Double? {
        val amounts = TicketAmounts.findAll(line)
        if (amounts.isEmpty()) return null
        val residue = line.replace(AMOUNT_REGEX, "").replace(PRICE_DECORATION, "")
        return if (residue.isBlank()) amounts.last() else null
    }

    fun parseNameOnly(line: String): NameLine? {
        if (TicketAmounts.findAll(line).isNotEmpty()) return null
        val (quantity, nameText) = splitLeadingQuantity(line)
        val name = cleanName(nameText, MIN_PENDING_NAME_LETTERS) ?: return null
        return NameLine(name = name, quantity = quantity)
    }

    private fun splitLeadingQuantity(text: String): Pair<Double?, String> {
        if (!leadingQuantity) return null to text
        val match = LEADING_QUANTITY.find(text) ?: return null to text
        val quantity = match.groupValues[1].toDouble().takeIf { it > 0 } ?: return null to text
        return quantity to text.substring(match.range.last + 1)
    }

    private fun parseQuantity(token: String): Double? =
        token.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 }

    private fun cleanName(text: String, minLetters: Int): String? {
        val name = text
            .replace(LEADING_CODE, "")
            .trim(*NAME_TRIM_CHARS)
            .replace(WHITESPACE, " ")
        return name.takeIf { candidate -> candidate.count { it.isLetter() } >= minLetters }
    }

    private companion object {
        const val MIN_ITEM_NAME_LETTERS = 2
        const val MIN_PENDING_NAME_LETTERS = 3

        const val QUANTITY = """\d{1,3}(?:[.,]\d{1,3})?"""
        const val CURRENCY = """(?:\s*(?:€|EUR))?"""
        const val PER_UNIT = """(?:\s*/\s*(?:KGS?|UDS?|U|UNID))?"""
        const val TAX_CODE = """(?:\s*(?:[A-E]|\*{1,2}))?"""
        const val UNIT = """(?:KGS?|UDS?|UNIDADES|UNID|U)\.?"""
        const val SEPARATOR = """[X*@]"""
        const val QUANTITY_TIMES = """($QUANTITY)\s*(?:$UNIT\s*$SEPARATOR?|$SEPARATOR)\s*"""

        val DETAIL = Regex(
            """$QUANTITY_TIMES($AMOUNT)$CURRENCY$PER_UNIT(?:\s*=?\s*($AMOUNT)$CURRENCY$TAX_CODE)?\s*""",
            RegexOption.IGNORE_CASE,
        )
        val REVERSED_DETAIL = Regex(
            """($AMOUNT)$CURRENCY\s*$SEPARATOR\s*(\d{1,3})\s*(?:UDS?|U)?\.?\s*""",
            RegexOption.IGNORE_CASE,
        )
        val TRAILING_TOTAL = Regex("""\s*($AMOUNT)$CURRENCY$TAX_CODE\s*$""", RegexOption.IGNORE_CASE)
        val INLINE_QUANTITY = Regex(
            """(?:^|\s)$QUANTITY_TIMES($AMOUNT)$CURRENCY$PER_UNIT\s*$""",
            RegexOption.IGNORE_CASE,
        )
        val TRAILING_UNIT_PRICE = Regex("""\s($AMOUNT)$CURRENCY$PER_UNIT\s*$""", RegexOption.IGNORE_CASE)
        val LEADING_QUANTITY = Regex("""^\s*(\d{1,3})\s+(?=\p{L})""")
        val LEADING_CODE = Regex("""^\s*\d{5,}\s+""")
        val AMOUNT_REGEX = Regex(AMOUNT)
        val PRICE_DECORATION = Regex("""€|EUR|\b[A-E]\b|\*""", RegexOption.IGNORE_CASE)
        val WHITESPACE = Regex("""\s+""")
        val NAME_TRIM_CHARS = charArrayOf(' ', '.', ':', '-', '*', '=')
    }
}
