package com.yunho.nanobanana.async

import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for AITelemetry
 */
class AITelemetryTest {
    
    private lateinit var telemetry: AITelemetry
    
    @Before
    fun setup() {
        telemetry = AITelemetry()
    }
    
    @Test
    fun `recordRequestSuccess should update metrics correctly`() {
        // Given
        val requestId = "test-request-1"
        telemetry.recordRequestStart(requestId, "image_generation")
        
        // When
        Thread.sleep(100) // Simulate some processing time
        telemetry.recordRequestSuccess(requestId, 1024)
        
        // Then
        val report = telemetry.getTelemetryReport()
        assertEquals(1L, report.totalRequests)
        assertEquals(1L, report.successfulRequests)
        assertEquals(0L, report.failedRequests)
        assertEquals(100.0, report.successRate)
    }
    
    @Test
    fun `recordRequestFailure should update error counts`() {
        // Given
        val requestId = "test-request-2"
        telemetry.recordRequestStart(requestId, "text_generation")
        
        // When
        telemetry.recordRequestFailure(requestId, "network_error", "Connection timeout")
        
        // Then
        val report = telemetry.getTelemetryReport()
        assertEquals(1L, report.totalRequests)
        assertEquals(0L, report.successfulRequests)
        assertEquals(1L, report.failedRequests)
        assertEquals(0.0, report.successRate)
        assertTrue(report.errorDistribution.containsKey("network_error"))
        assertEquals(1, report.errorDistribution["network_error"])
    }
    
    @Test
    fun `recordRetry should track retry attempts`() {
        // Given
        val requestId = "test-request-3"
        telemetry.recordRequestStart(requestId, "combined_generation")
        
        // When
        telemetry.recordRetry(requestId, 1)
        telemetry.recordRetry(requestId, 2)
        
        // Then
        val metrics = telemetry.getRecentMetrics(10)
        val requestMetrics = metrics.find { it.requestId == requestId }
        assertEquals(2, requestMetrics?.retryCount)
    }
    
    @Test
    fun `frame monitoring should calculate drop rate correctly`() {
        // Given/When
        telemetry.recordFrame() // normal
        telemetry.recordFrame() // normal
        telemetry.recordFrameDrop() // drop
        telemetry.recordFrame() // normal
        
        // Then
        val report = telemetry.getTelemetryReport()
        assertEquals(4, report.totalFrames)
        assertEquals(25.0, report.frameDropRate) // 1 drop out of 4 frames = 25%
    }
    
    @Test
    fun `clear should reset all metrics`() {
        // Given
        telemetry.recordRequestStart("req1", "test")
        telemetry.recordRequestSuccess("req1", 100)
        telemetry.recordFrame()
        
        // When
        telemetry.clear()
        
        // Then
        val report = telemetry.getTelemetryReport()
        assertEquals(0L, report.totalRequests)
        assertEquals(0, report.totalFrames)
    }
    
    @Test
    fun `getRecentMetrics should return limited results`() {
        // Given
        repeat(20) { index ->
            val requestId = "req-$index"
            telemetry.recordRequestStart(requestId, "test")
            telemetry.recordRequestSuccess(requestId, 100)
        }
        
        // When
        val recentMetrics = telemetry.getRecentMetrics(5)
        
        // Then
        assertEquals(5, recentMetrics.size)
    }
}
