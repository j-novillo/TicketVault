package com.ticketvault.parsing.store

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

@Serializable
data class StoreTemplate(
    val id: String,
    val name: String,
    val headerPatterns: List<String>,
)

@Serializable
data class StoreTemplatesFile(
    val stores: List<StoreTemplate>,
)

interface StoreTemplateSource {
    fun loadTemplates(): List<StoreTemplate>
}

class AssetStoreTemplateSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : StoreTemplateSource {

    override fun loadTemplates(): List<StoreTemplate> =
        context.assets.open(ASSET_PATH).bufferedReader().use { reader ->
            json.decodeFromString<StoreTemplatesFile>(reader.readText()).stores
        }

    companion object {
        private const val ASSET_PATH = "parsing/store_templates.json"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
