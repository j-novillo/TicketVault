package com.ticketvault.ui.capture

sealed interface CaptureUiState {
    data object Idle : CaptureUiState

    data object RequestingScan : CaptureUiState

    data object Saving : CaptureUiState

    data class Success(val imagePath: String) : CaptureUiState
}

enum class CaptureError {
    Cancelled,
    ScanFailed,
    SaveFailed,
    ScannerUnavailable,
}
