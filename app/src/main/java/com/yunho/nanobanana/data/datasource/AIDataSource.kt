package com.yunho.nanobanana.data.datasource

import android.graphics.Bitmap

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
}
