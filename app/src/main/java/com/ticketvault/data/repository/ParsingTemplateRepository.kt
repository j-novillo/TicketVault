package com.ticketvault.data.repository

import com.ticketvault.parsing.ParsingTemplate
import com.ticketvault.parsing.ParsingTemplateSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParsingTemplateRepository @Inject constructor(
    private val templateSource: ParsingTemplateSource,
) {
    private val templates: Map<String, ParsingTemplate> by lazy {
        templateSource.loadTemplates().associateBy { it.id }
    }

    /** Plantilla del comercio, o la genérica si no tiene (p. ej. comercios creados por el usuario). */
    suspend fun getTemplate(templateId: String?): ParsingTemplate = withContext(Dispatchers.IO) {
        templateId?.let(templates::get) ?: ParsingTemplate.GENERIC
    }
}
