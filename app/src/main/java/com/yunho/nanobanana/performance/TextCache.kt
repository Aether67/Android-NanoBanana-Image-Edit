package com.yunho.nanobanana.performance

import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * Simple text response cache with TTL (Time To Live)
 * Caches generated text responses to reduce API calls
 */
class TextCache private constructor() {
    
    private data class CacheEntry(
        val text: String,
        val timestamp: Long
    )
    
    private val cache = ConcurrentHashMap<String, CacheEntry>()
    private val ttlMillis = 30 * 60 * 1000L // 30 minutes
    
    /**
     * Retrieves cached text if available and not expired
     */
    fun get(key: String): String? {
        val entry = cache[key] ?: return null
        
        if (System.currentTimeMillis() - entry.timestamp > ttlMillis) {
            cache.remove(key)
            Log.d(TAG, "Cache entry expired: $key")
            return null
        }
        
        Log.d(TAG, "Cache hit for key: $key")
        return entry.text
    }
    
    /**
     * Stores text response in cache
     */
    fun put(key: String, text: String) {
        cache[key] = CacheEntry(text, System.currentTimeMillis())
        Log.d(TAG, "Cached text response: $key")
    }
    
    /**
     * Clears all cached text
     */
    fun clear() {
        cache.clear()
        Log.d(TAG, "Text cache cleared")
    }
    
    /**
     * Removes expired entries
     */
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        val toRemove = cache.filter { (_, entry) ->
            now - entry.timestamp > ttlMillis
        }.keys
        
        toRemove.forEach { cache.remove(it) }
        if (toRemove.isNotEmpty()) {
            Log.d(TAG, "Cleaned up ${toRemove.size} expired entries")
        }
    }
    
    companion object {
        private const val TAG = "TextCache"
        
        @Volatile
        private var instance: TextCache? = null
        
        fun getInstance(): TextCache {
            return instance ?: synchronized(this) {
                instance ?: TextCache().also { instance = it }
            }
        }
    }
}
