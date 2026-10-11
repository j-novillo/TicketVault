package com.ticketvault.ui.review

import com.ticketvault.classification.CategoryDefinition
import com.ticketvault.domain.model.Store

sealed interface TicketReviewUiState {
    data object Loading : TicketReviewUiState

    data object LoadFailed : TicketReviewUiState

    data class Editing(
        val cleanedImagePath: String?,
        val needsManualReview: Boolean,
        val store: Store?,
        val draft: TicketDraft,
        /** Opciones para reclasificar artículos a mano. */
        val categories: List<CategoryDefinition> = emptyList(),
        /** Tras editar a mano no se vuelve a parsear al cambiar el comercio, para no perder cambios. */
        val isEdited: Boolean = false,
        val showErrors: Boolean = false,
        val isSaving: Boolean = false,
    ) : TicketReviewUiState
}

enum class TicketReviewEvent {
    Saved,
    SaveFailed,
    InvalidFields,
    StoreAssignFailed,
}
