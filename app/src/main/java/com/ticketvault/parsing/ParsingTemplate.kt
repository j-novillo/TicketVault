package com.ticketvault.parsing

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Ajustes del motor de parsing para el formato de ticket de un comercio.
 *
 * Las regex se evalúan sobre la línea normalizada (`StoreTextNormalizer`: sin tildes,
 * en mayúsculas y sin puntuación) y se añaden a las reglas genéricas del parser.
 */
@Serializable
data class ParsingTemplate(
    val id: String,
    /** Las líneas de artículo empiezan por las unidades ("2 YOGUR GRIEGO 0,60 1,20"). */
    val leadingQuantity: Boolean = true,
    /** Cabecera de columnas tras la que empiezan los artículos. */
    val itemsStartPatterns: List<String> = emptyList(),
    /** Línea del total del ticket; también marca el fin de los artículos. */
    val totalPatterns: List<String> = emptyList(),
    /** Líneas que no son artículos aunque terminen en importe. */
    val ignorePatterns: List<String> = emptyList(),
) {
    companion object {
        val GENERIC = ParsingTemplate(id = "generic")
    }
}

@Serializable
data class ParsingTemplatesFile(
    val templates: List<ParsingTemplate>,
)

interface ParsingTemplateSource {
    fun loadTemplates(): List<ParsingTemplate>
}

class AssetParsingTemplateSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : ParsingTemplateSource {

    override fun loadTemplates(): List<ParsingTemplate> =
        context.assets.open(ASSET_PATH).bufferedReader().use { reader ->
            json.decodeFromString<ParsingTemplatesFile>(reader.readText()).templates
        }

    companion object {
        private const val ASSET_PATH = "parsing/parsing_templates.json"
        private val json = Json { ignoreUnknownKeys = true }
    }
}
