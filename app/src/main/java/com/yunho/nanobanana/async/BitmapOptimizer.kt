package com.yunho.nanobanana.async

import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min

/**
 * Bitmap optimization utilities for memory and performance
 * Provides adaptive quality adjustment based on device capabilities
 */
class BitmapOptimizer(
    private val deviceCapabilities: DeviceCapabilities
) {
    
    /**
     * Optimize bitmap for current device
     * Adjusts quality, resolution, and format based on device tier
     */
    fun optimize(
        bitmap: Bitmap,
        targetUse: TargetUse = TargetUse.DISPLAY
    ): OptimizedBitmap {
        val startTime = System.currentTimeMillis()
        
        // Determine optimal settings based on device and use case
        val settings = determineOptimizationSettings(bitmap, targetUse)
        
        // Apply optimizations
        var optimized = bitmap
        
        // 1. Scale down if necessary
        if (settings.shouldScale) {
            optimized = scaleBitmap(optimized, settings.targetWidth, settings.targetHeight)
            Log.d(TAG, "Scaled bitmap from ${bitmap.width}x${bitmap.height} to ${optimized.width}x${optimized.height}")
        }
        
        // 2. Compress with appropriate quality
        val compressed = compressBitmap(optimized, settings.compressionQuality, settings.format)
        
        val processingTime = System.currentTimeMillis() - startTime
        val originalSize = estimateBitmapSize(bitmap)
        val optimizedSize = compressed.size
        val compressionRatio = originalSize.toFloat() / optimizedSize.toFloat()
        
        Log.d(TAG, """
            Bitmap optimization complete:
            - Original: ${bitmap.width}x${bitmap.height} (~${originalSize / 1024}KB)
            - Optimized: ${optimized.width}x${optimized.height} (~${optimizedSize / 1024}KB)
            - Compression: ${compressionRatio}x
            - Time: ${processingTime}ms
        """.trimIndent())
        
        return OptimizedBitmap(
            bitmap = optimized,
            compressedData = compressed,
            compressionRatio = compressionRatio,
            processingTimeMs = processingTime
        )
    }
    
    /**
     * Determine optimization settings based on device and use case
     */
    private fun determineOptimizationSettings(
        bitmap: Bitmap,
        targetUse: TargetUse
    ): OptimizationSettings {
        val maxDimension = max(bitmap.width, bitmap.height)
        
        // Base settings on device tier
        val baseQuality = deviceCapabilities.recommendedImageQuality
        val maxAllowedDimension = when (deviceCapabilities.performanceTier) {
            DevicePerformanceTier.HIGH_END -> 2048
            DevicePerformanceTier.MID_RANGE -> 1536
            DevicePerformanceTier.LOW_END -> 1024
        }
        
        // Adjust based on target use
        val quality = when (targetUse) {
            TargetUse.DISPLAY -> baseQuality
            TargetUse.CACHE -> max(baseQuality - 10, 70)
            TargetUse.THUMBNAIL -> max(baseQuality - 20, 60)
        }
        
        // Determine if scaling is needed
        val shouldScale = maxDimension > maxAllowedDimension
        val scaleFactor = if (shouldScale) {
            maxAllowedDimension.toFloat() / maxDimension.toFloat()
        } else 1.0f
        
        return OptimizationSettings(
            compressionQuality = quality,
            format = Bitmap.CompressFormat.JPEG,
            shouldScale = shouldScale,
            targetWidth = (bitmap.width * scaleFactor).toInt(),
            targetHeight = (bitmap.height * scaleFactor).toInt()
        )
    }
    
    /**
     * Scale bitmap to target dimensions
     */
    private fun scaleBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
    }
    
    /**
     * Compress bitmap to byte array
     */
    private fun compressBitmap(
        bitmap: Bitmap,
        quality: Int,
        format: Bitmap.CompressFormat
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }
    
    /**
     * Estimate bitmap size in bytes
     */
    private fun estimateBitmapSize(bitmap: Bitmap): Int {
        // ARGB_8888 uses 4 bytes per pixel
        return bitmap.width * bitmap.height * 4
    }
    
    companion object {
        private const val TAG = "BitmapOptimizer"
    }
}

/**
 * Target use case for optimization
 */
enum class TargetUse {
    DISPLAY,    // For immediate display
    CACHE,      // For caching
    THUMBNAIL   // For thumbnails/previews
}

/**
 * Optimization settings
 */
private data class OptimizationSettings(
    val compressionQuality: Int,
    val format: Bitmap.CompressFormat,
    val shouldScale: Boolean,
    val targetWidth: Int,
    val targetHeight: Int
)

/**
 * Result of bitmap optimization
 */
data class OptimizedBitmap(
    val bitmap: Bitmap,
    val compressedData: ByteArray,
    val compressionRatio: Float,
    val processingTimeMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OptimizedBitmap
        return bitmap == other.bitmap && compressedData.contentEquals(other.compressedData)
    }
    
    override fun hashCode(): Int {
        var result = bitmap.hashCode()
        result = 31 * result + compressedData.contentHashCode()
        return result
    }
}
