package com.ticketvault.ui.capture

import com.ticketvault.domain.model.Store

sealed interface CaptureUiState {
    data object Idle : CaptureUiState

    data object RequestingScan : CaptureUiState

    data object Saving : CaptureUiState

    data object RecognizingText : CaptureUiState

    data class Success(
        val cleanedPath: String,
        val originalPath: String,
        val ticketId: Long,
        val needsManualReview: Boolean,
        val store: Store?,
    ) : CaptureUiState
}

enum class CaptureError {
    Cancelled,
    ScanFailed,
    SaveFailed,
    EnhanceFailed,
    ScannerUnavailable,
    InvalidImage,
    StoreAssignFailed,
}
