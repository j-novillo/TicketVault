package com.ticketvault.ui.capture

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketvault.camera.DocumentScannerGateway
import com.ticketvault.camera.ImageValidationError
import com.ticketvault.camera.ScanParseResult
import com.ticketvault.domain.usecase.ProcessScannedImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val documentScannerGateway: DocumentScannerGateway,
    private val processScannedImageUseCase: ProcessScannedImageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    private val _errors = MutableSharedFlow<CaptureError>(extraBufferCapacity = 1)
    val errors: SharedFlow<CaptureError> = _errors.asSharedFlow()

    private val _scanIntentSenders = MutableSharedFlow<IntentSender>(extraBufferCapacity = 1)
    val scanIntentSenders: SharedFlow<IntentSender> = _scanIntentSenders.asSharedFlow()

    fun requestScan(activity: Activity) {
        if (_uiState.value is CaptureUiState.RequestingScan ||
            _uiState.value is CaptureUiState.Saving
        ) {
            return
        }

        _uiState.update { CaptureUiState.RequestingScan }
        documentScannerGateway.getScanIntentSender(
            activity = activity,
            onReady = { intentSender ->
                viewModelScope.launch {
                    _scanIntentSenders.emit(intentSender)
                }
            },
            onFailure = {
                _uiState.update { CaptureUiState.Idle }
                _errors.tryEmit(CaptureError.ScannerUnavailable)
            },
        )
    }

    fun onScanActivityResult(resultCode: Int, data: Intent?) {
        when (val parseResult = documentScannerGateway.parseScanResult(resultCode, data)) {
            is ScanParseResult.Cancelled -> {
                _uiState.update { CaptureUiState.Idle }
                _errors.tryEmit(CaptureError.Cancelled)
            }

            is ScanParseResult.Error -> {
                _uiState.update { CaptureUiState.Idle }
                _errors.tryEmit(CaptureError.ScanFailed)
            }

            is ScanParseResult.Success -> persistImage(parseResult.pageUri)
        }
    }

    fun dismissSuccess() {
        _uiState.update { CaptureUiState.Idle }
    }

    private fun persistImage(sourceUri: android.net.Uri) {
        _uiState.update { CaptureUiState.Saving }
        viewModelScope.launch {
            processScannedImageUseCase(sourceUri)
                .onSuccess { processed ->
                    _uiState.update {
                        CaptureUiState.Success(
                            cleanedPath = processed.cleanedPath,
                            originalPath = processed.originalPath,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { CaptureUiState.Idle }
                    _errors.emit(
                        if (error is ImageValidationError) {
                            CaptureError.InvalidImage
                        } else {
                            CaptureError.SaveFailed
                        },
                    )
                }
        }
    }
}
