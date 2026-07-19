package com.ticketvault.domain.usecase

import com.ticketvault.camera.ImageValidationError

sealed class ProcessImageError : Exception() {
    data class Validation(val error: ImageValidationError) : ProcessImageError()

    data object EnhanceFailed : ProcessImageError()

    data object SaveFailed : ProcessImageError()
}
