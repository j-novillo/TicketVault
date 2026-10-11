package com.ticketvault.domain.usecase

import com.ticketvault.classification.ProductCategory
import com.ticketvault.data.repository.ProductClassificationRepository
import com.ticketvault.data.repository.TicketRepository
import com.ticketvault.domain.model.ReviewedTicket
import com.ticketvault.domain.model.ReviewedTicketItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class SaveReviewedTicketUseCaseTest {

    private val ticketRepository = mockk<TicketRepository>(relaxUnitFun = true)
    private val classificationRepository = mockk<ProductClassificationRepository>(relaxUnitFun = true)
    private val useCase = SaveReviewedTicketUseCase(ticketRepository, classificationRepository)

    private val higiene = ProductCategory("No comestible", "Higiene personal")
    private val reviewed = ReviewedTicket(
        date = null,
        time = null,
        total = 3.0,
        items = listOf(
            ReviewedTicketItem("LECHE LIMPIADORA", 1.0, null, 2.0, category = higiene, isCategoryCorrected = true),
            ReviewedTicketItem("LECHE", 1.0, null, 1.0, category = ProductCategory("Comida", "Lácteos")),
            ReviewedTicketItem("RARO", 1.0, null, 0.0, category = null, isCategoryCorrected = true),
        ),
    )

    @Test
    fun invoke_savesTicketAndLearnsOnlyManualCorrections() = runTest {
        assertTrue(useCase(10L, reviewed).isSuccess)

        coVerify(exactly = 1) { ticketRepository.saveReviewedTicket(10L, reviewed) }
        coVerify(exactly = 1) { classificationRepository.learnCorrections(mapOf("LECHE LIMPIADORA" to higiene)) }
    }

    @Test
    fun invoke_whenSaveFails_returnsFailureWithoutLearning() = runTest {
        coEvery { ticketRepository.saveReviewedTicket(any(), any()) } throws IllegalStateException("db")

        assertTrue(useCase(10L, reviewed).isFailure)
        coVerify(exactly = 0) { classificationRepository.learnCorrections(any()) }
    }
}
