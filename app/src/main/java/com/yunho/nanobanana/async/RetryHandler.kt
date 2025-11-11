package com.yunho.nanobanana.async

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Retry configuration for AI requests
 */
data class RetryConfig(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1000,
    val maxDelayMs: Long = 10000,
    val multiplier: Double = 2.0,
    val jitterFactor: Double = 0.1
)

/**
 * Retry result wrapper
 */
sealed class RetryResult<T> {
    data class Success<T>(val value: T, val attemptNumber: Int) : RetryResult<T>()
    data class Failure<T>(
        val error: Exception,
        val attemptNumber: Int,
        val allErrors: List<Exception>
    ) : RetryResult<T>()
}

/**
 * Enhanced retry mechanism with exponential backoff
 * Implements adaptive retry logic for AI API failures
 */
class RetryHandler(
    private val config: RetryConfig = RetryConfig(),
    private val telemetry: AITelemetry? = null
) {
    
    /**
     * Execute operation with retry logic
     */
    suspend fun <T> executeWithRetry(
        requestId: String,
        operation: suspend (attemptNumber: Int) -> T
    ): RetryResult<T> {
        val errors = mutableListOf<Exception>()
        
        for (attempt in 1..config.maxAttempts) {
            try {
                Log.d(TAG, "Attempt $attempt/$config.maxAttempts for request: $requestId")
                
                val result = operation(attempt)
                
                if (attempt > 1) {
                    Log.i(TAG, "Request succeeded on attempt $attempt: $requestId")
                    telemetry?.recordRetry(requestId, attempt)
                }
                
                return RetryResult.Success(result, attempt)
                
            } catch (e: Exception) {
                errors.add(e)
                Log.w(TAG, "Attempt $attempt failed for $requestId: ${e.message}")
                
                if (attempt < config.maxAttempts) {
                    val delay = calculateBackoffDelay(attempt)
                    Log.d(TAG, "Retrying in ${delay}ms...")
                    telemetry?.recordRetry(requestId, attempt)
                    delay(delay)
                } else {
                    Log.e(TAG, "All $config.maxAttempts attempts failed for $requestId")
                }
            }
        }
        
        return RetryResult.Failure(
            error = errors.last(),
            attemptNumber = config.maxAttempts,
            allErrors = errors
        )
    }
    
    /**
     * Calculate exponential backoff delay with jitter
     */
    private fun calculateBackoffDelay(attemptNumber: Int): Long {
        // Exponential backoff: delay = initialDelay * (multiplier ^ (attemptNumber - 1))
        val exponentialDelay = config.initialDelayMs * 
            config.multiplier.pow(attemptNumber - 1).toLong()
        
        // Cap at maximum delay
        val cappedDelay = min(exponentialDelay, config.maxDelayMs)
        
        // Add jitter to prevent thundering herd
        val jitter = (cappedDelay * config.jitterFactor * Math.random()).toLong()
        
        return cappedDelay + jitter
    }
    
    /**
     * Determine if an error is retryable
     */
    fun isRetryable(error: Exception): Boolean {
        // Network errors, timeouts, and rate limits are retryable
        val message = error.message?.lowercase() ?: ""
        return when {
            message.contains("timeout") -> true
            message.contains("network") -> true
            message.contains("connection") -> true
            message.contains("rate limit") -> true
            message.contains("429") -> true // HTTP 429 Too Many Requests
            message.contains("503") -> true // HTTP 503 Service Unavailable
            message.contains("502") -> true // HTTP 502 Bad Gateway
            else -> false
        }
    }
    
    companion object {
        private const val TAG = "RetryHandler"
    }
}
