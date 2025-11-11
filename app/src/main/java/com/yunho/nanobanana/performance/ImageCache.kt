package com.yunho.nanobanana.performance

import android.graphics.Bitmap
import android.util.Log
import androidx.collection.LruCache
import java.security.MessageDigest

/**
 * LRU-based image cache for efficient memory management
 * Caches both input and generated images to improve response times
 */
class ImageCache private constructor() {
    
    // Calculate max cache size (use 1/8 of available memory)
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    
    private val cache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Size in KB
            return bitmap.byteCount / 1024
        }
        
        override fun entryRemoved(
            evicted: Boolean,
            key: String,
            oldValue: Bitmap,
            newValue: Bitmap?
        ) {
            if (evicted) {
                Log.d(TAG, "Image evicted from cache: $key")
            }
        }
    }
    
    /**
     * Generates cache key from prompt and bitmap list
     */
    private fun generateKey(prompt: String, bitmaps: List<Bitmap>): String {
        val content = buildString {
            append(prompt)
            bitmaps.forEach { bitmap ->
                append(bitmap.width)
                append(bitmap.height)
            }
        }
        
        // Generate MD5 hash for compact key
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(content.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Retrieves cached image if available
     */
    fun get(prompt: String, bitmaps: List<Bitmap>): Bitmap? {
        val key = generateKey(prompt, bitmaps)
        val cached = cache.get(key)
        if (cached != null) {
            Log.d(TAG, "Cache hit for key: $key")
        }
        return cached
    }
    
    /**
     * Stores generated image in cache
     */
    fun put(prompt: String, bitmaps: List<Bitmap>, result: Bitmap) {
        val key = generateKey(prompt, bitmaps)
        cache.put(key, result)
        Log.d(TAG, "Cached image: $key (${cache.size()}/${cacheSize} KB used)")
    }
    
    /**
     * Clears all cached images
     */
    fun clear() {
        cache.evictAll()
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Returns current cache statistics
     */
    fun getStats(): CacheStats {
        return CacheStats(
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            evictionCount = cache.evictionCount(),
            currentSize = cache.size(),
            maxSize = cacheSize
        )
    }
    
    data class CacheStats(
        val hitCount: Int,
        val missCount: Int,
        val evictionCount: Int,
        val currentSize: Int,
        val maxSize: Int
    ) {
        val hitRate: Float
            get() = if (hitCount + missCount > 0) {
                hitCount.toFloat() / (hitCount + missCount)
            } else 0f
    }
    
    companion object {
        private const val TAG = "ImageCache"
        
        @Volatile
        private var instance: ImageCache? = null
        
        fun getInstance(): ImageCache {
            return instance ?: synchronized(this) {
                instance ?: ImageCache().also { instance = it }
            }
        }
    }
}
