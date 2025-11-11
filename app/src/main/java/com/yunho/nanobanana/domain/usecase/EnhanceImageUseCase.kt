package com.yunho.nanobanana.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.yunho.nanobanana.domain.model.EnhancementErrorReason
import com.yunho.nanobanana.domain.model.EnhancementResult
import com.yunho.nanobanana.domain.model.ImageEnhancementRequest
import com.yunho.nanobanana.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for enhancing images using AI
 * Handles enhancement logic and error mapping
 */
class EnhanceImageUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    /**
     * Enhance image with the specified parameters
     * Emits loading, success, or error states
     */
    operator fun invoke(request: ImageEnhancementRequest): Flow<EnhancementResult> = flow {
        try {
            emit(EnhancementResult.Loading(0f, "Preparing enhancement..."))
            
            // Validate image size to prevent computational constraints
            val imageSize = request.image.width * request.image.height
            val maxSize = if (request.targetRegion != null) {
                10_000_000 // 10MP for regional enhancement
            } else {
                4_000_000  // 4MP for full image enhancement
            }
            
            if (imageSize > maxSize) {
                emit(EnhancementResult.Error(
                    "Image is too large for enhancement (${imageSize / 1_000_000}MP). Maximum supported: ${maxSize / 1_000_000}MP",
                    EnhancementErrorReason.IMAGE_TOO_LARGE
                ))
                return@flow
            }
            
            emit(EnhancementResult.Loading(0.3f, "Enhancing image..."))
            
            val startTime = System.currentTimeMillis()
            
            // Perform enhancement through repository
            val enhancedImage = aiRepository.enhanceImage(
                image = request.image,
                enhancementType = request.enhancementType,
                targetRegion = request.targetRegion,
                intensity = request.intensity
            )
            
            val processingTime = System.currentTimeMillis() - startTime
            
            if (enhancedImage != null) {
                Log.d(TAG, "Enhancement successful in ${processingTime}ms")
                emit(EnhancementResult.Success(
                    enhancedImage = enhancedImage,
                    processingTimeMs = processingTime
                ))
            } else {
                emit(EnhancementResult.Error(
                    "Enhancement failed. Please try again or reduce image size.",
                    EnhancementErrorReason.UNKNOWN
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Enhancement error", e)
            
            val errorReason = when {
                e.message?.contains("quota", ignoreCase = true) == true ||
                e.message?.contains("limit", ignoreCase = true) == true -> 
                    EnhancementErrorReason.API_LIMIT_REACHED
                    
                e.message?.contains("network", ignoreCase = true) == true ||
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    EnhancementErrorReason.NETWORK_ERROR
                    
                e is OutOfMemoryError -> 
                    EnhancementErrorReason.COMPUTATIONAL_CONSTRAINT
                    
                else -> 
                    EnhancementErrorReason.UNKNOWN
            }
            
            val userMessage = when (errorReason) {
                EnhancementErrorReason.API_LIMIT_REACHED -> 
                    "API quota limit reached. Please try again later."
                EnhancementErrorReason.COMPUTATIONAL_CONSTRAINT -> 
                    "Enhancement requires too much processing power. Try a smaller region or lower intensity."
                EnhancementErrorReason.IMAGE_TOO_LARGE -> 
                    "Image is too large. Please select a smaller region."
                EnhancementErrorReason.NETWORK_ERROR -> 
                    "Network error occurred. Please check your connection and try again."
                EnhancementErrorReason.UNKNOWN -> 
                    "Enhancement failed: ${e.message ?: "Unknown error"}"
            }
            
            emit(EnhancementResult.Error(userMessage, errorReason))
        }
    }
    
    companion object {
        private const val TAG = "EnhanceImageUseCase"
    }
}
