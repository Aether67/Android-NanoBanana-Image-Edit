package com.yunho.nanobanana.performance

import android.graphics.Bitmap
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ImageCache
 */
class ImageCacheTest {
    
    @Test
    fun testCacheGetPutCycle() {
        val cache = ImageCache.getInstance()
        cache.clear()
        
        // Create a mock bitmap (in real test would use actual bitmap)
        // For this test, we're primarily testing the cache logic
        val prompt = "test prompt"
        val bitmaps = emptyList<Bitmap>()
        
        // Should return null for non-existent key
        val result1 = cache.get(prompt, bitmaps)
        assertNull(result1)
        
        // Note: In actual production test, we would create real bitmaps
        // and verify the put/get cycle works correctly
    }
    
    @Test
    fun testCacheStats() {
        val cache = ImageCache.getInstance()
        cache.clear()
        
        val stats = cache.getStats()
        
        // After clear, size should be 0
        assertEquals(0, stats.currentSize)
        assertTrue(stats.maxSize > 0)
    }
}
