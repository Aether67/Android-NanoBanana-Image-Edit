package com.yunho.nanobanana.async

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Device performance tier classification
 */
enum class DevicePerformanceTier {
    HIGH_END,    // Flagship devices with ample resources
    MID_RANGE,   // Standard devices with moderate resources
    LOW_END      // Budget devices with limited resources
}

/**
 * Device capability information
 */
data class DeviceCapabilities(
    val performanceTier: DevicePerformanceTier,
    val totalMemoryMB: Long,
    val availableMemoryMB: Long,
    val cpuCores: Int,
    val isLowMemoryDevice: Boolean,
    val recommendedImageQuality: Int,
    val recommendedCacheSize: Int,
    val maxConcurrentOperations: Int
)

/**
 * Device capability detector for adaptive resource management
 * Detects device capabilities and recommends optimal settings
 */
class DeviceCapabilityDetector(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    
    /**
     * Detect device capabilities
     */
    fun detectCapabilities(): DeviceCapabilities {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val totalMemoryMB = memoryInfo.totalMem / (1024 * 1024)
        val availableMemoryMB = memoryInfo.availMem / (1024 * 1024)
        val cpuCores = Runtime.getRuntime().availableProcessors()
        val isLowMemory = activityManager.isLowRamDevice
        
        // Determine performance tier based on multiple factors
        val performanceTier = determinePerformanceTier(
            totalMemoryMB = totalMemoryMB,
            cpuCores = cpuCores,
            isLowMemory = isLowMemory
        )
        
        val capabilities = DeviceCapabilities(
            performanceTier = performanceTier,
            totalMemoryMB = totalMemoryMB,
            availableMemoryMB = availableMemoryMB,
            cpuCores = cpuCores,
            isLowMemoryDevice = isLowMemory,
            recommendedImageQuality = getRecommendedImageQuality(performanceTier),
            recommendedCacheSize = getRecommendedCacheSize(performanceTier, totalMemoryMB),
            maxConcurrentOperations = getMaxConcurrentOperations(performanceTier, cpuCores)
        )
        
        logCapabilities(capabilities)
        return capabilities
    }
    
    /**
     * Determine device performance tier
     */
    private fun determinePerformanceTier(
        totalMemoryMB: Long,
        cpuCores: Int,
        isLowMemory: Boolean
    ): DevicePerformanceTier {
        return when {
            // Low-end: Less than 3GB RAM or marked as low memory device
            isLowMemory || totalMemoryMB < 3072 -> DevicePerformanceTier.LOW_END
            
            // High-end: 8GB+ RAM and 8+ CPU cores
            totalMemoryMB >= 8192 && cpuCores >= 8 -> DevicePerformanceTier.HIGH_END
            
            // High-end: 6GB+ RAM and 6+ CPU cores
            totalMemoryMB >= 6144 && cpuCores >= 6 -> DevicePerformanceTier.HIGH_END
            
            // Mid-range: Everything else
            else -> DevicePerformanceTier.MID_RANGE
        }
    }
    
    /**
     * Get recommended image quality based on performance tier
     */
    private fun getRecommendedImageQuality(tier: DevicePerformanceTier): Int {
        return when (tier) {
            DevicePerformanceTier.HIGH_END -> 95    // Maximum quality
            DevicePerformanceTier.MID_RANGE -> 85   // Good quality
            DevicePerformanceTier.LOW_END -> 75     // Acceptable quality
        }
    }
    
    /**
     * Get recommended cache size based on available memory
     */
    private fun getRecommendedCacheSize(tier: DevicePerformanceTier, totalMemoryMB: Long): Int {
        return when (tier) {
            DevicePerformanceTier.HIGH_END -> 30    // Cache up to 30 items
            DevicePerformanceTier.MID_RANGE -> 20   // Cache up to 20 items
            DevicePerformanceTier.LOW_END -> 10     // Cache up to 10 items
        }
    }
    
    /**
     * Get maximum concurrent operations
     */
    private fun getMaxConcurrentOperations(tier: DevicePerformanceTier, cpuCores: Int): Int {
        return when (tier) {
            DevicePerformanceTier.HIGH_END -> minOf(cpuCores, 4)  // Up to 4 concurrent
            DevicePerformanceTier.MID_RANGE -> minOf(cpuCores, 3) // Up to 3 concurrent
            DevicePerformanceTier.LOW_END -> minOf(cpuCores, 2)   // Up to 2 concurrent
        }
    }
    
    /**
     * Check if device is currently under memory pressure
     */
    fun isUnderMemoryPressure(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        // Consider device under pressure if less than 20% memory available
        val availablePercentage = (memoryInfo.availMem.toDouble() / memoryInfo.totalMem.toDouble()) * 100
        return availablePercentage < 20.0 || memoryInfo.lowMemory
    }
    
    /**
     * Log device capabilities
     */
    private fun logCapabilities(capabilities: DeviceCapabilities) {
        Log.i(TAG, """
            === Device Capabilities ===
            Performance Tier: ${capabilities.performanceTier}
            Total Memory: ${capabilities.totalMemoryMB}MB
            Available Memory: ${capabilities.availableMemoryMB}MB
            CPU Cores: ${capabilities.cpuCores}
            Low Memory Device: ${capabilities.isLowMemoryDevice}
            Recommended Image Quality: ${capabilities.recommendedImageQuality}
            Recommended Cache Size: ${capabilities.recommendedCacheSize}
            Max Concurrent Operations: ${capabilities.maxConcurrentOperations}
            ===========================
        """.trimIndent())
    }
    
    companion object {
        private const val TAG = "DeviceCapability"
    }
}
