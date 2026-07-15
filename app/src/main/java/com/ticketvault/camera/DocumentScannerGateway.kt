package com.ticketvault.camera

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.net.Uri

interface DocumentScannerGateway {
    fun getScanIntentSender(
        activity: Activity,
        onReady: (IntentSender) -> Unit,
        onFailure: (Throwable) -> Unit,
    )

    fun parseScanResult(resultCode: Int, data: Intent?): ScanParseResult

    suspend fun persistScannedImage(sourceUri: Uri): Result<String>
}

sealed class ScanParseResult {
    data class Success(val pageUri: Uri) : ScanParseResult()

    data object Cancelled : ScanParseResult()

    data class Error(val message: String) : ScanParseResult()
}
