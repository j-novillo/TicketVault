package com.ticketvault.camera

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmsDocumentScannerGateway @Inject constructor() : DocumentScannerGateway {

    private val scannerOptions: GmsDocumentScannerOptions =
        GmsDocumentScannerOptions.Builder()
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
            .setGalleryImportAllowed(true)
            .setPageLimit(1)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .build()

    override fun getScanIntentSender(
        activity: Activity,
        onReady: (IntentSender) -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        GmsDocumentScanning.getClient(scannerOptions)
            .getStartScanIntent(activity)
            .addOnSuccessListener(onReady)
            .addOnFailureListener(onFailure)
    }

    override fun parseScanResult(resultCode: Int, data: Intent?): ScanParseResult {
        if (resultCode != RESULT_OK) {
            return ScanParseResult.Cancelled
        }

        val scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(data)
            ?: return ScanParseResult.Error("No se pudo capturar el ticket")

        val pageUri = scanningResult.pages?.firstOrNull()?.imageUri
            ?: return ScanParseResult.Error("No se pudo capturar el ticket")

        return ScanParseResult.Success(pageUri)
    }
}
