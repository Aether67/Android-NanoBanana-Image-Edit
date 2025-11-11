package com.yunho.nanobanana.async

import android.util.Log

/**
 * Degradation mode levels
 */
enum class DegradationMode {
    NORMAL,         // Full features, no degradation
    REDUCED,        // Reduced quality but full features
    MINIMAL,        // Minimal features, low quality
    EMERGENCY       // Emergency mode, only critical operations
}

/**
 * Feature configuration based on degradation mode
 */
data class FeatureConfig(
    val mode: DegradationMode,
    val enableParallelProcessing: Boolean,
    val enableCache: Boolean,
    val enableRealTimeReasoning: Boolean,
    val maxImageResolution: Int,
    val compressionQuality: Int,
    val maxConcurrentOperations: Int,
    val enableRetry: Boolean,
    val maxRetryAttempts: Int
)

/**
 * Graceful degradation strategy manager
 * Automatically adjusts feature richness based on device constraints and API limits
 */
class GracefulDegradationManager(
    private val deviceCapabilities: DeviceCapabilities,
    private val deviceCapabilityDetector: DeviceCapabilityDetector
) {
    
    @Volatile
    private var currentMode = DegradationMode.NORMAL
    
    private var consecutiveApiFailures = 0
    private var consecutiveMemoryWarnings = 0
    
    /**
     * Get current feature configuration based on degradation mode
     */
    fun getCurrentConfig(): FeatureConfig {
        return getConfigForMode(currentMode)
    }
    
    /**
     * Evaluate and adjust degradation mode based on current conditions
     */
    fun evaluateAndAdjust(): DegradationMode {
        val previousMode = currentMode
        
        // Check various conditions
        val isMemoryPressure = deviceCapabilityDetector.isUnderMemoryPressure()
        val isLowEndDevice = deviceCapabilities.performanceTier == DevicePerformanceTier.LOW_END
        val hasApiIssues = consecutiveApiFailures >= 3
        
        // Determine appropriate mode
        currentMode = when {
            // Emergency mode: Severe constraints
            (isMemoryPressure && consecutiveMemoryWarnings >= 3) || consecutiveApiFailures >= 5 -> {
                DegradationMode.EMERGENCY
            }
            
            // Minimal mode: Significant constraints
            (isMemoryPressure && consecutiveMemoryWarnings >= 2) || hasApiIssues -> {
                DegradationMode.MINIMAL
            }
            
            // Reduced mode: Some constraints
            isMemoryPressure || isLowEndDevice || consecutiveApiFailures >= 1 -> {
                DegradationMode.REDUCED
            }
            
            // Normal mode: No constraints
            else -> DegradationMode.NORMAL
        }
        
        if (currentMode != previousMode) {
            Log.i(TAG, "Degradation mode changed: $previousMode -> $currentMode")
            notifyModeChange(currentMode)
        }
        
        return currentMode
    }
    
    /**
     * Record API failure
     */
    fun recordApiFailure(errorMessage: String) {
        consecutiveApiFailures++
        Log.w(TAG, "API failure recorded (count: $consecutiveApiFailures): $errorMessage")
        
        // Check if rate limited
        if (errorMessage.contains("rate limit", ignoreCase = true) || 
            errorMessage.contains("429")) {
            // Immediately degrade for rate limiting
            if (currentMode == DegradationMode.NORMAL) {
                currentMode = DegradationMode.REDUCED
                notifyModeChange(currentMode)
            }
        }
        
        evaluateAndAdjust()
    }
    
    /**
     * Record API success (recovery)
     */
    fun recordApiSuccess() {
        if (consecutiveApiFailures > 0) {
            consecutiveApiFailures = max(0, consecutiveApiFailures - 1)
            Log.d(TAG, "API success recorded (failure count: $consecutiveApiFailures)")
            evaluateAndAdjust()
        }
    }
    
    /**
     * Record memory warning
     */
    fun recordMemoryWarning() {
        consecutiveMemoryWarnings++
        Log.w(TAG, "Memory warning recorded (count: $consecutiveMemoryWarnings)")
        evaluateAndAdjust()
    }
    
    /**
     * Record memory recovery
     */
    fun recordMemoryRecovery() {
        if (consecutiveMemoryWarnings > 0) {
            consecutiveMemoryWarnings = max(0, consecutiveMemoryWarnings - 1)
            Log.d(TAG, "Memory recovery recorded (warning count: $consecutiveMemoryWarnings)")
            evaluateAndAdjust()
        }
    }
    
    /**
     * Force specific degradation mode
     */
    fun forceDegradationMode(mode: DegradationMode) {
        val previousMode = currentMode
        currentMode = mode
        Log.i(TAG, "Degradation mode forced: $previousMode -> $currentMode")
        notifyModeChange(currentMode)
    }
    
    /**
     * Reset to normal mode
     */
    fun reset() {
        consecutiveApiFailures = 0
        consecutiveMemoryWarnings = 0
        currentMode = DegradationMode.NORMAL
        Log.i(TAG, "Degradation manager reset to NORMAL mode")
    }
    
    /**
     * Get feature configuration for specific mode
     */
    private fun getConfigForMode(mode: DegradationMode): FeatureConfig {
        return when (mode) {
            DegradationMode.NORMAL -> FeatureConfig(
                mode = mode,
                enableParallelProcessing = true,
                enableCache = true,
                enableRealTimeReasoning = true,
                maxImageResolution = 2048,
                compressionQuality = deviceCapabilities.recommendedImageQuality,
                maxConcurrentOperations = deviceCapabilities.maxConcurrentOperations,
                enableRetry = true,
                maxRetryAttempts = 3
            )
            
            DegradationMode.REDUCED -> FeatureConfig(
                mode = mode,
                enableParallelProcessing = true,
                enableCache = true,
                enableRealTimeReasoning = false, // Disable real-time reasoning
                maxImageResolution = 1536,
                compressionQuality = max(deviceCapabilities.recommendedImageQuality - 10, 70),
                maxConcurrentOperations = max(deviceCapabilities.maxConcurrentOperations - 1, 1),
                enableRetry = true,
                maxRetryAttempts = 2
            )
            
            DegradationMode.MINIMAL -> FeatureConfig(
                mode = mode,
                enableParallelProcessing = false, // Sequential processing only
                enableCache = true,
                enableRealTimeReasoning = false,
                maxImageResolution = 1024,
                compressionQuality = 70,
                maxConcurrentOperations = 1,
                enableRetry = true,
                maxRetryAttempts = 1
            )
            
            DegradationMode.EMERGENCY -> FeatureConfig(
                mode = mode,
                enableParallelProcessing = false,
                enableCache = false, // Disable cache to save memory
                enableRealTimeReasoning = false,
                maxImageResolution = 512,
                compressionQuality = 60,
                maxConcurrentOperations = 1,
                enableRetry = false, // No retries in emergency mode
                maxRetryAttempts = 0
            )
        }
    }
    
    /**
     * Notify about mode change (can be extended to emit events)
     */
    private fun notifyModeChange(newMode: DegradationMode) {
        val config = getConfigForMode(newMode)
        Log.i(TAG, """
            === Degradation Mode Active: $newMode ===
            Parallel Processing: ${config.enableParallelProcessing}
            Cache: ${config.enableCache}
            Real-time Reasoning: ${config.enableRealTimeReasoning}
            Max Resolution: ${config.maxImageResolution}px
            Compression Quality: ${config.compressionQuality}%
            Max Concurrent Ops: ${config.maxConcurrentOperations}
            Retry: ${config.enableRetry} (max ${config.maxRetryAttempts})
            ========================================
        """.trimIndent())
    }
    
    /**
     * Get user-friendly message for current mode
     */
    fun getUserMessage(): String? {
        return when (currentMode) {
            DegradationMode.NORMAL -> null
            DegradationMode.REDUCED -> "Performance mode: Reduced quality for better responsiveness"
            DegradationMode.MINIMAL -> "Low resource mode: Limited features to optimize performance"
            DegradationMode.EMERGENCY -> "Emergency mode: Minimal features active due to resource constraints"
        }
    }
    
    private fun max(a: Int, b: Int) = if (a > b) a else b
    
    companion object {
        private const val TAG = "GracefulDegradation"
    }
}
