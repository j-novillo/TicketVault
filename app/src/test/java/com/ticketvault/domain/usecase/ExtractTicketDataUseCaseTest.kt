package com.ticketvault.domain.usecase

import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.ParsingTemplateRepository
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.CapturedTicket
import com.ticketvault.domain.model.Store
import com.ticketvault.parsing.ParsedTicket
import com.ticketvault.parsing.ParsedTicketItem
import com.ticketvault.parsing.ParsingTemplate
import com.ticketvault.parsing.TicketParser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtractTicketDataUseCaseTest {

    private val ticketRepository = mockk<TicketRepository>()
    private val storeRepository = mockk<StoreRepository>()
    private val templateRepository = mockk<ParsingTemplateRepository>()
    private val ticketParser = mockk<TicketParser>()
    private val useCase = ExtractTicketDataUseCase(
        ticketRepository = ticketRepository,
        storeRepository = storeRepository,
        templateRepository = templateRepository,
        ticketParser = ticketParser,
    )

    private val rawText = "MERCADONA\n1 LECHE 0,95\nTOTAL 0,95"
    private val mercadona = Store(id = 1L, name = "Mercadona")
    private val mercadonaTemplate = ParsingTemplate(id = "mercadona")
    private val parsed = ParsedTicket(
        date = null,
        time = null,
        items = listOf(ParsedTicketItem("LECHE", quantity = 1.0, unitPrice = 0.95, totalPrice = 0.95)),
        total = 0.95,
    )

    private fun capturedTicket(storeId: Long?, rawOcrText: String? = rawText) = CapturedTicket(
        id = 10L,
        storeId = storeId,
        rawOcrText = rawOcrText,
        cleanedImagePath = "/tickets/cleaned/a.jpg",
        status = TicketStatus.OCR_COMPLETED,
    )

    @Test
    fun invoke_parsesWithTheStoreTemplate() = runTest {
        coEvery { ticketRepository.getCapturedTicket(10L) } returns capturedTicket(storeId = 1L)
        coEvery { storeRepository.getStore(1L) } returns mercadona
        coEvery { storeRepository.getParsingTemplateId(1L) } returns "mercadona"
        coEvery { templateRepository.getTemplate("mercadona") } returns mercadonaTemplate
        every { ticketParser.parse(rawText, mercadonaTemplate) } returns parsed

        val result = useCase(10L).getOrThrow()

        assertEquals(mercadona, result.store)
        assertEquals(parsed, result.data)
    }

    @Test
    fun invoke_withoutStore_usesGenericTemplate() = runTest {
        coEvery { ticketRepository.getCapturedTicket(10L) } returns capturedTicket(storeId = null)
        coEvery { templateRepository.getTemplate(null) } returns ParsingTemplate.GENERIC
        every { ticketParser.parse(rawText, ParsingTemplate.GENERIC) } returns parsed

        assertEquals(parsed, useCase(10L).getOrThrow().data)
    }

    @Test
    fun invoke_whenTemplateLoadingFails_fallsBackToGenericTemplate() = runTest {
        coEvery { ticketRepository.getCapturedTicket(10L) } returns capturedTicket(storeId = 1L)
        coEvery { storeRepository.getStore(1L) } returns mercadona
        coEvery { storeRepository.getParsingTemplateId(1L) } returns "mercadona"
        coEvery { templateRepository.getTemplate(any()) } throws IllegalStateException("assets missing")
        every { ticketParser.parse(rawText, ParsingTemplate.GENERIC) } returns parsed

        assertEquals(parsed, useCase(10L).getOrThrow().data)
    }

    @Test
    fun invoke_whenParserFails_returnsEmptyDataForManualEntry() = runTest {
        coEvery { ticketRepository.getCapturedTicket(10L) } returns capturedTicket(storeId = null)
        coEvery { templateRepository.getTemplate(null) } returns ParsingTemplate.GENERIC
        every { ticketParser.parse(any(), any()) } throws IllegalArgumentException("bad regex")

        assertEquals(ParsedTicket.EMPTY, useCase(10L).getOrThrow().data)
    }

    @Test
    fun invoke_withoutOcrText_returnsEmptyDataWithoutParsing() = runTest {
        coEvery { ticketRepository.getCapturedTicket(10L) } returns capturedTicket(storeId = null, rawOcrText = null)

        assertEquals(ParsedTicket.EMPTY, useCase(10L).getOrThrow().data)
        verify(exactly = 0) { ticketParser.parse(any(), any()) }
    }

    @Test
    fun invoke_whenTicketMissing_returnsFailure() = runTest {
        coEvery { ticketRepository.getCapturedTicket(10L) } returns null

        assertTrue(useCase(10L).isFailure)
    }
}
