package com.ticketvault.parsing.store

import java.text.Normalizer
import java.util.Locale

/**
 * Normaliza texto OCR y patrones para compararlos: sin tildes, en mayúsculas,
 * sin signos de puntuación y con espacios colapsados.
 */
object StoreTextNormalizer {

    private val DIACRITICS = Regex("""\p{Mn}+""")
    private val NON_ALPHANUMERIC = Regex("""[^A-Z0-9]+""")

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(DIACRITICS, "")
            .uppercase(Locale.ROOT)
            .replace(NON_ALPHANUMERIC, " ")
            .trim()

    fun headerLines(rawOcrText: String, maxLines: Int): List<String> =
        rawOcrText.lineSequence()
            .map(::normalize)
            .filter { it.isNotEmpty() }
            .take(maxLines)
            .toList()
}
