package com.yunho.nanobanana.performance

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log

/**
 * Resource monitor for detecting system constraints
 * Enables graceful degradation based on available resources
 */
class ResourceMonitor(private val context: Context) {
    
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    /**
     * Checks if device has low memory
     */
    fun isLowMemory(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val isLow = memoryInfo.lowMemory
        if (isLow) {
            Log.w(TAG, "Low memory detected: Available=${memoryInfo.availMem / 1024 / 1024}MB, Threshold=${memoryInfo.threshold / 1024 / 1024}MB")
        }
        return isLow
    }
    
    /**
     * Gets available memory in MB
     */
    fun getAvailableMemoryMB(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem / 1024 / 1024
    }
    
    /**
     * Checks if device has network connectivity
     */
    fun hasNetworkConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Checks if connection is metered (cellular, limited data)
     */
    fun isConnectionMetered(): Boolean {
        return connectivityManager.isActiveNetworkMetered
    }
    
    /**
     * Gets connection quality assessment
     */
    fun getConnectionQuality(): ConnectionQuality {
        if (!hasNetworkConnection()) {
            return ConnectionQuality.NONE
        }
        
        val network = connectivityManager.activeNetwork ?: return ConnectionQuality.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionQuality.NONE
        
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionQuality.EXCELLENT
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionQuality.EXCELLENT
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Check bandwidth if available
                val downKbps = capabilities.linkDownstreamBandwidthKbps
                when {
                    downKbps > 5000 -> ConnectionQuality.GOOD
                    downKbps > 1000 -> ConnectionQuality.FAIR
                    else -> ConnectionQuality.POOR
                }
            }
            else -> ConnectionQuality.POOR
        }
    }
    
    /**
     * Checks if device is suitable for high-quality image processing
     */
    fun canProcessHighQualityImages(): Boolean {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        
        val availableMemoryMB = memoryInfo.availMem / 1024 / 1024
        val hasGoodMemory = availableMemoryMB > MIN_MEMORY_FOR_HIGH_QUALITY_MB
        val hasGoodConnection = getConnectionQuality() in listOf(ConnectionQuality.EXCELLENT, ConnectionQuality.GOOD)
        
        return hasGoodMemory && hasGoodConnection && !isLowMemory()
    }
    
    /**
     * Gets recommended image quality based on resources
     */
    fun getRecommendedImageQuality(): Int {
        return when {
            isLowMemory() -> 75 // Low quality
            getAvailableMemoryMB() < 200 -> 85 // Medium quality
            else -> 95 // High quality
        }
    }
    
    /**
     * Gets resource status summary
     */
    fun getResourceStatus(): ResourceStatus {
        return ResourceStatus(
            availableMemoryMB = getAvailableMemoryMB(),
            isLowMemory = isLowMemory(),
            hasNetwork = hasNetworkConnection(),
            connectionQuality = getConnectionQuality(),
            isMetered = isConnectionMetered(),
            canProcessHighQuality = canProcessHighQualityImages(),
            recommendedQuality = getRecommendedImageQuality()
        )
    }
    
    enum class ConnectionQuality {
        NONE, POOR, FAIR, GOOD, EXCELLENT
    }
    
    data class ResourceStatus(
        val availableMemoryMB: Long,
        val isLowMemory: Boolean,
        val hasNetwork: Boolean,
        val connectionQuality: ConnectionQuality,
        val isMetered: Boolean,
        val canProcessHighQuality: Boolean,
        val recommendedQuality: Int
    )
    
    companion object {
        private const val TAG = "ResourceMonitor"
        private const val MIN_MEMORY_FOR_HIGH_QUALITY_MB = 300L
    }
}
