package com.yunho.nanobanana.data.datasource

import android.graphics.Bitmap
import android.graphics.Rect
import com.yunho.nanobanana.domain.model.EnhancementType

/**
 * Data source interface for AI service operations
 * Abstracts the AI service implementation for testability
 */
interface AIDataSource {
    /**
     * Generate image using AI
     */
    suspend fun generateImage(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): Bitmap?
    
    /**
     * Generate text using AI
     */
    suspend fun generateText(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): String?
    
    /**
     * Generate both image and text
     */
    suspend fun generateCombined(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): Pair<Bitmap?, String?>
    
    /**
     * Enhance image using Gemini 2.5 Flash Image Preview model
     * Supports both full-image and region-specific enhancement
     */
    suspend fun enhanceImage(
        image: Bitmap,
        enhancementType: EnhancementType,
        targetRegion: Rect? = null,
        intensity: Float = 0.7f
    ): Bitmap?
}
