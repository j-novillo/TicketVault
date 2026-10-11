package com.ticketvault.parsing

import com.ticketvault.parsing.store.StoreTextNormalizer
import javax.inject.Inject

/**
 * Motor de parsing genérico: localiza el bloque de artículos (tras la cabecera de
 * columnas y hasta la línea del total), interpreta cada línea como artículo,
 * detalle de unidades/peso o nombre partido, y extrae fecha, hora y total.
 * La [ParsingTemplate] del comercio añade reglas propias de su formato.
 */
class RuleBasedTicketParser @Inject constructor(
    private val dateTimeExtractor: TicketDateTimeExtractor,
) : TicketParser {

    override fun parse(rawOcrText: String, template: ParsingTemplate): ParsedTicket {
        val lines = rawOcrText.lineSequence()
            .map { it.trim().replace(WHITESPACE, " ") }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.isEmpty()) return ParsedTicket.EMPTY

        val normalizedLines = lines.map(StoreTextNormalizer::normalize)
        val rules = TemplateRules(template)

        val headerIndex = normalizedLines.indexOfFirst(rules::isItemsStart)
        val itemsStart = if (headerIndex < 0) 0 else headerIndex + 1
        val totalIndex = (itemsStart until lines.size).firstOrNull { rules.isTotal(normalizedLines[it]) }

        val collector = ItemCollector(TicketLineParser(template.leadingQuantity))
        for (index in itemsStart until (totalIndex ?: lines.size)) {
            if (rules.isIgnored(lines[index], normalizedLines[index])) {
                collector.skip()
            } else {
                collector.accept(lines[index])
            }
        }

        val dateTime = dateTimeExtractor.extract(lines)
        return ParsedTicket(
            date = dateTime.date,
            time = dateTime.time,
            items = collector.items,
            total = totalIndex?.let { totalAmount(lines, it) },
        )
    }

    /** El importe puede quedar en la línea siguiente si el OCR separa la columna de precios. */
    private fun totalAmount(lines: List<String>, totalIndex: Int): Double? =
        TicketAmounts.findAll(lines[totalIndex]).lastOrNull()
            ?: lines.drop(totalIndex + 1)
                .take(TOTAL_LOOKAHEAD_LINES)
                .firstNotNullOfOrNull { TicketAmounts.findAll(it).firstOrNull() }

    private class TemplateRules(template: ParsingTemplate) {
        private val itemsStart = (template.itemsStartPatterns + GENERIC_ITEMS_START).map(::Regex)
        private val total = (template.totalPatterns + GENERIC_TOTAL).map(::Regex)
        private val ignored = (template.ignorePatterns + GENERIC_IGNORED + TOTAL_SUMMARY).map(::Regex)
        private val totalSummary = TOTAL_SUMMARY.map(::Regex)

        fun isItemsStart(normalized: String) = itemsStart.any { it.containsMatchIn(normalized) }

        fun isTotal(normalized: String) =
            total.any { it.containsMatchIn(normalized) } && totalSummary.none { it.containsMatchIn(normalized) }

        fun isIgnored(line: String, normalized: String) =
            ignored.any { it.containsMatchIn(normalized) } || TAX_BREAKDOWN.containsMatchIn(line)
    }

    private class ItemCollector(private val lineParser: TicketLineParser) {
        val items = mutableListOf<ParsedTicketItem>()
        private var pendingName: NameLine? = null
        private var pendingDetail: QuantityDetail? = null

        fun skip() {
            pendingName = null
            pendingDetail = null
        }

        /** Un nombre o detalle pendiente solo se combina con la línea inmediatamente siguiente. */
        fun accept(line: String) {
            val name = pendingName
            val detail = pendingDetail
            skip()

            lineParser.parseDetail(line)?.let {
                onDetail(it, name)
                return
            }
            lineParser.parseItem(line)?.let {
                onItem(it, detail)
                return
            }
            lineParser.parsePriceOnly(line)?.let { price ->
                if (name != null) add(name.name, name.quantity, unitPrice = null, totalPrice = price)
                return
            }
            pendingName = lineParser.parseNameOnly(line)
        }

        private fun onDetail(detail: QuantityDetail, name: NameLine?) {
            val expectedTotal = detail.totalPrice ?: TicketAmounts.round2(detail.quantity * detail.unitPrice)
            if (name != null) {
                add(name.name, detail.quantity, detail.unitPrice, expectedTotal)
                return
            }
            val lastIndex = items.lastIndex
            val last = items.lastOrNull()
            when {
                last != null && TicketAmounts.approxEquals(last.totalPrice, expectedTotal) ->
                    items[lastIndex] = last.copy(quantity = detail.quantity, unitPrice = detail.unitPrice)
                detail.totalPrice == null -> pendingDetail = detail
            }
        }

        private fun onItem(item: ItemLine, detail: QuantityDetail?) {
            val fromDetail = detail?.takeIf {
                item.quantity == null &&
                    item.unitPrice == null &&
                    TicketAmounts.approxEquals(item.totalPrice, it.quantity * it.unitPrice)
            }
            add(
                name = item.name,
                quantity = fromDetail?.quantity ?: item.quantity,
                unitPrice = fromDetail?.unitPrice ?: item.unitPrice,
                totalPrice = item.totalPrice,
            )
        }

        private fun add(name: String, quantity: Double?, unitPrice: Double?, totalPrice: Double) {
            val resolvedQuantity = when {
                quantity != null -> quantity
                unitPrice != null && unitPrice != 0.0 -> quantityFrom(totalPrice / unitPrice)
                else -> 1.0
            }
            val resolvedUnitPrice = unitPrice ?: TicketAmounts.round2(totalPrice / resolvedQuantity)
            items += ParsedTicketItem(
                name = name,
                quantity = resolvedQuantity,
                unitPrice = resolvedUnitPrice,
                totalPrice = TicketAmounts.round2(totalPrice),
            )
        }

        private fun quantityFrom(ratio: Double): Double {
            val rounded = Math.round(ratio).toDouble()
            return if (rounded > 0 && TicketAmounts.approxEquals(ratio, rounded)) rounded else TicketAmounts.round3(ratio)
        }
    }

    private companion object {
        const val TOTAL_LOOKAHEAD_LINES = 2
        val WHITESPACE = Regex("""\s+""")

        /** Desglose de IVA: "10% 5,23 0,52" o "B 10% 5,23 0,52" (con código de tipo). */
        val TAX_BREAKDOWN = Regex("""^(?:[A-E]\s+)?\d{1,2}(?:[.,]\d{1,2})?\s*%""")

        val GENERIC_ITEMS_START = listOf(
            """^(UDS?|CANT|CANTIDAD|DESCRIPCION|ARTICULO|CONCEPTO|PRODUCTO)\b.*\b(IMPORTE|PRECIO|EUROS?|EUR|P UNIT|PVP)\b""",
        )
        val GENERIC_TOTAL = listOf(
            """^TOTAL\b""",
            """\bTOTAL A PAGAR\b""",
            """\bIMPORTE TOTAL\b""",
            """^(IMPORTE )?A PAGAR\b""",
        )
        val TOTAL_SUMMARY = listOf(
            """^TOTAL (ARTICULOS|ARTS|UNIDADES|UDS|PRODUCTOS|PIEZAS|AHORRO|DESCUENTOS?|IVA|PUNTOS)\b""",
        )
        val GENERIC_IGNORED = listOf(
            """\bI ?V ?A\b""",
            """\bBASE IMP""",
            """\bCUOTA\b""",
            """\bSUBTOTAL\b""",
            """\bTARJETA\b""",
            """\bEFECTIVO\b""",
            """\bENTREGAD[OA]\b""",
            """\bCAMBIO\b""",
            """\b(VISA|MASTERCARD|CONTACTLESS)\b""",
            """\bPUNTOS\b""",
            """\bSALDO\b""",
            """\b(N|NUM|NUMERO) (DE )?ARTICULOS\b""",
        )
    }
}
