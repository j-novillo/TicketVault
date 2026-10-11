package com.ticketvault.parsing

import com.ticketvault.parsing.store.StoreTemplatesFile
import kotlinx.serialization.json.Json
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ParsingTemplatesAssetTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun readAsset(path: String) = File("src/main/assets/$path").readText()

    @Test
    fun templates_belongToKnownStoresAndHaveValidPatterns() {
        val templates = json.decodeFromString<ParsingTemplatesFile>(readAsset("parsing/parsing_templates.json")).templates
        val storeIds = json.decodeFromString<StoreTemplatesFile>(readAsset("parsing/store_templates.json"))
            .stores.map { it.id }.toSet()

        assertTrue(templates.isNotEmpty())
        templates.forEach { template ->
            assertTrue("Unknown store template ${template.id}", template.id in storeIds)
            (template.itemsStartPatterns + template.totalPatterns + template.ignorePatterns).forEach(::Regex)
        }
    }
}
