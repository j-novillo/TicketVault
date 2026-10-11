package com.ticketvault.domain.usecase

import com.ticketvault.data.repository.ParsingTemplateRepository
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.CapturedTicket
import com.ticketvault.domain.model.Store
import com.ticketvault.parsing.ParsedTicket
import com.ticketvault.parsing.ParsingTemplate
import com.ticketvault.parsing.TicketParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

data class TicketExtraction(
    val ticket: CapturedTicket,
    val store: Store?,
    val data: ParsedTicket,
)

/**
 * Extrae fecha, hora, total y artículos del texto OCR de un ticket ya escaneado,
 * usando la plantilla de parsing de su comercio (o la genérica).
 */
class ExtractTicketDataUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val storeRepository: StoreRepository,
    private val templateRepository: ParsingTemplateRepository,
    private val ticketParser: TicketParser,
) {
    /**
     * Si el parsing falla se devuelven datos vacíos en lugar de un error,
     * para que el usuario pueda introducirlos a mano en la revisión.
     */
    suspend operator fun invoke(ticketId: Long): Result<TicketExtraction> =
        try {
            val ticket = checkNotNull(ticketRepository.getCapturedTicket(ticketId)) { "Ticket $ticketId not found" }
            val store = ticket.storeId?.let { storeRepository.getStore(it) }
            val data = ticket.rawOcrText?.let { parse(it, store) } ?: ParsedTicket.EMPTY
            Result.success(TicketExtraction(ticket = ticket, store = store, data = data))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun parse(rawOcrText: String, store: Store?): ParsedTicket {
        val template = try {
            templateRepository.getTemplate(store?.let { storeRepository.getParsingTemplateId(it.id) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ParsingTemplate.GENERIC
        }
        return try {
            withContext(Dispatchers.Default) { ticketParser.parse(rawOcrText, template) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ParsedTicket.EMPTY
        }
    }
}
