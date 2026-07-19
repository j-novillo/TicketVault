package com.ticketvault.domain.usecase

import android.net.Uri
import com.ticketvault.camera.ImageValidationError
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
        imageValidator.validate(sourceUri).getOrElse { error ->
            val processError = if (error is ImageValidationError) {
                ProcessImageError.Validation(error)
            } else {
                ProcessImageError.Validation(ImageValidationError.Unreadable)
            }
            return Result.failure(processError)
        }

        val originalPath = ticketImageStorage.saveOriginalFromUri(sourceUri)
            .getOrElse { return Result.failure(ProcessImageError.SaveFailed) }

        val cleanedBitmap = ticketImageCleaner.enhance(sourceUri)
            .getOrElse {
                ticketImageStorage.deleteIfExists(originalPath)
                return Result.failure(ProcessImageError.EnhanceFailed)
            }

        return try {
            ticketImageStorage.saveCleanedBitmap(cleanedBitmap)
                .fold(
                    onSuccess = { cleanedPath ->
                        Result.success(
                            ProcessedTicketImage(
                                originalPath = originalPath,
                                cleanedPath = cleanedPath,
                            ),
                        )
                    },
                    onFailure = {
                        ticketImageStorage.deleteIfExists(originalPath)
                        Result.failure(ProcessImageError.SaveFailed)
                    },
                )
        } finally {
            if (!cleanedBitmap.isRecycled) {
                cleanedBitmap.recycle()
            }
        }
    }
}
