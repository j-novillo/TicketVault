package com.ticketvault.parsing.store

import javax.inject.Inject

data class StoreCandidate(
    val storeId: Long,
    val patterns: List<String>,
)

data class StoreMatch(
    val storeId: Long,
    val matchedPattern: String,
    val lineIndex: Int,
)

/**
 * Detecta el comercio buscando sus patrones de cabecera en las primeras líneas
 * del texto OCR. Gana la coincidencia en la línea más alta; a igualdad de línea,
 * el patrón más largo (más específico).
 */
class StoreMatcher @Inject constructor() {

    fun match(rawOcrText: String, candidates: List<StoreCandidate>): StoreMatch? {
        val lines = StoreTextNormalizer.headerLines(rawOcrText, HEADER_LINES)
        if (lines.isEmpty()) return null

        return candidates
            .flatMap { candidate ->
                candidate.patterns
                    .map(StoreTextNormalizer::normalize)
                    .filter { it.length >= MIN_PATTERN_LENGTH }
                    .distinct()
                    .mapNotNull { pattern ->
                        val lineIndex = lines.indexOfFirst { line -> lineContains(line, pattern) }
                        if (lineIndex < 0) {
                            null
                        } else {
                            StoreMatch(candidate.storeId, pattern, lineIndex)
                        }
                    }
            }
            .minWithOrNull(
                compareBy<StoreMatch> { it.lineIndex }.thenByDescending { it.matchedPattern.length },
            )
    }

    private fun lineContains(line: String, pattern: String): Boolean {
        if (" $line ".contains(" $pattern ")) return true
        // OCR suele partir palabras largas ("MERCA DONA"); solo para patrones largos,
        // porque sin límites de palabra los cortos dan falsos positivos.
        return pattern.length >= COMPACT_MATCH_MIN_LENGTH &&
            line.replace(" ", "").contains(pattern.replace(" ", ""))
    }

    companion object {
        const val HEADER_LINES = 10
        private const val MIN_PATTERN_LENGTH = 3
        private const val COMPACT_MATCH_MIN_LENGTH = 8
    }
}
