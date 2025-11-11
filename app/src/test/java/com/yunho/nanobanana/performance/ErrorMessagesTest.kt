package com.yunho.nanobanana.performance

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ErrorMessages
 */
class ErrorMessagesTest {
    
    @Test
    fun testNetworkErrorMessage() {
        val exception = Exception("network error occurred")
        val message = ErrorMessages.getErrorMessage(exception)
        
        assertTrue(message.contains("Network error"))
        assertTrue(message.contains("internet connection"))
    }
    
    @Test
    fun testTimeoutErrorMessage() {
        val exception = Exception("Request timeout after 60 seconds")
        val message = ErrorMessages.getErrorMessage(exception)
        
        assertTrue(message.contains("timed out"))
    }
    
    @Test
    fun testApiKeyErrorMessage() {
        val exception = Exception("API key is invalid")
        val message = ErrorMessages.getErrorMessage(exception)
        
        assertTrue(message.contains("API key"))
    }
    
    @Test
    fun testCircuitBreakerErrorMessage() {
        val exception = RetryPolicy.CircuitBreakerException("Service unavailable")
        val message = ErrorMessages.getErrorMessage(exception)
        
        assertTrue(message.contains("temporarily unavailable"))
    }
    
    @Test
    fun testErrorTitle() {
        val networkException = Exception("network error")
        val title = ErrorMessages.getErrorTitle(networkException)
        
        assertEquals("Network Error", title)
    }
    
    @Test
    fun testDegradedModeMessage() {
        val message = ErrorMessages.getDegradedModeMessage(ErrorMessages.DegradationReason.LOW_MEMORY)
        
        assertTrue(message.contains("low memory"))
        assertTrue(message.contains("reduced quality"))
    }
}
