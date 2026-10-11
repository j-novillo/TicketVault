package com.ticketvault.parsing.store

import javax.inject.Inject

/**
 * Extrae de la cabecera del ticket la línea más característica del comercio
 * (normalmente su nombre o razón social), para aprenderla como patrón cuando
 * el usuario asigna el comercio manualmente.
 */
class HeaderSignatureExtractor @Inject constructor() {

    fun extract(rawOcrText: String): String? =
        StoreTextNormalizer.headerLines(rawOcrText, StoreMatcher.HEADER_LINES)
            .firstOrNull(::isDistinctive)
            ?.take(MAX_SIGNATURE_LENGTH)
            ?.trim()

    private fun isDistinctive(line: String): Boolean {
        val letters = line.count { it.isLetter() }
        val digits = line.count { it.isDigit() }
        if (letters < MIN_LETTERS || digits > letters / 2) return false
        if (line in GENERIC_LINES) return false
        val firstWord = line.substringBefore(' ')
        return firstWord !in NON_STORE_PREFIXES
    }

    companion object {
        private const val MIN_LETTERS = 4
        private const val MAX_SIGNATURE_LENGTH = 40

        private val GENERIC_LINES = setOf(
            "FACTURA",
            "FACTURA SIMPLIFICADA",
            "TICKET",
            "TICKET DE COMPRA",
            "TIQUE",
            "BIENVENIDO",
            "BIENVENIDOS",
            "SUPERMERCADO",
            "SUPERMERCADOS",
            "HIPERMERCADO",
            "COPIA",
            "COPIA CLIENTE",
        )

        private val NON_STORE_PREFIXES = setOf(
            "C", "CL", "CALLE", "AV", "AVDA", "AVENIDA", "PZA", "PLAZA", "CTRA", "CARRETERA", "PASEO",
            "TEL", "TELF", "TELEFONO", "CIF", "NIF", "FECHA", "HORA", "CAJA", "OP", "CAJERO",
            "FACTURA", "TICKET", "TIQUE", "WWW", "HTTP", "HTTPS", "CP",
        )
    }
}
