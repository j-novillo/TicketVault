package com.ticketvault.domain.usecase

import com.ticketvault.classification.ProductCategory
import com.ticketvault.data.repository.ProductClassificationRepository
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Sugiere categoría/subcategoría para cada artículo. Si la clasificación falla se dejan
 * sin clasificar en lugar de devolver un error, para que el usuario los clasifique a mano.
 */
class ClassifyProductsUseCase @Inject constructor(
    private val classificationRepository: ProductClassificationRepository,
) {
    suspend operator fun invoke(productNames: List<String>): List<ProductCategory?> {
        if (productNames.isEmpty()) return emptyList()
        return try {
            classificationRepository.classify(productNames)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            productNames.map { null }
        }
    }
}
