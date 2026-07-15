package com.ticketvault.ui.capture

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ticketvault.R
import com.ticketvault.camera.CameraPreview
import java.io.File

@Composable
fun CaptureScreen(
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val activity = LocalActivity.current
    val context = LocalContext.current

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

    LaunchedEffect(viewModel, context) {
        viewModel.errors.collect { error ->
            val messageRes = when (error) {
                CaptureError.Cancelled -> R.string.capture_error_cancelled
                CaptureError.ScanFailed -> R.string.capture_error_scan_failed
                CaptureError.SaveFailed -> R.string.capture_error_save_failed
                CaptureError.ScannerUnavailable -> R.string.capture_error_scanner_unavailable
            }
            snackbarHostState.showSnackbar(context.getString(messageRes))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val isBusy = uiState is CaptureUiState.RequestingScan || uiState is CaptureUiState.Saving
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

            when (val state = uiState) {
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

                is CaptureUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.capture_success_message),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AsyncImage(
                            model = File(state.imagePath),
                            contentDescription = stringResource(R.string.capture_success_thumbnail),
                            modifier = Modifier
                                .size(160.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}
