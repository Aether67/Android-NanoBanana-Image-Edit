package com.yunho.nanobanana.data.repository

import android.graphics.Bitmap
import android.graphics.Rect
import com.yunho.nanobanana.data.datasource.AIDataSource
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.EnhancementType
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.repository.AIRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Implementation of AIRepository
 * Manages AI generation requests and coordinates with data sources
 */
class AIRepositoryImpl @Inject constructor(
    private val aiDataSource: AIDataSource
) : AIRepository {
    
    private var currentJob: Job? = null
    
    override fun generateContent(request: ImageGenerationRequest): Flow<AIGenerationResult> = flow {
        try {
            // Emit loading state
            emit(AIGenerationResult.Loading(message = "Preparing generation..."))
            
            // Generate based on output mode
            val result = when (request.outputMode) {
                AIOutputMode.IMAGE_ONLY -> {
                    emit(AIGenerationResult.Loading(0.3f, "Generating image..."))
                    val image = aiDataSource.generateImage(
                        prompt = request.prompt,
                        bitmaps = request.bitmaps,
                        temperature = request.parameters.creativityLevel
                    )
                    
                    if (image != null) {
                        AIGenerationResult.Success(image = image)
                    } else {
                        AIGenerationResult.Error("Failed to generate image")
                    }
                }
                
                AIOutputMode.TEXT_ONLY -> {
                    emit(AIGenerationResult.Loading(0.3f, "Generating text..."))
                    val text = aiDataSource.generateText(
                        prompt = request.prompt,
                        bitmaps = request.bitmaps,
                        temperature = request.parameters.creativityLevel
                    )
                    
                    if (text != null) {
                        AIGenerationResult.Success(text = text)
                    } else {
                        AIGenerationResult.Error("Failed to generate text")
                    }
                }
                
                AIOutputMode.COMBINED -> {
                    emit(AIGenerationResult.Loading(0.3f, "Generating content..."))
                    val (image, text) = aiDataSource.generateCombined(
                        prompt = request.prompt,
                        bitmaps = request.bitmaps,
                        temperature = request.parameters.creativityLevel
                    )
                    
                    if (image != null || text != null) {
                        AIGenerationResult.Success(image = image, text = text)
                    } else {
                        AIGenerationResult.Error("Failed to generate content")
                    }
                }
            }
            
            // Emit final result
            emit(result)
            
        } catch (e: Exception) {
            emit(AIGenerationResult.Error("Generation failed: ${e.message}", e))
        }
    }
    
    override suspend fun enhanceImage(
        image: Bitmap,
        enhancementType: EnhancementType,
        targetRegion: Rect?,
        intensity: Float
    ): Bitmap? {
        return aiDataSource.enhanceImage(
            image = image,
            enhancementType = enhancementType,
            targetRegion = targetRegion,
            intensity = intensity
        )
    }
    
    override suspend fun cancelGeneration() {
        currentJob?.cancel()
        currentJob = null
    }
}
