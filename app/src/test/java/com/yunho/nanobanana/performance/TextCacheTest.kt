package com.yunho.nanobanana.performance

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TextCache
 */
class TextCacheTest {
    
    @Test
    fun testTextCacheGetPut() {
        val cache = TextCache.getInstance()
        cache.clear()
        
        val key = "test_key"
        val value = "test_value"
        
        // Should return null for non-existent key
        assertNull(cache.get(key))
        
        // Put and retrieve
        cache.put(key, value)
        assertEquals(value, cache.get(key))
    }
    
    @Test
    fun testTextCacheClear() {
        val cache = TextCache.getInstance()
        
        cache.put("key1", "value1")
        cache.put("key2", "value2")
        
        cache.clear()
        
        assertNull(cache.get("key1"))
        assertNull(cache.get("key2"))
    }
}
