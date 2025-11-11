package com.yunho.nanobanana.domain.model

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * Domain model for image enhancement request
 * Supports both full-image and region-specific enhancement
 */
data class ImageEnhancementRequest(
    val image: Bitmap,
    val enhancementType: EnhancementType = EnhancementType.DETAIL_SHARPEN,
    val targetRegion: Rect? = null, // null means full image
    val intensity: Float = 0.7f
) {
    init {
        require(intensity in 0f..1f) { "Intensity must be between 0 and 1" }
    }
}

/**
 * Types of enhancement operations
 */
enum class EnhancementType(val displayName: String, val prompt: String) {
    DETAIL_SHARPEN(
        "Detail Sharpening",
        "Enhance and sharpen fine details, textures, and edges in this image. " +
        "Preserve natural image fidelity and avoid over-sharpening or introducing artifacts. " +
        "Focus on refining clarity while maintaining realistic appearance."
    ),
    SUPER_RESOLUTION(
        "Super Resolution",
        "Apply super-resolution enhancement to increase image detail and clarity. " +
        "Intelligently restore fine textures and sharpen edges while preserving natural appearance. " +
        "Avoid creating artificial patterns or artifacts."
    ),
    LOCALIZED_ENHANCE(
        "Localized Enhancement",
        "Enhance the specified region with detail restoration and sharpening. " +
        "Ensure seamless integration with surrounding areas to prevent discontinuities. " +
        "Maintain consistency with overall image style and quality."
    )
}

/**
 * Result of enhancement operation
 */
sealed class EnhancementResult {
    data class Success(
        val enhancedImage: Bitmap,
        val processingTimeMs: Long = 0
    ) : EnhancementResult()
    
    data class Error(
        val message: String,
        val reason: EnhancementErrorReason
    ) : EnhancementResult()
    
    data class Loading(
        val progress: Float = 0f,
        val message: String = "Enhancing image..."
    ) : EnhancementResult()
}

/**
 * Reasons for enhancement failures
 */
enum class EnhancementErrorReason {
    API_LIMIT_REACHED,
    COMPUTATIONAL_CONSTRAINT,
    IMAGE_TOO_LARGE,
    NETWORK_ERROR,
    UNKNOWN
}
