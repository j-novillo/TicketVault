package com.ticketvault.domain.usecase

import android.net.Uri
import com.ticketvault.camera.ImageValidator
import com.ticketvault.camera.ProcessedTicketImage
import com.ticketvault.camera.TicketImageCleaner
import com.ticketvault.camera.TicketImageStorage
import javax.inject.Inject

class ProcessScannedImageUseCase @Inject constructor(
    private val imageValidator: ImageValidator,
    private val ticketImageStorage: TicketImageStorage,
    private val ticketImageCleaner: TicketImageCleaner,
) {
    suspend operator fun invoke(sourceUri: Uri): Result<ProcessedTicketImage> {
        imageValidator.validate(sourceUri).getOrElse { return Result.failure(it) }

        val originalPath = ticketImageStorage.saveOriginalFromUri(sourceUri)
            .getOrElse { return Result.failure(it) }

        val cleanedBitmap = ticketImageCleaner.enhance(sourceUri)
            .getOrElse { return Result.failure(it) }

        return try {
            ticketImageStorage.saveCleanedBitmap(cleanedBitmap)
                .map { cleanedPath ->
                    ProcessedTicketImage(
                        originalPath = originalPath,
                        cleanedPath = cleanedPath,
                    )
                }
        } finally {
            if (!cleanedBitmap.isRecycled) {
                cleanedBitmap.recycle()
            }
        }
    }
}
