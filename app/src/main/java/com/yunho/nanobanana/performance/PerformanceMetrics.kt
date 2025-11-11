package com.yunho.nanobanana.performance

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance metrics and telemetry tracker
 * Monitors AI response latencies, failure modes, and memory consumption
 */
class PerformanceMetrics private constructor() {
    
    private val apiCallCount = AtomicInteger(0)
    private val apiSuccessCount = AtomicInteger(0)
    private val apiFailureCount = AtomicInteger(0)
    private val cacheHitCount = AtomicInteger(0)
    private val cacheMissCount = AtomicInteger(0)
    
    private val apiLatencies = mutableListOf<Long>()
    private val errorTypes = ConcurrentHashMap<String, AtomicInteger>()
    private val memorySnapshots = mutableListOf<MemorySnapshot>()
    
    private var telemetryJob: Job? = null
    
    data class MemorySnapshot(
        val timestamp: Long,
        val usedMemoryMB: Long,
        val maxMemoryMB: Long,
        val freeMemoryMB: Long
    )
    
    /**
     * Starts periodic telemetry collection
     */
    fun startTelemetry(scope: CoroutineScope) {
        telemetryJob?.cancel()
        telemetryJob = scope.launch(DispatcherProvider.lowPriority) {
            while (isActive) {
                captureMemorySnapshot()
                delay(30_000) // Every 30 seconds
            }
        }
    }
    
    /**
     * Stops telemetry collection
     */
    fun stopTelemetry() {
        telemetryJob?.cancel()
        telemetryJob = null
    }
    
    /**
     * Records an API call attempt
     */
    fun recordApiCall() {
        apiCallCount.incrementAndGet()
    }
    
    /**
     * Records a successful API call with latency
     */
    fun recordApiSuccess(latencyMs: Long) {
        apiSuccessCount.incrementAndGet()
        synchronized(apiLatencies) {
            apiLatencies.add(latencyMs)
            // Keep only last 100 latencies
            if (apiLatencies.size > 100) {
                apiLatencies.removeAt(0)
            }
        }
        Log.d(TAG, "API call successful in ${latencyMs}ms")
    }
    
    /**
     * Records an API failure with error type
     */
    fun recordApiFailure(errorType: String, errorMessage: String) {
        apiFailureCount.incrementAndGet()
        errorTypes.getOrPut(errorType) { AtomicInteger(0) }.incrementAndGet()
        Log.w(TAG, "API call failed: $errorType - $errorMessage")
    }
    
    /**
     * Records a cache hit
     */
    fun recordCacheHit() {
        cacheHitCount.incrementAndGet()
        Log.d(TAG, "Cache hit")
    }
    
    /**
     * Records a cache miss
     */
    fun recordCacheMiss() {
        cacheMissCount.incrementAndGet()
    }
    
    /**
     * Captures current memory usage
     */
    private fun captureMemorySnapshot() {
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024
        val freeMemory = runtime.freeMemory() / 1024 / 1024
        
        synchronized(memorySnapshots) {
            memorySnapshots.add(
                MemorySnapshot(
                    timestamp = System.currentTimeMillis(),
                    usedMemoryMB = usedMemory,
                    maxMemoryMB = maxMemory,
                    freeMemoryMB = freeMemory
                )
            )
            // Keep only last 100 snapshots
            if (memorySnapshots.size > 100) {
                memorySnapshots.removeAt(0)
            }
        }
        
        Log.d(TAG, "Memory: Used=${usedMemory}MB, Max=${maxMemory}MB, Free=${freeMemory}MB")
    }
    
    /**
     * Gets comprehensive performance summary
     */
    fun getSummary(): PerformanceSummary {
        val avgLatency = synchronized(apiLatencies) {
            if (apiLatencies.isNotEmpty()) {
                apiLatencies.average().toLong()
            } else 0L
        }
        
        val p95Latency = synchronized(apiLatencies) {
            if (apiLatencies.isNotEmpty()) {
                val sorted = apiLatencies.sorted()
                sorted[(sorted.size * 0.95).toInt().coerceAtMost(sorted.size - 1)]
            } else 0L
        }
        
        val avgMemory = synchronized(memorySnapshots) {
            if (memorySnapshots.isNotEmpty()) {
                memorySnapshots.map { it.usedMemoryMB }.average().toLong()
            } else 0L
        }
        
        return PerformanceSummary(
            totalApiCalls = apiCallCount.get(),
            successfulCalls = apiSuccessCount.get(),
            failedCalls = apiFailureCount.get(),
            successRate = if (apiCallCount.get() > 0) {
                apiSuccessCount.get().toFloat() / apiCallCount.get()
            } else 0f,
            averageLatencyMs = avgLatency,
            p95LatencyMs = p95Latency,
            cacheHits = cacheHitCount.get(),
            cacheMisses = cacheMissCount.get(),
            cacheHitRate = if (cacheHitCount.get() + cacheMissCount.get() > 0) {
                cacheHitCount.get().toFloat() / (cacheHitCount.get() + cacheMissCount.get())
            } else 0f,
            averageMemoryMB = avgMemory,
            errorBreakdown = errorTypes.mapValues { it.value.get() }
        )
    }
    
    /**
     * Logs performance summary
     */
    fun logSummary() {
        val summary = getSummary()
        Log.i(TAG, """
            Performance Summary:
            - API Calls: ${summary.totalApiCalls} (${summary.successfulCalls} success, ${summary.failedCalls} failed)
            - Success Rate: ${(summary.successRate * 100).toInt()}%
            - Avg Latency: ${summary.averageLatencyMs}ms
            - P95 Latency: ${summary.p95LatencyMs}ms
            - Cache Hit Rate: ${(summary.cacheHitRate * 100).toInt()}%
            - Avg Memory: ${summary.averageMemoryMB}MB
            - Error Breakdown: ${summary.errorBreakdown}
        """.trimIndent())
    }
    
    data class PerformanceSummary(
        val totalApiCalls: Int,
        val successfulCalls: Int,
        val failedCalls: Int,
        val successRate: Float,
        val averageLatencyMs: Long,
        val p95LatencyMs: Long,
        val cacheHits: Int,
        val cacheMisses: Int,
        val cacheHitRate: Float,
        val averageMemoryMB: Long,
        val errorBreakdown: Map<String, Int>
    )
    
    companion object {
        private const val TAG = "PerformanceMetrics"
        
        @Volatile
        private var instance: PerformanceMetrics? = null
        
        fun getInstance(): PerformanceMetrics {
            return instance ?: synchronized(this) {
                instance ?: PerformanceMetrics().also { instance = it }
            }
        }
    }
}
