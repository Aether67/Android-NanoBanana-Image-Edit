package com.yunho.nanobanana.async

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for RetryHandler
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RetryHandlerTest {
    
    private lateinit var retryHandler: RetryHandler
    private lateinit var telemetry: AITelemetry
    
    @Before
    fun setup() {
        telemetry = AITelemetry()
        retryHandler = RetryHandler(
            config = RetryConfig(
                maxAttempts = 3,
                initialDelayMs = 100,
                maxDelayMs = 1000
            ),
            telemetry = telemetry
        )
    }
    
    @Test
    fun `executeWithRetry should succeed on first attempt`() = runTest {
        // Given
        var callCount = 0
        val operation: suspend (Int) -> String = { 
            callCount++
            "Success"
        }
        
        // When
        val result = retryHandler.executeWithRetry("test-request", operation)
        
        // Then
        assertTrue(result is RetryResult.Success)
        assertEquals("Success", (result as RetryResult.Success).value)
        assertEquals(1, result.attemptNumber)
        assertEquals(1, callCount)
    }
    
    @Test
    fun `executeWithRetry should retry on failure and eventually succeed`() = runTest {
        // Given
        var callCount = 0
        val operation: suspend (Int) -> String = { attempt ->
            callCount++
            if (attempt < 3) {
                throw Exception("Temporary failure")
            }
            "Success"
        }
        
        // When
        val result = retryHandler.executeWithRetry("test-request", operation)
        
        // Then
        assertTrue(result is RetryResult.Success)
        assertEquals("Success", (result as RetryResult.Success).value)
        assertEquals(3, result.attemptNumber)
        assertEquals(3, callCount)
    }
    
    @Test
    fun `executeWithRetry should fail after max attempts`() = runTest {
        // Given
        var callCount = 0
        val operation: suspend (Int) -> String = { 
            callCount++
            throw Exception("Permanent failure")
        }
        
        // When
        val result = retryHandler.executeWithRetry("test-request", operation)
        
        // Then
        assertTrue(result is RetryResult.Failure)
        assertEquals(3, (result as RetryResult.Failure).attemptNumber)
        assertEquals(3, result.allErrors.size)
        assertEquals(3, callCount)
    }
    
    @Test
    fun `isRetryable should identify retryable errors`() {
        // Test various retryable error messages
        assertTrue(retryHandler.isRetryable(Exception("Connection timeout")))
        assertTrue(retryHandler.isRetryable(Exception("Network error")))
        assertTrue(retryHandler.isRetryable(Exception("HTTP 429 rate limit")))
        assertTrue(retryHandler.isRetryable(Exception("503 Service Unavailable")))
    }
}
