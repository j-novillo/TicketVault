package com.ticketvault.ui.review

import com.ticketvault.classification.ProductCategory
import com.ticketvault.domain.model.ReviewedTicket
import com.ticketvault.domain.model.ReviewedTicketItem
import com.ticketvault.parsing.ParsedTicket
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale

/** Línea de artículo editable; los campos son texto para no perder lo que escribe el usuario. */
data class TicketItemDraft(
    val key: Long,
    val name: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val totalPrice: String = "",
    val category: ProductCategory? = null,
    /** Elegida a mano: ya no se reclasifica al editar el nombre. */
    val isCategoryCorrected: Boolean = false,
) {
    val isBlank: Boolean get() = name.isBlank() && unitPrice.isBlank() && totalPrice.isBlank()
    val nameError: Boolean get() = !isBlank && name.isBlank()
    val quantityError: Boolean get() = !isBlank && quantity.isNotBlank() && TicketDraftFormat.parseQuantity(quantity) == null
    val unitPriceError: Boolean get() = unitPrice.isNotBlank() && TicketDraftFormat.parseAmount(unitPrice) == null
    val totalPriceError: Boolean get() = !isBlank && TicketDraftFormat.parseAmount(totalPrice) == null
    val hasErrors: Boolean get() = nameError || quantityError || unitPriceError || totalPriceError
}

data class TicketDraft(
    val date: String,
    val time: String,
    val total: String,
    val items: List<TicketItemDraft>,
) {
    val dateError: Boolean get() = date.isNotBlank() && TicketDraftFormat.parseDate(date) == null
    val timeError: Boolean get() = time.isNotBlank() && TicketDraftFormat.parseTime(time) == null
    val totalError: Boolean get() = total.isNotBlank() && TicketDraftFormat.parseAmount(total) == null
    val hasErrors: Boolean get() = dateError || timeError || totalError || items.any { it.hasErrors }

    /** Devuelve null si hay campos inválidos. Las líneas vacías se descartan. */
    fun toReviewedTicket(): ReviewedTicket? {
        if (hasErrors) return null
        return ReviewedTicket(
            date = TicketDraftFormat.parseDate(date),
            time = TicketDraftFormat.parseTime(time),
            total = TicketDraftFormat.parseAmount(total),
            items = items.filterNot { it.isBlank }.map { item ->
                ReviewedTicketItem(
                    name = item.name.trim(),
                    quantity = TicketDraftFormat.parseQuantity(item.quantity) ?: 1.0,
                    unitPrice = TicketDraftFormat.parseAmount(item.unitPrice),
                    totalPrice = checkNotNull(TicketDraftFormat.parseAmount(item.totalPrice)),
                    category = item.category,
                    isCategoryCorrected = item.isCategoryCorrected,
                )
            },
        )
    }

    /** Aplica la clasificación automática (una por artículo), respetando las categorías elegidas a mano. */
    fun withSuggestedCategories(categories: List<ProductCategory?>): TicketDraft = copy(
        items = items.mapIndexed { index, item ->
            if (item.isCategoryCorrected) item else item.copy(category = categories.getOrNull(index))
        },
    )

    companion object {
        fun from(parsed: ParsedTicket, nextKey: () -> Long) = TicketDraft(
            date = parsed.date?.let(TicketDraftFormat::formatDate).orEmpty(),
            time = parsed.time?.let(TicketDraftFormat::formatTime).orEmpty(),
            total = parsed.total?.let(TicketDraftFormat::formatAmount).orEmpty(),
            items = parsed.items.map { item ->
                TicketItemDraft(
                    key = nextKey(),
                    name = item.name,
                    quantity = TicketDraftFormat.formatQuantity(item.quantity),
                    unitPrice = item.unitPrice?.let(TicketDraftFormat::formatAmount).orEmpty(),
                    totalPrice = TicketDraftFormat.formatAmount(item.totalPrice),
                )
            },
        )
    }
}

/** Formato español: coma decimal, fecha dd/mm/aaaa y hora hh:mm. */
object TicketDraftFormat {
    private val SYMBOLS = DecimalFormatSymbols(Locale.forLanguageTag("es-ES"))
    private val DATE_OUTPUT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    private val DATE_INPUT = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val TIME_OUTPUT = DateTimeFormatter.ofPattern("HH:mm")
    private val TIME_INPUT = DateTimeFormatter.ofPattern("H:mm").withResolverStyle(ResolverStyle.STRICT)
    private val AMOUNT = Regex("""-?\d+([.,]\d{1,2})?""")
    private val QUANTITY = Regex("""\d+([.,]\d{1,3})?""")

    fun formatDate(date: LocalDate): String = date.format(DATE_OUTPUT)

    fun formatTime(time: LocalTime): String = time.format(TIME_OUTPUT)

    fun formatAmount(value: Double): String = DecimalFormat("0.00", SYMBOLS).format(value)

    fun formatQuantity(value: Double): String =
        DecimalFormat("0.###", SYMBOLS).apply { roundingMode = RoundingMode.HALF_UP }.format(value)

    fun parseDate(text: String): LocalDate? = parseOrNull { LocalDate.parse(text.trim(), DATE_INPUT) }

    fun parseTime(text: String): LocalTime? = parseOrNull { LocalTime.parse(text.trim(), TIME_INPUT) }

    fun parseAmount(text: String): Double? =
        text.trim().takeIf(AMOUNT::matches)?.replace(',', '.')?.toDouble()

    fun parseQuantity(text: String): Double? =
        text.trim().takeIf(QUANTITY::matches)?.replace(',', '.')?.toDouble()?.takeIf { it > 0 }

    private inline fun <T> parseOrNull(block: () -> T): T? =
        try {
            block()
        } catch (e: DateTimeParseException) {
            null
        }
}
