package com.yunho.nanobanana.domain.repository

import android.graphics.Bitmap
import android.graphics.Rect
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.EnhancementType
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for AI operations
 * Defines contract for data operations following clean architecture
 */
interface AIRepository {
    /**
     * Generate AI content based on request
     * Returns a Flow for reactive updates
     */
    fun generateContent(request: ImageGenerationRequest): Flow<AIGenerationResult>
    
    /**
     * Enhance image using AI
     * Returns enhanced bitmap or null if enhancement fails
     */
    suspend fun enhanceImage(
        image: Bitmap,
        enhancementType: EnhancementType,
        targetRegion: Rect? = null,
        intensity: Float = 0.7f
    ): Bitmap?
    
    /**
     * Cancel ongoing generation
     */
    suspend fun cancelGeneration()
}
