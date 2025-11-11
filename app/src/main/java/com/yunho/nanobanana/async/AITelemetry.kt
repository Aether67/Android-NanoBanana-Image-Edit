package com.yunho.nanobanana.async

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Comprehensive telemetry system for AI operations
 * Tracks latency, errors, cache performance, and UI frame rates
 */
class AITelemetry {
    
    private val requestMetrics = ConcurrentHashMap<String, RequestMetrics>()
    private val errorCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val latencyHistogram = ConcurrentHashMap<LatencyBucket, AtomicInteger>()
    
    private var totalRequests = AtomicLong(0)
    private var successfulRequests = AtomicLong(0)
    private var failedRequests = AtomicLong(0)
    
    // Frame rate monitoring
    private val frameDropCounts = AtomicInteger(0)
    private val totalFrames = AtomicInteger(0)
    
    /**
     * Record the start of an AI request
     */
    fun recordRequestStart(requestId: String, requestType: String) {
        totalRequests.incrementAndGet()
        requestMetrics[requestId] = RequestMetrics(
            requestId = requestId,
            requestType = requestType,
            startTime = System.currentTimeMillis()
        )
        Log.d(TAG, "Request started: $requestId ($requestType)")
    }
    
    /**
     * Record successful completion of an AI request
     */
    fun recordRequestSuccess(requestId: String, dataSizeBytes: Int = 0) {
        val metrics = requestMetrics[requestId]
        if (metrics != null) {
            val latency = System.currentTimeMillis() - metrics.startTime
            metrics.endTime = System.currentTimeMillis()
            metrics.latencyMs = latency
            metrics.success = true
            metrics.dataSizeBytes = dataSizeBytes
            
            successfulRequests.incrementAndGet()
            recordLatency(latency)
            
            Log.d(TAG, "Request succeeded: $requestId (${latency}ms, ${dataSizeBytes}bytes)")
        }
    }
    
    /**
     * Record failed AI request
     */
    fun recordRequestFailure(requestId: String, errorType: String, errorMessage: String?) {
        val metrics = requestMetrics[requestId]
        if (metrics != null) {
            val latency = System.currentTimeMillis() - metrics.startTime
            metrics.endTime = System.currentTimeMillis()
            metrics.latencyMs = latency
            metrics.success = false
            metrics.errorType = errorType
            metrics.errorMessage = errorMessage
            
            failedRequests.incrementAndGet()
            errorCounts.getOrPut(errorType) { AtomicInteger(0) }.incrementAndGet()
            
            Log.e(TAG, "Request failed: $requestId ($errorType) - $errorMessage")
        }
    }
    
    /**
     * Record retry attempt
     */
    fun recordRetry(requestId: String, attemptNumber: Int) {
        val metrics = requestMetrics[requestId]
        if (metrics != null) {
            metrics.retryCount = attemptNumber
            Log.d(TAG, "Request retry: $requestId (attempt $attemptNumber)")
        }
    }
    
    /**
     * Record latency in histogram buckets
     */
    private fun recordLatency(latencyMs: Long) {
        val bucket = when {
            latencyMs < 500 -> LatencyBucket.UNDER_500MS
            latencyMs < 1000 -> LatencyBucket.UNDER_1S
            latencyMs < 2000 -> LatencyBucket.UNDER_2S
            latencyMs < 5000 -> LatencyBucket.UNDER_5S
            latencyMs < 10000 -> LatencyBucket.UNDER_10S
            else -> LatencyBucket.OVER_10S
        }
        latencyHistogram.getOrPut(bucket) { AtomicInteger(0) }.incrementAndGet()
    }
    
    /**
     * Record frame drop (UI performance monitoring)
     */
    fun recordFrameDrop() {
        frameDropCounts.incrementAndGet()
        totalFrames.incrementAndGet()
    }
    
    /**
     * Record normal frame
     */
    fun recordFrame() {
        totalFrames.incrementAndGet()
    }
    
    /**
     * Get comprehensive telemetry report
     */
    fun getTelemetryReport(): TelemetryReport {
        val avgLatency = if (successfulRequests.get() > 0) {
            requestMetrics.values
                .filter { it.success && it.latencyMs > 0 }
                .map { it.latencyMs }
                .average()
        } else 0.0
        
        val successRate = if (totalRequests.get() > 0) {
            (successfulRequests.get().toDouble() / totalRequests.get().toDouble()) * 100
        } else 0.0
        
        val frameDropRate = if (totalFrames.get() > 0) {
            (frameDropCounts.get().toDouble() / totalFrames.get().toDouble()) * 100
        } else 0.0
        
        return TelemetryReport(
            totalRequests = totalRequests.get(),
            successfulRequests = successfulRequests.get(),
            failedRequests = failedRequests.get(),
            successRate = successRate,
            averageLatencyMs = avgLatency,
            latencyDistribution = latencyHistogram.mapValues { it.value.get() },
            errorDistribution = errorCounts.mapValues { it.value.get() },
            frameDropRate = frameDropRate,
            totalFrames = totalFrames.get()
        )
    }
    
    /**
     * Get recent request metrics
     */
    fun getRecentMetrics(limit: Int = 10): List<RequestMetrics> {
        return requestMetrics.values
            .sortedByDescending { it.startTime }
            .take(limit)
    }
    
    /**
     * Clear all telemetry data
     */
    fun clear() {
        requestMetrics.clear()
        errorCounts.clear()
        latencyHistogram.clear()
        totalRequests.set(0)
        successfulRequests.set(0)
        failedRequests.set(0)
        frameDropCounts.set(0)
        totalFrames.set(0)
        Log.d(TAG, "Telemetry data cleared")
    }
    
    /**
     * Log telemetry summary
     */
    fun logSummary() {
        val report = getTelemetryReport()
        Log.i(TAG, """
            === AI Telemetry Summary ===
            Total Requests: ${report.totalRequests}
            Success Rate: ${"%.2f".format(report.successRate)}%
            Average Latency: ${"%.2f".format(report.averageLatencyMs)}ms
            Frame Drop Rate: ${"%.2f".format(report.frameDropRate)}%
            Errors: ${report.errorDistribution}
            ===========================
        """.trimIndent())
    }
    
    companion object {
        private const val TAG = "AITelemetry"
    }
}

/**
 * Metrics for a single request
 */
data class RequestMetrics(
    val requestId: String,
    val requestType: String,
    val startTime: Long,
    var endTime: Long = 0,
    var latencyMs: Long = 0,
    var success: Boolean = false,
    var errorType: String? = null,
    var errorMessage: String? = null,
    var retryCount: Int = 0,
    var dataSizeBytes: Int = 0
)

/**
 * Latency buckets for histogram
 */
private enum class LatencyBucket {
    UNDER_500MS,
    UNDER_1S,
    UNDER_2S,
    UNDER_5S,
    UNDER_10S,
    OVER_10S
}

/**
 * Comprehensive telemetry report
 */
data class TelemetryReport(
    val totalRequests: Long,
    val successfulRequests: Long,
    val failedRequests: Long,
    val successRate: Double,
    val averageLatencyMs: Double,
    val latencyDistribution: Map<LatencyBucket, Int>,
    val errorDistribution: Map<String, Int>,
    val frameDropRate: Double,
    val totalFrames: Int
)
