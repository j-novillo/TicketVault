package com.ticketvault.ocr

import javax.inject.Inject

/**
 * Heurística para decidir si el texto OCR es aprovechable o si el ticket
 * debe marcarse como "requiere revisión manual".
 */
class OcrReadabilityEvaluator @Inject constructor() {

    fun isReadable(rawText: String): Boolean {
        val nonBlankLines = rawText.lines().count { it.isNotBlank() }
        val alphanumericChars = rawText.count { it.isLetterOrDigit() }
        return nonBlankLines >= MIN_LINES &&
            alphanumericChars >= MIN_ALPHANUMERIC_CHARS &&
            PRICE_REGEX.containsMatchIn(rawText)
    }

    companion object {
        private const val MIN_LINES = 3
        private const val MIN_ALPHANUMERIC_CHARS = 20
        private val PRICE_REGEX = Regex("""\d+[.,]\d{2}\b""")
    }
}
