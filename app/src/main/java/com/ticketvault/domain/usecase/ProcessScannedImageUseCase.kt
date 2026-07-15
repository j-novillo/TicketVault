package com.ticketvault.domain.usecase

import android.net.Uri
import com.ticketvault.camera.DocumentScannerGateway
import javax.inject.Inject

class ProcessScannedImageUseCase @Inject constructor(
    private val documentScannerGateway: DocumentScannerGateway,
) {
    suspend operator fun invoke(sourceUri: Uri): Result<String> =
        documentScannerGateway.persistScannedImage(sourceUri)
}
