package com.ticketvault.classification

import com.ticketvault.parsing.store.StoreTextNormalizer

data class ProductCategory(
    val category: String,
    val subcategory: String?,
)

interface ProductClassifier {
    fun classify(productName: String): ProductCategory?
}

/**
 * Clasifica un artículo buscando las palabras clave del diccionario en su nombre.
 *
 * Las palabras clave deben aparecer como palabras completas (admitiendo el plural en -S/-ES)
 * y se ignoran las palabras vacías ("pan de molde" coincide con "PAN MOLDE"). Si coinciden
 * varias, gana la más larga (más específica: "tomate frito" frente a "tomate"); a igual
 * longitud, la que aparece antes en el diccionario.
 */
class KeywordProductClassifier(dictionary: CategoryDictionary) : ProductClassifier {

    private val rules: List<KeywordRule> = dictionary.categories.flatMap { category ->
        category.keywords.map { KeywordRule(it, ProductCategory(category.name, null)) } +
            category.subcategories.flatMap { subcategory ->
                subcategory.keywords.map { KeywordRule(it, ProductCategory(category.name, subcategory.name)) }
            }
    }.filter { it.tokens.isNotEmpty() }

    override fun classify(productName: String): ProductCategory? {
        val tokens = ProductNameTokenizer.tokenize(productName)
        if (tokens.isEmpty()) return null
        return rules
            .filter { it.matches(tokens) }
            .maxByOrNull { it.length }
            ?.category
    }

    private class KeywordRule(keyword: String, val category: ProductCategory) {
        val tokens: List<String> = ProductNameTokenizer.tokenize(keyword)
        val length: Int = tokens.sumOf { it.length }

        fun matches(name: List<String>): Boolean =
            (0..name.size - tokens.size).any { start ->
                tokens.indices.all { offset -> tokenMatches(name[start + offset], tokens[offset]) }
            }

        private fun tokenMatches(token: String, keyword: String): Boolean =
            token == keyword || token == keyword + "S" || token == keyword + "ES"
    }
}

object ProductNameTokenizer {

    private val STOP_WORDS = setOf("DE", "DEL", "LA", "LAS", "EL", "LOS", "CON", "EN", "Y", "A", "AL", "PARA")

    /** Mismo criterio que la clave de las correcciones del usuario. */
    fun normalize(productName: String): String = StoreTextNormalizer.normalize(productName)

    fun tokenize(text: String): List<String> =
        normalize(text).split(' ').filter { it.isNotEmpty() && it !in STOP_WORDS }
}
