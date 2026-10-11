package com.ticketvault.ocr

/**
 * Wrapper sobre ML Kit Text Recognition v2.
 */
interface TextRecognitionGateway {
    suspend fun recognizeText(imagePath: String): Result<String>
}
