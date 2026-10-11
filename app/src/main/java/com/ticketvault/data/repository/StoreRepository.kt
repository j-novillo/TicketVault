package com.ticketvault.data.repository

import com.ticketvault.data.local.dao.StoreDao
import com.ticketvault.data.local.entity.StoreEntity
import com.ticketvault.domain.model.Store
import com.ticketvault.parsing.store.StoreCandidate
import com.ticketvault.parsing.store.StoreMatcher
import com.ticketvault.parsing.store.StoreTemplateSource
import com.ticketvault.parsing.store.StoreTextNormalizer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreRepository @Inject constructor(
    private val storeDao: StoreDao,
    private val templateSource: StoreTemplateSource,
    private val storeMatcher: StoreMatcher,
) {
    private val seedMutex = Mutex()

    @Volatile
    private var seeded = false

    fun observeStores(): Flow<List<Store>> = flow {
        ensureSeeded()
        emitAll(storeDao.observeAll().map { entities -> entities.map { it.toDomain() } })
    }

    suspend fun detectStore(rawOcrText: String): Store? {
        ensureSeeded()
        val stores = storeDao.getAll()
        val match = storeMatcher.match(
            rawOcrText = rawOcrText,
            candidates = stores.map { StoreCandidate(it.id, it.recognitionPatterns) },
        ) ?: return null
        return stores.first { it.id == match.storeId }.toDomain()
    }

    suspend fun getStore(storeId: Long): Store? = storeDao.getById(storeId)?.toDomain()

    /** Crea el comercio o devuelve el existente si ya hay uno con ese nombre (sin distinguir mayúsculas). */
    suspend fun createStore(name: String): Store {
        ensureSeeded()
        val trimmedName = name.trim()
        require(trimmedName.isNotEmpty()) { "Store name must not be blank" }
        storeDao.findByName(trimmedName)?.let { return it.toDomain() }

        val entity = StoreEntity(
            name = trimmedName,
            recognitionPatterns = listOf(StoreTextNormalizer.normalize(trimmedName)),
            templateId = null,
            parsingTemplate = null,
        )
        return entity.copy(id = storeDao.insert(entity)).toDomain()
    }

    suspend fun learnPattern(storeId: Long, pattern: String) {
        val normalized = StoreTextNormalizer.normalize(pattern)
        if (normalized.isEmpty()) return
        val store = storeDao.getById(storeId) ?: return
        if (normalized in store.recognitionPatterns.map(StoreTextNormalizer::normalize)) return
        storeDao.update(store.copy(recognitionPatterns = store.recognitionPatterns + normalized))
    }

    private suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            val existingTemplateIds = storeDao.getTemplateIds().toSet()
            val missing = templateSource.loadTemplates()
                .filter { it.id !in existingTemplateIds }
                .map { template ->
                    StoreEntity(
                        name = template.name,
                        recognitionPatterns = template.headerPatterns,
                        templateId = template.id,
                        parsingTemplate = template.id,
                    )
                }
            if (missing.isNotEmpty()) {
                storeDao.insertAllIgnoringDuplicates(missing)
            }
            seeded = true
        }
    }

    private fun StoreEntity.toDomain() = Store(id = id, name = name)
}
