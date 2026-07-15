package com.ticketvault.ocr

/**
 * Wrapper sobre ML Kit Text Recognition v2.
 * Implementación pendiente (HU-2.1).
 */
interface TextRecognitionGateway {
    suspend fun recognizeText(imageUri: String): Result<String>
}
