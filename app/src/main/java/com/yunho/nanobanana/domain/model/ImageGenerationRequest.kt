package com.yunho.nanobanana.domain.model

import android.graphics.Bitmap

/**
 * Domain model for image generation request
 * Immutable data class following clean architecture principles
 */
data class ImageGenerationRequest(
    val prompt: String,
    val bitmaps: List<Bitmap>,
    val outputMode: AIOutputMode = AIOutputMode.COMBINED,
    val parameters: AIParameters = AIParameters()
) {
    init {
        require(prompt.isNotBlank()) { "Prompt cannot be blank" }
    }
}

/**
 * AI generation parameters
 */
data class AIParameters(
    val creativityLevel: Float = 0.7f,
    val detailLevel: Int = 3,
    val reasoningDepth: Int = 2,
    val outputStyle: AIOutputStyle = AIOutputStyle.BALANCED
) {
    init {
        require(creativityLevel in 0f..1f) { "Creativity level must be between 0 and 1" }
        require(detailLevel in 1..5) { "Detail level must be between 1 and 5" }
        require(reasoningDepth in 1..3) { "Reasoning depth must be between 1 and 3" }
    }
}

/**
 * AI output modes
 */
enum class AIOutputMode {
    IMAGE_ONLY,
    TEXT_ONLY,
    COMBINED
}

/**
 * AI output styles for text generation
 */
enum class AIOutputStyle(val displayName: String) {
    FORMAL("Formal & Professional"),
    CASUAL("Casual & Friendly"),
    TECHNICAL("Technical & Precise"),
    CREATIVE("Creative & Vivid"),
    BALANCED("Balanced & Clear")
}
