package com.ticketvault.ui.capture

sealed interface CaptureUiState {
    data object Idle : CaptureUiState

    data object RequestingScan : CaptureUiState

    data object Saving : CaptureUiState

    data class Success(
        val cleanedPath: String,
        val originalPath: String,
    ) : CaptureUiState
}

enum class CaptureError {
    Cancelled,
    ScanFailed,
    SaveFailed,
    ScannerUnavailable,
    InvalidImage,
}
