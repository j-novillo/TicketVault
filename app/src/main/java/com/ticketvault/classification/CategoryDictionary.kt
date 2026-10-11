package com.ticketvault.classification

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

/** Diccionario de categorías → subcategorías → palabras clave (`assets/classification/keywords.json`). */
@Serializable
data class CategoryDictionary(
    val categories: List<CategoryDefinition>,
)

@Serializable
data class CategoryDefinition(
    val name: String,
    /** Palabras clave que asignan la categoría sin subcategoría. */
    val keywords: List<String> = emptyList(),
    val subcategories: List<SubcategoryDefinition> = emptyList(),
)

@Serializable
data class SubcategoryDefinition(
    val name: String,
    val keywords: List<String> = emptyList(),
)

interface CategoryDictionarySource {
    fun loadDictionary(): CategoryDictionary
}

class AssetCategoryDictionarySource @Inject constructor(
    @ApplicationContext private val context: Context,
) : CategoryDictionarySource {

    override fun loadDictionary(): CategoryDictionary =
        context.assets.open(ASSET_PATH).bufferedReader().use { reader ->
            json.decodeFromString<CategoryDictionary>(reader.readText())
        }

    companion object {
        private const val ASSET_PATH = "classification/keywords.json"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
