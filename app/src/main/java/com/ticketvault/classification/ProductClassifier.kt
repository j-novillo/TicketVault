package com.ticketvault.classification

/**
 * Diccionario local de palabras clave → categoría/subcategoría.
 * Implementación pendiente (HU-3.x).
 */
data class ProductCategory(
    val category: String,
    val subcategory: String?,
)

interface ProductClassifier {
    fun classify(productName: String): ProductCategory?
}
