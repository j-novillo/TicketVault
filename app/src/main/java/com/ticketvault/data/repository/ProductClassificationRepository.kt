package com.ticketvault.data.repository

import com.ticketvault.classification.CategoryDefinition
import com.ticketvault.classification.CategoryDictionary
import com.ticketvault.classification.CategoryDictionarySource
import com.ticketvault.classification.KeywordProductClassifier
import com.ticketvault.classification.ProductCategory
import com.ticketvault.classification.ProductNameTokenizer
import com.ticketvault.data.local.dao.ProductCategoryRuleDao
import com.ticketvault.data.local.entity.ProductCategoryRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductClassificationRepository @Inject constructor(
    private val dictionarySource: CategoryDictionarySource,
    private val ruleDao: ProductCategoryRuleDao,
) {
    private val dictionary: CategoryDictionary by lazy { dictionarySource.loadDictionary() }
    private val keywordClassifier by lazy { KeywordProductClassifier(dictionary) }

    suspend fun getCategories(): List<CategoryDefinition> = withContext(Dispatchers.IO) { dictionary.categories }

    /** Las correcciones del usuario (por nombre de artículo exacto) prevalecen sobre las palabras clave. */
    suspend fun classify(productNames: List<String>): List<ProductCategory?> = withContext(Dispatchers.IO) {
        val keys = productNames.map(ProductNameTokenizer::normalize)
        val learned = keys.filter { it.isNotEmpty() }.distinct()
            .takeIf { it.isNotEmpty() }
            ?.let { ruleDao.findByNames(it) }
            .orEmpty()
            .associateBy { it.normalizedName }
        productNames.mapIndexed { index, name ->
            learned[keys[index]]?.let { ProductCategory(it.category, it.subcategory) }
                ?: keywordClassifier.classify(name)
        }
    }

    suspend fun learnCorrections(corrections: Map<String, ProductCategory>) {
        val rules = corrections.mapNotNull { (productName, category) ->
            ProductNameTokenizer.normalize(productName).takeIf { it.isNotEmpty() }?.let { key ->
                ProductCategoryRuleEntity(
                    normalizedName = key,
                    category = category.category,
                    subcategory = category.subcategory,
                )
            }
        }
        if (rules.isNotEmpty()) ruleDao.upsert(rules)
    }
}
