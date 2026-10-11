package com.ticketvault.ui.capture

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ticketvault.camera.DocumentScannerGateway
import com.ticketvault.camera.ScanParseResult
import com.ticketvault.data.local.entity.TicketStatus
import com.ticketvault.data.repository.StoreRepository
import com.ticketvault.domain.model.Store
import com.ticketvault.domain.usecase.AssignTicketStoreUseCase
import com.ticketvault.domain.usecase.ProcessImageError
import com.ticketvault.domain.usecase.ProcessScannedImageUseCase
import com.ticketvault.domain.usecase.RecognizeTicketTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val documentScannerGateway: DocumentScannerGateway,
    private val processScannedImageUseCase: ProcessScannedImageUseCase,
    private val recognizeTicketTextUseCase: RecognizeTicketTextUseCase,
    private val assignTicketStoreUseCase: AssignTicketStoreUseCase,
    storeRepository: StoreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CaptureUiState>(CaptureUiState.Idle)
    val uiState: StateFlow<CaptureUiState> = _uiState.asStateFlow()

    val stores: StateFlow<List<Store>> = storeRepository.observeStores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    private val _errors = MutableSharedFlow<CaptureError>(extraBufferCapacity = 1)
    val errors: SharedFlow<CaptureError> = _errors.asSharedFlow()

    private val _scanIntentSenders = MutableSharedFlow<IntentSender>(extraBufferCapacity = 1)
    val scanIntentSenders: SharedFlow<IntentSender> = _scanIntentSenders.asSharedFlow()

    fun requestScan(activity: Activity) {
        if (_uiState.value is CaptureUiState.RequestingScan ||
            _uiState.value is CaptureUiState.Saving ||
            _uiState.value is CaptureUiState.RecognizingText
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
            val processed = processScannedImageUseCase(sourceUri).getOrElse { error ->
                onProcessingFailed(error)
                return@launch
            }

            _uiState.update { CaptureUiState.RecognizingText }
            val recognized = recognizeTicketTextUseCase(processed).getOrElse { error ->
                onProcessingFailed(error)
                return@launch
            }

            _uiState.update {
                CaptureUiState.Success(
                    cleanedPath = processed.cleanedPath,
                    originalPath = processed.originalPath,
                    ticketId = recognized.ticketId,
                    needsManualReview = recognized.status == TicketStatus.NEEDS_MANUAL_REVIEW,
                    store = recognized.store,
                )
            }
        }
    }

    fun assignStore(storeId: Long) {
        val success = _uiState.value as? CaptureUiState.Success ?: return
        viewModelScope.launch {
            onStoreAssigned(assignTicketStoreUseCase.assignExisting(success.ticketId, storeId))
        }
    }

    fun createAndAssignStore(storeName: String) {
        val success = _uiState.value as? CaptureUiState.Success ?: return
        if (storeName.isBlank()) return
        viewModelScope.launch {
            onStoreAssigned(assignTicketStoreUseCase.assignNew(success.ticketId, storeName))
        }
    }

    private suspend fun onStoreAssigned(result: Result<Store>) {
        result
            .onSuccess { store ->
                _uiState.update { state ->
                    if (state is CaptureUiState.Success) state.copy(store = store) else state
                }
            }
            .onFailure { _errors.emit(CaptureError.StoreAssignFailed) }
    }

    private suspend fun onProcessingFailed(error: Throwable) {
        _uiState.update { CaptureUiState.Idle }
        _errors.emit(
            when (error) {
                is ProcessImageError.Validation -> CaptureError.InvalidImage
                is ProcessImageError.EnhanceFailed -> CaptureError.EnhanceFailed
                is ProcessImageError.SaveFailed -> CaptureError.SaveFailed
                else -> CaptureError.SaveFailed
            },
        )
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
