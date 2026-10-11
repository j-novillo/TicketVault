package com.ticketvault.data.repository

import com.ticketvault.classification.CategoryDefinition
import com.ticketvault.classification.CategoryDictionary
import com.ticketvault.classification.CategoryDictionarySource
import com.ticketvault.classification.ProductCategory
import com.ticketvault.classification.SubcategoryDefinition
import com.ticketvault.data.local.dao.ProductCategoryRuleDao
import com.ticketvault.data.local.entity.ProductCategoryRuleEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductClassificationRepositoryTest {

    private val dictionarySource = mockk<CategoryDictionarySource>()
    private val ruleDao = mockk<ProductCategoryRuleDao>(relaxUnitFun = true)
    private val repository = ProductClassificationRepository(dictionarySource, ruleDao)

    init {
        every { dictionarySource.loadDictionary() } returns CategoryDictionary(
            categories = listOf(
                CategoryDefinition(
                    name = "Comida",
                    subcategories = listOf(SubcategoryDefinition("Lácteos", listOf("leche"))),
                ),
            ),
        )
    }

    @Test
    fun classify_prefersLearnedRulesOverKeywords() = runTest {
        coEvery { ruleDao.findByNames(listOf("LECHE LIMPIADORA", "LECHE ENTERA", "MISTERIO")) } returns listOf(
            ProductCategoryRuleEntity("LECHE LIMPIADORA", "No comestible", "Higiene personal"),
        )

        val result = repository.classify(listOf("Leche limpiadora", "LECHE ENTERA", "MISTERIO"))

        assertEquals(
            listOf(
                ProductCategory("No comestible", "Higiene personal"),
                ProductCategory("Comida", "Lácteos"),
                null,
            ),
            result,
        )
    }

    @Test
    fun learnCorrections_storesRulesByNormalizedName() = runTest {
        repository.learnCorrections(
            mapOf(
                "Leche limpiadora" to ProductCategory("No comestible", "Higiene personal"),
                "  " to ProductCategory("Comida", null),
            ),
        )

        coVerify(exactly = 1) {
            ruleDao.upsert(listOf(ProductCategoryRuleEntity("LECHE LIMPIADORA", "No comestible", "Higiene personal")))
        }
    }

    @Test
    fun learnCorrections_withoutCorrections_doesNotTouchTheDatabase() = runTest {
        repository.learnCorrections(emptyMap())

        coVerify(exactly = 0) { ruleDao.upsert(any()) }
    }
}
