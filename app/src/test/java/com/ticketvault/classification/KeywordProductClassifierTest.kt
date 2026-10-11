package com.ticketvault.classification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KeywordProductClassifierTest {

    private val dictionary = CategoryDictionary(
        categories = listOf(
            CategoryDefinition(
                name = "Comida",
                subcategories = listOf(
                    SubcategoryDefinition("Verdura", listOf("tomate", "limón")),
                    SubcategoryDefinition("Despensa", listOf("tomate frito", "sal")),
                    SubcategoryDefinition("Panadería", listOf("pan de molde", "pan")),
                ),
            ),
            CategoryDefinition(name = "No comestible", keywords = listOf("pilas")),
        ),
    )
    private val classifier = KeywordProductClassifier(dictionary)

    private val verdura = ProductCategory("Comida", "Verdura")
    private val despensa = ProductCategory("Comida", "Despensa")
    private val panaderia = ProductCategory("Comida", "Panadería")

    @Test
    fun classify_matchesKeywordIgnoringCaseAndAccents() {
        assertEquals(verdura, classifier.classify("Tomate pera"))
        assertEquals(verdura, classifier.classify("LIMON"))
    }

    @Test
    fun classify_acceptsPlurals() {
        assertEquals(verdura, classifier.classify("TOMATES CHERRY"))
        assertEquals(verdura, classifier.classify("LIMONES MALLA"))
    }

    @Test
    fun classify_prefersTheLongestKeyword() {
        assertEquals(despensa, classifier.classify("TOMATE FRITO 400G"))
    }

    @Test
    fun classify_ignoresStopWords() {
        assertEquals(panaderia, classifier.classify("PAN MOLDE INTEGRAL"))
        assertEquals(panaderia, classifier.classify("PAN DE MOLDE"))
    }

    @Test
    fun classify_requiresWholeWords() {
        assertNull(classifier.classify("SALSA BRAVA"))
        assertNull(classifier.classify("PANCETA"))
    }

    @Test
    fun classify_withCategoryLevelKeyword_returnsNoSubcategory() {
        assertEquals(ProductCategory("No comestible", null), classifier.classify("PILAS AA"))
    }

    @Test
    fun classify_withoutMatch_returnsNull() {
        assertNull(classifier.classify("XYZ 123"))
        assertNull(classifier.classify(""))
    }
}
