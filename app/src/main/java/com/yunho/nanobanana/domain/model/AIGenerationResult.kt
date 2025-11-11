package com.yunho.nanobanana.domain.model

import android.graphics.Bitmap

/**
 * Domain model for AI generation results
 * Sealed class for type-safe result handling
 */
sealed class AIGenerationResult {
    /**
     * Successful generation with optional image and text
     */
    data class Success(
        val image: Bitmap? = null,
        val text: String? = null,
        val reasoning: String? = null
    ) : AIGenerationResult() {
        init {
            require(image != null || text != null) { 
                "At least one of image or text must be non-null" 
            }
        }
    }
    
    /**
     * Generation failed with error message
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : AIGenerationResult()
    
    /**
     * Generation is in progress
     */
    data class Loading(
        val progress: Float = 0f,
        val message: String = "Generating...",
        val reasoning: String? = null
    ) : AIGenerationResult()
}
