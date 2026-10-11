package com.ticketvault.ocr

import kotlin.math.abs

data class OcrTextLine(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val centerY: Double get() = (top + bottom) / 2.0
    val height: Int get() = bottom - top
}

/**
 * ML Kit agrupa el texto por bloques y en los tickets suele separar la columna
 * de precios de la de nombres. Aquí se reconstruyen las filas visuales
 * (líneas con la misma altura en la imagen, ordenadas de izquierda a derecha)
 * para que cada artículo quede en una sola línea junto a su precio.
 */
object OcrLineAssembler {

    private const val ROW_TOLERANCE = 0.6

    fun assemble(lines: List<OcrTextLine>): String {
        val rows = mutableListOf<MutableList<OcrTextLine>>()
        lines
            .filter { it.text.isNotBlank() }
            .sortedBy { it.centerY }
            .forEach { line ->
                val currentRow = rows.lastOrNull()
                if (currentRow != null && currentRow.accepts(line)) {
                    currentRow += line
                } else {
                    rows += mutableListOf(line)
                }
            }
        return rows.joinToString("\n") { row ->
            row.sortedBy { it.left }.joinToString(" ") { it.text.trim() }
        }
    }

    private fun List<OcrTextLine>.accepts(line: OcrTextLine): Boolean {
        val rowCenter = map { it.centerY }.average()
        val averageHeight = (map { it.height }.average() + line.height) / 2
        return abs(line.centerY - rowCenter) <= ROW_TOLERANCE * averageHeight
    }
}
