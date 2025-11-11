package com.yunho.nanobanana.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.yunho.nanobanana.async.*
import com.yunho.nanobanana.data.cache.AIContentCache
import com.yunho.nanobanana.data.datasource.AIDataSource
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.repository.AIRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Enhanced implementation of AIRepository with async architecture
 * Implements parallel processing, caching, retry logic, and adaptive resource management
 */
class EnhancedAIRepositoryImpl @Inject constructor(
    private val aiDataSource: AIDataSource,
    private val context: Context
) : AIRepository {
    
    // Initialize async components
    private val telemetry = AITelemetry()
    private val deviceCapability = DeviceCapabilityDetector(context)
    private val capabilities = deviceCapability.detectCapabilities()
    
    // Configure cache based on device capabilities
    private val cache = AIContentCache(
        maxImageCacheSize = capabilities.recommendedCacheSize,
        maxTextCacheSize = capabilities.recommendedCacheSize * 2,
        compressionQuality = capabilities.recommendedImageQuality
    )
    
    // Configure retry handler
    private val retryHandler = RetryHandler(
        config = RetryConfig(
            maxAttempts = 3,
            initialDelayMs = 1000,
            maxDelayMs = 10000
        ),
        telemetry = telemetry
    )
    
    // Configure task prioritization based on device capabilities
    private val taskPrioritization = TaskPrioritizationManager(
        maxConcurrentTasks = capabilities.maxConcurrentOperations
    )
    
    // Parallel processing pipeline
    private val pipeline = ParallelAIPipeline(
        telemetry = telemetry,
        retryHandler = retryHandler,
        taskPrioritization = taskPrioritization
    )
    
    override fun generateContent(request: ImageGenerationRequest): Flow<AIGenerationResult> = flow {
        val cacheKey = generateCacheKey(request)
        
        // Check if device is under memory pressure
        if (deviceCapability.isUnderMemoryPressure()) {
            Log.w(TAG, "Device under memory pressure - enabling degradation mode")
            emit(AIGenerationResult.Loading(
                progress = 0.0f,
                message = "Device resources constrained - optimizing settings..."
            ))
        }
        
        // Try cache first
        val cachedResult = checkCache(cacheKey, request.outputMode)
        if (cachedResult != null) {
            Log.i(TAG, "Cache hit for request: $cacheKey")
            emit(cachedResult)
            return@flow
        }
        
        // Determine processing strategy based on output mode
        val resultFlow = when (request.outputMode) {
            AIOutputMode.IMAGE_ONLY -> {
                generateImageOnly(request, cacheKey)
            }
            
            AIOutputMode.TEXT_ONLY -> {
                generateTextOnly(request, cacheKey)
            }
            
            AIOutputMode.COMBINED -> {
                generateCombined(request, cacheKey)
            }
        }
        
        // Emit all results from the flow
        resultFlow.collect { result ->
            emit(result)
            
            // Cache successful results
            if (result is AIGenerationResult.Success) {
                cacheResult(cacheKey, result)
            }
        }
        
        // Log telemetry summary
        telemetry.logSummary()
    }
    
    /**
     * Generate image only
     */
    private fun generateImageOnly(
        request: ImageGenerationRequest,
        cacheKey: String
    ): Flow<AIGenerationResult> {
        return pipeline.processImageWithProgress { progress ->
            val adjustedTemperature = adjustTemperatureForDevice(request.parameters.creativityLevel)
            
            progress(0.3f, "Generating image...")
            aiDataSource.generateImage(
                prompt = request.prompt,
                bitmaps = request.bitmaps,
                temperature = adjustedTemperature
            )
        }
    }
    
    /**
     * Generate text only
     */
    private fun generateTextOnly(
        request: ImageGenerationRequest,
        cacheKey: String
    ): Flow<AIGenerationResult> {
        return pipeline.processTextWithStreaming { chunk ->
            val adjustedTemperature = adjustTemperatureForDevice(request.parameters.creativityLevel)
            
            aiDataSource.generateText(
                prompt = request.prompt,
                bitmaps = request.bitmaps,
                temperature = adjustedTemperature
            )
        }
    }
    
    /**
     * Generate image and text in parallel
     */
    private fun generateCombined(
        request: ImageGenerationRequest,
        cacheKey: String
    ): Flow<AIGenerationResult> {
        val adjustedTemperature = adjustTemperatureForDevice(request.parameters.creativityLevel)
        
        return pipeline.processParallel(
            imageOperation = {
                aiDataSource.generateImage(
                    prompt = request.prompt,
                    bitmaps = request.bitmaps,
                    temperature = adjustedTemperature
                )
            },
            textOperation = {
                aiDataSource.generateText(
                    prompt = request.prompt,
                    bitmaps = request.bitmaps,
                    temperature = adjustedTemperature
                )
            },
            priority = TaskPriority.NORMAL
        )
    }
    
    /**
     * Check cache for existing results
     */
    private fun checkCache(cacheKey: String, outputMode: AIOutputMode): AIGenerationResult? {
        return when (outputMode) {
            AIOutputMode.IMAGE_ONLY -> {
                cache.getImage(cacheKey)?.let { 
                    AIGenerationResult.Success(image = it)
                }
            }
            
            AIOutputMode.TEXT_ONLY -> {
                cache.getText(cacheKey)?.let { 
                    AIGenerationResult.Success(text = it)
                }
            }
            
            AIOutputMode.COMBINED -> {
                val image = cache.getImage(cacheKey)
                val text = cache.getText(cacheKey)
                if (image != null || text != null) {
                    AIGenerationResult.Success(image = image, text = text)
                } else null
            }
        }
    }
    
    /**
     * Cache successful results
     */
    private fun cacheResult(cacheKey: String, result: AIGenerationResult.Success) {
        result.image?.let { cache.putImage(cacheKey, it) }
        result.text?.let { cache.putText(cacheKey, it) }
    }
    
    /**
     * Generate cache key from request
     */
    private fun generateCacheKey(request: ImageGenerationRequest): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val data = "${request.prompt}${request.outputMode}${request.parameters.creativityLevel}"
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(16)
    }
    
    /**
     * Adjust temperature based on device capabilities
     */
    private fun adjustTemperatureForDevice(requestedTemperature: Float): Float {
        return when (capabilities.performanceTier) {
            DevicePerformanceTier.LOW_END -> {
                // Lower temperature slightly to reduce processing complexity
                (requestedTemperature * 0.9f).coerceIn(0.1f, 1.0f)
            }
            else -> requestedTemperature
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats() = cache.getStats()
    
    /**
     * Get telemetry report
     */
    fun getTelemetryReport() = telemetry.getTelemetryReport()
    
    /**
     * Get device capabilities
     */
    fun getDeviceCapabilities() = capabilities
    
    /**
     * Clear cache
     */
    fun clearCache() {
        cache.clear()
    }
    
    override suspend fun cancelGeneration() {
        taskPrioritization.clearQueue()
        Log.d(TAG, "Generation cancelled")
    }
    
    companion object {
        private const val TAG = "EnhancedAIRepository"
    }
}
