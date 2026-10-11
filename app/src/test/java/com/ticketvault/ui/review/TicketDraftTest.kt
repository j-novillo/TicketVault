package com.ticketvault.ui.review

import com.ticketvault.classification.ProductCategory
import com.ticketvault.domain.model.ReviewedTicket
import com.ticketvault.domain.model.ReviewedTicketItem
import com.ticketvault.parsing.ParsedTicket
import com.ticketvault.parsing.ParsedTicketItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class TicketDraftTest {

    @Test
    fun from_formatsParsedDataInSpanishFormat() {
        var key = 0L
        val parsed = ParsedTicket(
            date = LocalDate.of(2026, 10, 11),
            time = LocalTime.of(9, 5),
            items = listOf(ParsedTicketItem("PLATANO", quantity = 1.234, unitPrice = 1.99, totalPrice = 2.46)),
            total = 12.5,
        )

        val draft = TicketDraft.from(parsed) { key++ }

        assertEquals("11/10/2026", draft.date)
        assertEquals("09:05", draft.time)
        assertEquals("12,50", draft.total)
        assertEquals(
            listOf(TicketItemDraft(key = 0L, name = "PLATANO", quantity = "1,234", unitPrice = "1,99", totalPrice = "2,46")),
            draft.items,
        )
    }

    @Test
    fun toReviewedTicket_parsesFieldsAndDropsBlankLines() {
        val draft = TicketDraft(
            date = "1/3/2026",
            time = "8:30",
            total = "3.40",
            items = listOf(
                TicketItemDraft(key = 0L, name = " PAN ", quantity = "", unitPrice = "", totalPrice = "1,20"),
                TicketItemDraft(key = 1L, name = "LECHE", quantity = "2", unitPrice = "1,10", totalPrice = "2,20"),
                TicketItemDraft(key = 2L),
            ),
        )

        assertEquals(
            ReviewedTicket(
                date = LocalDate.of(2026, 3, 1),
                time = LocalTime.of(8, 30),
                total = 3.40,
                items = listOf(
                    ReviewedTicketItem("PAN", quantity = 1.0, unitPrice = null, totalPrice = 1.20),
                    ReviewedTicketItem("LECHE", quantity = 2.0, unitPrice = 1.10, totalPrice = 2.20),
                ),
            ),
            draft.toReviewedTicket(),
        )
    }

    @Test
    fun withSuggestedCategories_keepsManualCorrections() {
        val fruta = ProductCategory("Comida", "Fruta")
        val higiene = ProductCategory("No comestible", "Higiene personal")
        val draft = TicketDraft(
            date = "",
            time = "",
            total = "",
            items = listOf(
                TicketItemDraft(key = 0L, name = "PLATANO"),
                TicketItemDraft(key = 1L, name = "LECHE LIMPIADORA", category = higiene, isCategoryCorrected = true),
            ),
        )

        val classified = draft.withSuggestedCategories(listOf(fruta, ProductCategory("Comida", "Lácteos")))

        assertEquals(listOf(fruta, higiene), classified.items.map { it.category })
    }

    @Test
    fun toReviewedTicket_keepsCategoriesAndCorrections() {
        val higiene = ProductCategory("No comestible", "Higiene personal")
        val item = TicketItemDraft(
            key = 0L,
            name = "LECHE LIMPIADORA",
            totalPrice = "2,00",
            category = higiene,
            isCategoryCorrected = true,
        )

        val reviewedItem = TicketDraft(date = "", time = "", total = "", items = listOf(item))
            .toReviewedTicket()?.items?.single()

        assertEquals(
            ReviewedTicketItem("LECHE LIMPIADORA", 1.0, null, 2.0, category = higiene, isCategoryCorrected = true),
            reviewedItem,
        )
    }

    @Test
    fun toReviewedTicket_withEmptyOptionalFields_keepsThemNull() {
        val reviewed = TicketDraft(date = "", time = "", total = "", items = emptyList()).toReviewedTicket()

        assertEquals(ReviewedTicket(date = null, time = null, total = null, items = emptyList()), reviewed)
    }

    @Test
    fun toReviewedTicket_withInvalidFields_returnsNullAndFlagsThem() {
        val item = TicketItemDraft(key = 0L, name = "", quantity = "0", unitPrice = "abc", totalPrice = "")
        val draft = TicketDraft(date = "31/02/2026", time = "25:00", total = "12,3,4", items = listOf(item))

        assertNull(draft.toReviewedTicket())
        assertTrue(draft.dateError && draft.timeError && draft.totalError)
        assertTrue(item.nameError && item.quantityError && item.unitPriceError && item.totalPriceError)
    }
}
