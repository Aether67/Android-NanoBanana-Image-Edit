package com.yunho.nanobanana.async

import android.graphics.Bitmap
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.yunho.nanobanana.data.cache.AIContentCache

/**
 * Unit tests for AIContentCache
 */
class AIContentCacheTest {
    
    private lateinit var cache: AIContentCache
    
    @Before
    fun setup() {
        cache = AIContentCache(
            maxImageCacheSize = 5,
            maxTextCacheSize = 10,
            compressionQuality = 85
        )
    }
    
    @After
    fun tearDown() {
        cache.clear()
    }
    
    @Test
    fun `putImage and getImage should work correctly`() {
        // Given
        val bitmap = createMockBitmap(100, 100)
        val key = "test-image-1"
        
        // When
        cache.putImage(key, bitmap)
        val retrieved = cache.getImage(key)
        
        // Then
        assertNotNull(retrieved)
        assertEquals(100, retrieved.width)
        assertEquals(100, retrieved.height)
    }
    
    @Test
    fun `getImage should return null for non-existent key`() {
        // When
        val retrieved = cache.getImage("non-existent")
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun `putText and getText should work correctly`() {
        // Given
        val text = "This is a test text response from AI"
        val key = "test-text-1"
        
        // When
        cache.putText(key, text)
        val retrieved = cache.getText(key)
        
        // Then
        assertEquals(text, retrieved)
    }
    
    @Test
    fun `getText should return null for non-existent key`() {
        // When
        val retrieved = cache.getText("non-existent")
        
        // Then
        assertNull(retrieved)
    }
    
    @Test
    fun `cache should evict oldest entry when max size exceeded`() {
        // Given - max size is 5
        val bitmaps = (1..6).map { createMockBitmap(50, 50) }
        
        // When - add 6 images (exceeds max of 5)
        bitmaps.forEachIndexed { index, bitmap ->
            cache.putImage("image-$index", bitmap)
        }
        
        // Then - first image should be evicted
        assertNull(cache.getImage("image-0"))
        assertNotNull(cache.getImage("image-1"))
        assertNotNull(cache.getImage("image-5"))
    }
    
    @Test
    fun `LRU policy should work correctly`() {
        // Given - max size is 5
        (1..5).forEach { cache.putImage("image-$it", createMockBitmap(50, 50)) }
        
        // When - access image-1 to make it most recently used
        cache.getImage("image-1")
        
        // And add a new image (this should evict image-2, not image-1)
        cache.putImage("image-6", createMockBitmap(50, 50))
        
        // Then - image-1 should still be in cache, image-2 should be evicted
        assertNotNull(cache.getImage("image-1"))
        assertNull(cache.getImage("image-2"))
    }
    
    @Test
    fun `getStats should return correct statistics`() {
        // Given
        cache.putImage("img1", createMockBitmap(50, 50))
        cache.putImage("img2", createMockBitmap(50, 50))
        cache.putText("text1", "Test text")
        
        // Access for hit rate calculation
        cache.getImage("img1") // hit
        cache.getImage("img-nonexistent") // miss
        cache.getText("text1") // hit
        
        // When
        val stats = cache.getStats()
        
        // Then
        assertEquals(2, stats.imageCacheSize)
        assertEquals(1, stats.textCacheSize)
        assertTrue(stats.imageHitRate > 0f)
        assertTrue(stats.textHitRate > 0f)
    }
    
    @Test
    fun `clear should remove all cached items`() {
        // Given
        cache.putImage("img1", createMockBitmap(50, 50))
        cache.putText("text1", "Test")
        
        // When
        cache.clear()
        
        // Then
        assertNull(cache.getImage("img1"))
        assertNull(cache.getText("text1"))
        assertEquals(0, cache.getStats().imageCacheSize)
        assertEquals(0, cache.getStats().textCacheSize)
    }
    
    private fun createMockBitmap(width: Int, height: Int): Bitmap {
        val bitmap = mock<Bitmap>()
        whenever(bitmap.width).thenReturn(width)
        whenever(bitmap.height).thenReturn(height)
        whenever(bitmap.compress(org.mockito.kotlin.any(), org.mockito.kotlin.any(), org.mockito.kotlin.any()))
            .thenAnswer { invocation ->
                val stream = invocation.getArgument<java.io.ByteArrayOutputStream>(2)
                stream.write(ByteArray(width * height)) // Simulate compressed data
                true
            }
        return bitmap
    }
}
