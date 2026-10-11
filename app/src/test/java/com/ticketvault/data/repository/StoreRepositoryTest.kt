package com.ticketvault.data.repository

import com.ticketvault.data.local.dao.StoreDao
import com.ticketvault.data.local.entity.StoreEntity
import com.ticketvault.domain.model.Store
import com.ticketvault.parsing.store.StoreMatcher
import com.ticketvault.parsing.store.StoreTemplate
import com.ticketvault.parsing.store.StoreTemplateSource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoreRepositoryTest {

    private val storeDao = mockk<StoreDao>(relaxUnitFun = true)
    private val templateSource = mockk<StoreTemplateSource>()
    private val repository = StoreRepository(storeDao, templateSource, StoreMatcher())

    private val mercadonaEntity = StoreEntity(
        id = 1L,
        name = "Mercadona",
        recognitionPatterns = listOf("MERCADONA"),
        templateId = "mercadona",
        parsingTemplate = "mercadona",
    )

    init {
        every { templateSource.loadTemplates() } returns listOf(
            StoreTemplate(id = "mercadona", name = "Mercadona", headerPatterns = listOf("MERCADONA")),
            StoreTemplate(id = "lidl", name = "Lidl", headerPatterns = listOf("LIDL")),
        )
    }

    @Test
    fun detectStore_seedsMissingTemplatesOnlyOnce() = runTest {
        coEvery { storeDao.getTemplateIds() } returns listOf("mercadona")
        coEvery { storeDao.getAll() } returns listOf(mercadonaEntity)
        val inserted = slot<List<StoreEntity>>()
        coEvery { storeDao.insertAllIgnoringDuplicates(capture(inserted)) } returns Unit

        repository.detectStore("MERCADONA S.A.")
        repository.detectStore("MERCADONA S.A.")

        assertEquals(listOf("lidl"), inserted.captured.map { it.templateId })
        verify(exactly = 1) { templateSource.loadTemplates() }
    }

    @Test
    fun detectStore_returnsMatchingStore() = runTest {
        coEvery { storeDao.getTemplateIds() } returns listOf("mercadona", "lidl")
        coEvery { storeDao.getAll() } returns listOf(mercadonaEntity)

        assertEquals(Store(1L, "Mercadona"), repository.detectStore("Mercadona, S.A.\nTOTAL 1,00"))
        assertNull(repository.detectStore("FRUTERIA PEPE"))
    }

    @Test
    fun createStore_returnsExistingStoreWhenNameAlreadyExists() = runTest {
        coEvery { storeDao.getTemplateIds() } returns listOf("mercadona", "lidl")
        coEvery { storeDao.findByName("mercadona") } returns mercadonaEntity

        assertEquals(Store(1L, "Mercadona"), repository.createStore("  mercadona "))
        coVerify(exactly = 0) { storeDao.insert(any()) }
    }

    @Test
    fun createStore_insertsStoreUsingItsNameAsPattern() = runTest {
        coEvery { storeDao.getTemplateIds() } returns listOf("mercadona", "lidl")
        coEvery { storeDao.findByName(any()) } returns null
        val inserted = slot<StoreEntity>()
        coEvery { storeDao.insert(capture(inserted)) } returns 42L

        val store = repository.createStore("Frutería Pepe")

        assertEquals(Store(42L, "Frutería Pepe"), store)
        assertEquals(listOf("FRUTERIA PEPE"), inserted.captured.recognitionPatterns)
        assertNull(inserted.captured.templateId)
    }

    @Test
    fun learnPattern_appendsNewPatternOnly() = runTest {
        coEvery { storeDao.getById(1L) } returns mercadonaEntity

        repository.learnPattern(1L, "Mercadona")
        repository.learnPattern(1L, "Hacendado S.A.")

        coVerify(exactly = 1) {
            storeDao.update(mercadonaEntity.copy(recognitionPatterns = listOf("MERCADONA", "HACENDADO S A")))
        }
    }
}
