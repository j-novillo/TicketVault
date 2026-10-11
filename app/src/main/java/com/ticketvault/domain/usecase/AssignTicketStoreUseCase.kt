package com.ticketvault.domain.usecase

import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.parsing.store.HeaderSignatureExtractor
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Asigna manualmente un comercio a un ticket y aprende la cabecera del ticket
 * como patrón del comercio, para que la próxima vez se detecte automáticamente.
 */
class AssignTicketStoreUseCase @Inject constructor(
    private val ticketRepository: TicketRepository,
    private val storeRepository: StoreRepository,
    private val headerSignatureExtractor: HeaderSignatureExtractor,
) {
    suspend fun assignExisting(ticketId: Long, storeId: Long): Result<Store> = runCatchingNonCancellable {
        val store = checkNotNull(storeRepository.getStore(storeId)) { "Store $storeId not found" }
        assign(ticketId, store)
    }

    suspend fun assignNew(ticketId: Long, storeName: String): Result<Store> = runCatchingNonCancellable {
        assign(ticketId, storeRepository.createStore(storeName))
    }

    private suspend fun assign(ticketId: Long, store: Store): Store {
        ticketRepository.updateStore(ticketId, store.id)
        ticketRepository.getRawOcrText(ticketId)
            ?.let(headerSignatureExtractor::extract)
            ?.let { signature -> storeRepository.learnPattern(store.id, signature) }
        return store
    }

    private inline fun <T> runCatchingNonCancellable(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
