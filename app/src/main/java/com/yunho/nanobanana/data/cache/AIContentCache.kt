package com.yunho.nanobanana.data.cache

import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * LRU Cache for AI-generated content (images and text)
 * Thread-safe implementation with compressed storage for memory optimization
 */
class AIContentCache(
    private val maxImageCacheSize: Int = 20, // Maximum number of images to cache
    private val maxTextCacheSize: Int = 50,  // Maximum number of text responses to cache
    private val compressionQuality: Int = 85  // JPEG compression quality (0-100)
) {
    
    private val imageCacheLock = ReentrantReadWriteLock()
    private val textCacheLock = ReentrantReadWriteLock()
    
    // LRU cache for images (stored as compressed byte arrays to save memory)
    private val imageCache = object : LinkedHashMap<String, CompressedImage>(
        maxImageCacheSize,
        0.75f,
        true // access-order (LRU)
    ) {
        override fun removeEldestEntry(eldest: Map.Entry<String, CompressedImage>?): Boolean {
            val shouldRemove = size > maxImageCacheSize
            if (shouldRemove && eldest != null) {
                Log.d(TAG, "Evicting image from cache: ${eldest.key}")
                telemetry.recordCacheEviction("image")
            }
            return shouldRemove
        }
    }
    
    // LRU cache for text responses
    private val textCache = object : LinkedHashMap<String, CachedText>(
        maxTextCacheSize,
        0.75f,
        true // access-order (LRU)
    ) {
        override fun removeEldestEntry(eldest: Map.Entry<String, CachedText>?): Boolean {
            val shouldRemove = size > maxTextCacheSize
            if (shouldRemove && eldest != null) {
                Log.d(TAG, "Evicting text from cache: ${eldest.key}")
                telemetry.recordCacheEviction("text")
            }
            return shouldRemove
        }
    }
    
    private val telemetry = CacheTelemetry()
    
    /**
     * Store image in cache with compression
     */
    fun putImage(key: String, bitmap: Bitmap) {
        imageCacheLock.write {
            try {
                val compressed = compressBitmap(bitmap)
                imageCache[key] = compressed
                Log.d(TAG, "Cached image: $key (${compressed.sizeBytes} bytes)")
                telemetry.recordCachePut("image", compressed.sizeBytes)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to cache image: $key", e)
                telemetry.recordCacheError("image", "put")
            }
        }
    }
    
    /**
     * Retrieve image from cache
     */
    fun getImage(key: String): Bitmap? {
        return imageCacheLock.read {
            val compressed = imageCache[key]
            if (compressed != null) {
                try {
                    val bitmap = decompressBitmap(compressed)
                    Log.d(TAG, "Cache hit for image: $key")
                    telemetry.recordCacheHit("image")
                    bitmap
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decompress image: $key", e)
                    telemetry.recordCacheError("image", "get")
                    null
                }
            } else {
                Log.d(TAG, "Cache miss for image: $key")
                telemetry.recordCacheMiss("image")
                null
            }
        }
    }
    
    /**
     * Store text in cache
     */
    fun putText(key: String, text: String) {
        textCacheLock.write {
            textCache[key] = CachedText(text, System.currentTimeMillis())
            Log.d(TAG, "Cached text: $key (${text.length} chars)")
            telemetry.recordCachePut("text", text.length)
        }
    }
    
    /**
     * Retrieve text from cache
     */
    fun getText(key: String): String? {
        return textCacheLock.read {
            val cached = textCache[key]
            if (cached != null) {
                Log.d(TAG, "Cache hit for text: $key")
                telemetry.recordCacheHit("text")
                cached.content
            } else {
                Log.d(TAG, "Cache miss for text: $key")
                telemetry.recordCacheMiss("text")
                null
            }
        }
    }
    
    /**
     * Clear all caches
     */
    fun clear() {
        imageCacheLock.write { imageCache.clear() }
        textCacheLock.write { textCache.clear() }
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats {
        val imageCount = imageCacheLock.read { imageCache.size }
        val textCount = textCacheLock.read { textCache.size }
        return CacheStats(
            imageCacheSize = imageCount,
            textCacheSize = textCount,
            imageHitRate = telemetry.getHitRate("image"),
            textHitRate = telemetry.getHitRate("text"),
            totalEvictions = telemetry.getTotalEvictions()
        )
    }
    
    /**
     * Compress bitmap to byte array
     */
    private fun compressBitmap(bitmap: Bitmap): CompressedImage {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, stream)
        val bytes = stream.toByteArray()
        return CompressedImage(
            data = bytes,
            width = bitmap.width,
            height = bitmap.height,
            sizeBytes = bytes.size
        )
    }
    
    /**
     * Decompress byte array to bitmap
     */
    private fun decompressBitmap(compressed: CompressedImage): Bitmap {
        return android.graphics.BitmapFactory.decodeByteArray(
            compressed.data,
            0,
            compressed.data.size
        ) ?: throw IllegalStateException("Failed to decode bitmap")
    }
    
    /**
     * Compressed image data
     */
    private data class CompressedImage(
        val data: ByteArray,
        val width: Int,
        val height: Int,
        val sizeBytes: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as CompressedImage
            return data.contentEquals(other.data) && width == other.width && height == other.height
        }
        
        override fun hashCode(): Int {
            var result = data.contentHashCode()
            result = 31 * result + width
            result = 31 * result + height
            return result
        }
    }
    
    /**
     * Cached text with timestamp
     */
    private data class CachedText(
        val content: String,
        val timestamp: Long
    )
    
    companion object {
        private const val TAG = "AIContentCache"
    }
}

/**
 * Cache statistics
 */
data class CacheStats(
    val imageCacheSize: Int,
    val textCacheSize: Int,
    val imageHitRate: Float,
    val textHitRate: Float,
    val totalEvictions: Int
)

/**
 * Cache telemetry for monitoring
 */
private class CacheTelemetry {
    private val lock = ReentrantReadWriteLock()
    private val stats = mutableMapOf<String, TypeStats>()
    
    fun recordCacheHit(type: String) {
        lock.write {
            stats.getOrPut(type) { TypeStats() }.hits++
        }
    }
    
    fun recordCacheMiss(type: String) {
        lock.write {
            stats.getOrPut(type) { TypeStats() }.misses++
        }
    }
    
    fun recordCachePut(type: String, sizeBytes: Int) {
        lock.write {
            val typeStats = stats.getOrPut(type) { TypeStats() }
            typeStats.puts++
            typeStats.totalBytes += sizeBytes
        }
    }
    
    fun recordCacheEviction(type: String) {
        lock.write {
            stats.getOrPut(type) { TypeStats() }.evictions++
        }
    }
    
    fun recordCacheError(type: String, operation: String) {
        lock.write {
            stats.getOrPut(type) { TypeStats() }.errors++
        }
        Log.e("CacheTelemetry", "Cache error: $type.$operation")
    }
    
    fun getHitRate(type: String): Float {
        return lock.read {
            val typeStats = stats[type] ?: return@read 0f
            val total = typeStats.hits + typeStats.misses
            if (total == 0) 0f else typeStats.hits.toFloat() / total
        }
    }
    
    fun getTotalEvictions(): Int {
        return lock.read {
            stats.values.sumOf { it.evictions }
        }
    }
    
    private data class TypeStats(
        var hits: Int = 0,
        var misses: Int = 0,
        var puts: Int = 0,
        var evictions: Int = 0,
        var errors: Int = 0,
        var totalBytes: Int = 0
    )
}
