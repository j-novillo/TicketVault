package com.ticketvault.classification

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CategoryDictionaryAssetTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val dictionary =
        json.decodeFromString<CategoryDictionary>(File("src/main/assets/classification/keywords.json").readText())
    private val classifier = KeywordProductClassifier(dictionary)

    @Test
    fun dictionary_hasFoodAndNonFoodCategoriesWithoutDuplicatedKeywords() {
        assertEquals(listOf("Comida", "No comestible"), dictionary.categories.map { it.name })

        val keywords = dictionary.categories.flatMap { category ->
            category.keywords + category.subcategories.flatMap { it.keywords }
        }.map { ProductNameTokenizer.tokenize(it).joinToString(" ") }

        assertTrue(keywords.none { it.isEmpty() })
        assertEquals(emptySet<String>(), keywords.groupBy { it }.filterValues { it.size > 1 }.keys)
    }

    @Test
    fun dictionary_classifiesTypicalTicketItems() {
        mapOf(
            "LECHE ENTERA" to ProductCategory("Comida", "Lácteos y huevos"),
            "PECHUGA POLLO" to ProductCategory("Comida", "Carne"),
            "PLATANOS CANARIAS" to ProductCategory("Comida", "Fruta"),
            "TOMATE FRITO" to ProductCategory("Comida", "Despensa"),
            "PAN MOLDE" to ProductCategory("Comida", "Panadería y bollería"),
            "JAMON SERRANO" to ProductCategory("Comida", "Procesados"),
            "CHOCOLATE CON LECHE" to ProductCategory("Comida", "Dulces y snacks"),
            "AGUA MINERAL 1,5L" to ProductCategory("Comida", "Bebidas"),
            "DETERGENTE LIQUIDO" to ProductCategory("No comestible", "Limpieza"),
            "AGUA MICELAR" to ProductCategory("No comestible", "Higiene personal"),
            "BOLSA BASURA" to ProductCategory("No comestible", "Limpieza"),
            "PIENSO PERRO" to ProductCategory("No comestible", "Mascotas"),
        ).forEach { (name, expected) ->
            assertEquals(name, expected, classifier.classify(name))
        }
    }
}
