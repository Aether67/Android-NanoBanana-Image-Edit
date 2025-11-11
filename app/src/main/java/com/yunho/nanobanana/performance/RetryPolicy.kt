package com.yunho.nanobanana.performance

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

/**
 * Enhanced retry mechanism with exponential backoff and circuit breaker pattern
 */
class RetryPolicy {
    
    private var consecutiveFailures = 0
    private var circuitBreakerOpen = false
    private var circuitBreakerOpenTime = 0L
    
    /**
     * Executes operation with retry logic and exponential backoff
     */
    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        backoffMultiplier: Double = 2.0,
        operation: suspend (attempt: Int) -> T?
    ): Result<T> {
        // Check circuit breaker
        if (circuitBreakerOpen) {
            val timeSinceOpen = System.currentTimeMillis() - circuitBreakerOpenTime
            if (timeSinceOpen < CIRCUIT_BREAKER_TIMEOUT_MS) {
                Log.w(TAG, "Circuit breaker is open, rejecting request")
                return Result.failure(
                    CircuitBreakerException("Service temporarily unavailable. Please try again later.")
                )
            } else {
                // Try to close circuit breaker
                Log.i(TAG, "Circuit breaker timeout expired, attempting to close")
                circuitBreakerOpen = false
                consecutiveFailures = 0
            }
        }
        
        var currentDelay = initialDelayMs
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                Log.d(TAG, "Attempt ${attempt + 1} of $maxAttempts")
                val result = operation(attempt + 1)
                
                if (result != null) {
                    // Success - reset failure counter
                    consecutiveFailures = 0
                    Log.i(TAG, "Operation successful on attempt ${attempt + 1}")
                    return Result.success(result)
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt ${attempt + 1} failed: ${e.message}", e)
                consecutiveFailures++
                
                // Open circuit breaker if too many failures
                if (consecutiveFailures >= CIRCUIT_BREAKER_THRESHOLD) {
                    circuitBreakerOpen = true
                    circuitBreakerOpenTime = System.currentTimeMillis()
                    Log.e(TAG, "Circuit breaker opened after $consecutiveFailures consecutive failures")
                    return Result.failure(
                        CircuitBreakerException("Service temporarily unavailable due to repeated failures")
                    )
                }
            }
            
            // Wait before retry (except on last attempt)
            if (attempt < maxAttempts - 1) {
                val jitter = (Math.random() * 500).toLong() // Add jitter to prevent thundering herd
                val delayWithJitter = currentDelay + jitter
                Log.d(TAG, "Waiting ${delayWithJitter}ms before retry...")
                delay(delayWithJitter)
                
                // Calculate next delay with exponential backoff
                currentDelay = min(
                    (currentDelay * backoffMultiplier).toLong(),
                    maxDelayMs
                )
            }
        }
        
        Log.e(TAG, "All $maxAttempts attempts failed")
        return Result.failure(
            lastException ?: Exception("Operation failed after $maxAttempts attempts")
        )
    }
    
    /**
     * Resets the circuit breaker manually
     */
    fun resetCircuitBreaker() {
        circuitBreakerOpen = false
        consecutiveFailures = 0
        Log.i(TAG, "Circuit breaker manually reset")
    }
    
    /**
     * Gets circuit breaker status
     */
    fun isCircuitBreakerOpen(): Boolean = circuitBreakerOpen
    
    class CircuitBreakerException(message: String) : Exception(message)
    
    companion object {
        private const val TAG = "RetryPolicy"
        private const val CIRCUIT_BREAKER_THRESHOLD = 5 // Open after 5 consecutive failures
        private const val CIRCUIT_BREAKER_TIMEOUT_MS = 60_000L // 1 minute
    }
}
