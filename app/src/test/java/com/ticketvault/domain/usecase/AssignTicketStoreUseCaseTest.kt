package com.ticketvault.domain.usecase

import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.parsing.store.HeaderSignatureExtractor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssignTicketStoreUseCaseTest {

    private val ticketRepository = mockk<TicketRepository>(relaxUnitFun = true)
    private val storeRepository = mockk<StoreRepository>(relaxUnitFun = true)
    private val useCase = AssignTicketStoreUseCase(
        ticketRepository = ticketRepository,
        storeRepository = storeRepository,
        headerSignatureExtractor = HeaderSignatureExtractor(),
    )
    private val rawText = "Fruteria Pepe, S.L.\nPLATANOS 2,10"

    @Test
    fun assignExisting_updatesTicketAndLearnsHeaderPattern() = runTest {
        val store = Store(id = 5L, name = "Frutería Pepe")
        coEvery { storeRepository.getStore(5L) } returns store
        coEvery { ticketRepository.getRawOcrText(10L) } returns rawText

        val result = useCase.assignExisting(ticketId = 10L, storeId = 5L)

        assertEquals(store, result.getOrNull())
        coVerify(exactly = 1) { ticketRepository.updateStore(10L, 5L) }
        coVerify(exactly = 1) { storeRepository.learnPattern(5L, "FRUTERIA PEPE S L") }
    }

    @Test
    fun assignNew_createsStoreAndAssignsIt() = runTest {
        val store = Store(id = 6L, name = "Frutería Pepe")
        coEvery { storeRepository.createStore("Frutería Pepe") } returns store
        coEvery { ticketRepository.getRawOcrText(10L) } returns null

        val result = useCase.assignNew(ticketId = 10L, storeName = "Frutería Pepe")

        assertEquals(store, result.getOrNull())
        coVerify(exactly = 1) { ticketRepository.updateStore(10L, 6L) }
        coVerify(exactly = 0) { storeRepository.learnPattern(any(), any()) }
    }

    @Test
    fun assignExisting_whenStoreMissing_returnsFailure() = runTest {
        coEvery { storeRepository.getStore(99L) } returns null

        val result = useCase.assignExisting(ticketId = 10L, storeId = 99L)

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { ticketRepository.updateStore(any(), any()) }
    }
}
