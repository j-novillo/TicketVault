package com.ticketvault.ui.capture

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ticketvault.R
import com.ticketvault.camera.CameraPreview

@Composable
fun CaptureScreen(
    onTicketRecognized: (ticketId: Long) -> Unit,
    showTicketSavedMessage: Boolean,
    onTicketSavedMessageShown: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalActivity.current
    val context = LocalContext.current
    val currentOnTicketRecognized by rememberUpdatedState(onTicketRecognized)

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { result ->
        viewModel.onScanActivityResult(result.resultCode, result.data)
    }

    LaunchedEffect(viewModel) {
        viewModel.scanIntentSenders.collect { intentSender ->
            scanLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.recognizedTickets.collect { ticketId -> currentOnTicketRecognized(ticketId) }
    }

    LaunchedEffect(showTicketSavedMessage) {
        if (showTicketSavedMessage) {
            snackbarHostState.showSnackbar(context.getString(R.string.capture_ticket_saved))
            onTicketSavedMessageShown()
        }
    }

    LaunchedEffect(viewModel, context) {
        viewModel.errors.collect { error ->
            val messageRes = when (error) {
                CaptureError.Cancelled -> R.string.capture_error_cancelled
                CaptureError.ScanFailed -> R.string.capture_error_scan_failed
                CaptureError.SaveFailed -> R.string.capture_error_save_failed
                CaptureError.EnhanceFailed -> R.string.capture_error_enhance_failed
                CaptureError.ScannerUnavailable -> R.string.capture_error_scanner_unavailable
                CaptureError.InvalidImage -> R.string.capture_error_invalid_image
            }
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val isBusy = uiState is CaptureUiState.RequestingScan ||
                uiState is CaptureUiState.Saving ||
                uiState is CaptureUiState.RecognizingText
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.capture_scan_button)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = null,
                    )
                },
                onClick = {
                    if (isBusy) return@ExtendedFloatingActionButton
                    val currentActivity = activity as? Activity ?: return@ExtendedFloatingActionButton
                    viewModel.requestScan(currentActivity)
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
            )

            when (uiState) {
                CaptureUiState.Idle -> Unit

                CaptureUiState.RequestingScan,
                CaptureUiState.Saving,
                -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                CaptureUiState.RecognizingText -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.capture_processing_ocr),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
