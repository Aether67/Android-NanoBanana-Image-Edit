package com.yunho.nanobanana.async

import android.graphics.Bitmap
import android.util.Log
import com.yunho.nanobanana.domain.model.AIGenerationResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Parallel AI processing pipeline
 * Orchestrates simultaneous image and text generation operations
 */
class ParallelAIPipeline(
    private val telemetry: AITelemetry,
    private val retryHandler: RetryHandler,
    private val taskPrioritization: TaskPrioritizationManager
) {
    
    /**
     * Process image and text generation in parallel
     */
    fun processParallel(
        imageOperation: suspend () -> Bitmap?,
        textOperation: suspend () -> String?,
        priority: TaskPriority = TaskPriority.NORMAL
    ): Flow<AIGenerationResult> = flow {
        val requestId = UUID.randomUUID().toString()
        telemetry.recordRequestStart(requestId, "parallel_generation")
        
        try {
            emit(AIGenerationResult.Loading(0.1f, "Starting parallel processing..."))
            
            // Launch both operations in parallel with coroutines
            coroutineScope {
                val imageDeferred = async(Dispatchers.Default) {
                    processWithPriority(priority) {
                        retryHandler.executeWithRetry("$requestId-image") { attemptNumber ->
                            Log.d(TAG, "Image generation attempt $attemptNumber")
                            emit(AIGenerationResult.Loading(0.3f, "Generating image..."))
                            imageOperation()
                        }
                    }
                }
                
                val textDeferred = async(Dispatchers.Default) {
                    processWithPriority(priority) {
                        retryHandler.executeWithRetry("$requestId-text") { attemptNumber ->
                            Log.d(TAG, "Text generation attempt $attemptNumber")
                            emit(AIGenerationResult.Loading(0.5f, "Generating text..."))
                            textOperation()
                        }
                    }
                }
                
                // Wait for both to complete
                emit(AIGenerationResult.Loading(0.7f, "Finalizing results..."))
                
                val imageResult = imageDeferred.await()
                val textResult = textDeferred.await()
                
                // Process results
                val image = when (imageResult) {
                    is RetryResult.Success -> imageResult.value
                    is RetryResult.Failure -> {
                        Log.e(TAG, "Image generation failed after retries", imageResult.error)
                        null
                    }
                }
                
                val text = when (textResult) {
                    is RetryResult.Success -> textResult.value
                    is RetryResult.Failure -> {
                        Log.e(TAG, "Text generation failed after retries", textResult.error)
                        null
                    }
                }
                
                if (image != null || text != null) {
                    telemetry.recordRequestSuccess(requestId, estimateDataSize(image, text))
                    emit(AIGenerationResult.Success(image = image, text = text))
                } else {
                    val error = (imageResult as? RetryResult.Failure)?.error 
                        ?: (textResult as? RetryResult.Failure)?.error
                        ?: Exception("Both operations failed")
                    telemetry.recordRequestFailure(requestId, "generation_failed", error.message)
                    emit(AIGenerationResult.Error("Failed to generate content: ${error.message}"))
                }
            }
            
        } catch (e: CancellationException) {
            Log.w(TAG, "Parallel processing cancelled: $requestId")
            telemetry.recordRequestFailure(requestId, "cancelled", "User cancelled operation")
            emit(AIGenerationResult.Error("Operation cancelled"))
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Parallel processing failed: $requestId", e)
            telemetry.recordRequestFailure(requestId, "exception", e.message)
            emit(AIGenerationResult.Error("Processing failed: ${e.message}"))
        }
    }
    
    /**
     * Process image generation with incremental updates
     */
    fun processImageWithProgress(
        operation: suspend (progress: (Float, String) -> Unit) -> Bitmap?
    ): Flow<AIGenerationResult> = flow {
        val requestId = UUID.randomUUID().toString()
        telemetry.recordRequestStart(requestId, "image_generation")
        
        try {
            emit(AIGenerationResult.Loading(0.0f, "Preparing image generation..."))
            
            val progressCallback: (Float, String) -> Unit = { progress, message ->
                // Emit progress updates asynchronously
                taskPrioritization.submitTask(TaskPriority.CRITICAL) {
                    emit(AIGenerationResult.Loading(progress, message))
                }
            }
            
            val result = retryHandler.executeWithRetry(requestId) { _ ->
                operation(progressCallback)
            }
            
            when (result) {
                is RetryResult.Success -> {
                    if (result.value != null) {
                        telemetry.recordRequestSuccess(requestId, estimateImageSize(result.value))
                        emit(AIGenerationResult.Success(image = result.value))
                    } else {
                        telemetry.recordRequestFailure(requestId, "null_result", "Operation returned null")
                        emit(AIGenerationResult.Error("Image generation returned no result"))
                    }
                }
                is RetryResult.Failure -> {
                    telemetry.recordRequestFailure(requestId, "retry_exhausted", result.error.message)
                    emit(AIGenerationResult.Error("Image generation failed: ${result.error.message}"))
                }
            }
            
        } catch (e: CancellationException) {
            telemetry.recordRequestFailure(requestId, "cancelled", "User cancelled")
            emit(AIGenerationResult.Error("Operation cancelled"))
            throw e
        } catch (e: Exception) {
            telemetry.recordRequestFailure(requestId, "exception", e.message)
            emit(AIGenerationResult.Error("Processing failed: ${e.message}"))
        }
    }
    
    /**
     * Process text generation with streaming updates
     */
    fun processTextWithStreaming(
        operation: suspend (chunk: (String) -> Unit) -> String?
    ): Flow<AIGenerationResult> = flow {
        val requestId = UUID.randomUUID().toString()
        telemetry.recordRequestStart(requestId, "text_generation")
        
        try {
            emit(AIGenerationResult.Loading(0.0f, "Starting text generation..."))
            
            val textBuilder = StringBuilder()
            val chunkCallback: (String) -> Unit = { chunk ->
                textBuilder.append(chunk)
                // Emit incremental text updates with high priority
                taskPrioritization.submitTask(TaskPriority.HIGH) {
                    emit(AIGenerationResult.Loading(
                        progress = 0.5f,
                        message = "Generating...",
                        reasoning = textBuilder.toString()
                    ))
                }
            }
            
            val result = retryHandler.executeWithRetry(requestId) { _ ->
                operation(chunkCallback)
            }
            
            when (result) {
                is RetryResult.Success -> {
                    if (result.value != null) {
                        telemetry.recordRequestSuccess(requestId, result.value.length)
                        emit(AIGenerationResult.Success(text = result.value))
                    } else {
                        telemetry.recordRequestFailure(requestId, "null_result", "Operation returned null")
                        emit(AIGenerationResult.Error("Text generation returned no result"))
                    }
                }
                is RetryResult.Failure -> {
                    telemetry.recordRequestFailure(requestId, "retry_exhausted", result.error.message)
                    emit(AIGenerationResult.Error("Text generation failed: ${result.error.message}"))
                }
            }
            
        } catch (e: CancellationException) {
            telemetry.recordRequestFailure(requestId, "cancelled", "User cancelled")
            emit(AIGenerationResult.Error("Operation cancelled"))
            throw e
        } catch (e: Exception) {
            telemetry.recordRequestFailure(requestId, "exception", e.message)
            emit(AIGenerationResult.Error("Processing failed: ${e.message}"))
        }
    }
    
    /**
     * Execute task with prioritization
     */
    private suspend fun <T> processWithPriority(
        priority: TaskPriority,
        operation: suspend () -> T
    ): T {
        return suspendCancellableCoroutine { continuation ->
            taskPrioritization.submitTask(priority) {
                try {
                    val result = operation()
                    continuation.resumeWith(Result.success(result))
                } catch (e: Exception) {
                    continuation.resumeWith(Result.failure(e))
                }
            }
        }
    }
    
    /**
     * Estimate data size for telemetry
     */
    private fun estimateDataSize(image: Bitmap?, text: String?): Int {
        var size = 0
        image?.let { size += estimateImageSize(it) }
        text?.let { size += it.length }
        return size
    }
    
    /**
     * Estimate image size in bytes
     */
    private fun estimateImageSize(bitmap: Bitmap): Int {
        return bitmap.width * bitmap.height * 4 // Assuming ARGB_8888
    }
    
    companion object {
        private const val TAG = "ParallelAIPipeline"
    }
}
