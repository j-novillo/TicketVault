package com.ticketvault.ui.capture

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
    ) : CaptureUiState
}

enum class CaptureError {
    Cancelled,
    ScanFailed,
    SaveFailed,
    EnhanceFailed,
    ScannerUnavailable,
    InvalidImage,
}
