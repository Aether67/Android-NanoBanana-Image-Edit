package com.yunho.nanobanana.domain.repository

import com.yunho.nanobanana.domain.model.AIGenerationResult
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
     * Cancel ongoing generation
     */
    suspend fun cancelGeneration()
}
