package com.yunho.nanobanana.data

import android.graphics.Bitmap
import com.yunho.nanobanana.data.datasource.AIDataSource
import com.yunho.nanobanana.data.repository.AIRepositoryImpl
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for AIRepositoryImpl
 * Tests data layer logic for AI operations
 */
class AIRepositoryImplTest {
    
    private lateinit var mockDataSource: AIDataSource
    private lateinit var repository: AIRepositoryImpl
    
    @Before
    fun setup() {
        mockDataSource = mock()
        repository = AIRepositoryImpl(mockDataSource)
    }
    
    @Test
    fun `generateContent with IMAGE_ONLY should call generateImage`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.IMAGE_ONLY,
            parameters = AIParameters(creativityLevel = 0.8f)
        )
        
        whenever(mockDataSource.generateImage(any(), any(), any()))
            .thenReturn(mockBitmap)
        
        // When
        val results = repository.generateContent(request).toList()
        
        // Then
        verify(mockDataSource).generateImage(
            prompt = eq(request.prompt),
            bitmaps = eq(request.bitmaps),
            temperature = eq(0.8f)
        )
        
        val successResult = results.last()
        assertIs<AIGenerationResult.Success>(successResult)
        assertNotNull(successResult.image)
        assertNull(successResult.text)
    }
    
    @Test
    fun `generateContent with TEXT_ONLY should call generateText`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.TEXT_ONLY,
            parameters = AIParameters(creativityLevel = 0.7f)
        )
        
        whenever(mockDataSource.generateText(any(), any(), any()))
            .thenReturn("Generated text")
        
        // When
        val results = repository.generateContent(request).toList()
        
        // Then
        verify(mockDataSource).generateText(
            prompt = eq(request.prompt),
            bitmaps = eq(request.bitmaps),
            temperature = eq(0.7f)
        )
        
        val successResult = results.last()
        assertIs<AIGenerationResult.Success>(successResult)
        assertNull(successResult.image)
        assertEquals("Generated text", successResult.text)
    }
    
    @Test
    fun `generateContent with COMBINED should call generateCombined`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.COMBINED,
            parameters = AIParameters()
        )
        
        whenever(mockDataSource.generateCombined(any(), any(), any()))
            .thenReturn(Pair(mockBitmap, "Generated text"))
        
        // When
        val results = repository.generateContent(request).toList()
        
        // Then
        verify(mockDataSource).generateCombined(
            prompt = eq(request.prompt),
            bitmaps = eq(request.bitmaps),
            temperature = eq(0.7f)
        )
        
        val successResult = results.last()
        assertIs<AIGenerationResult.Success>(successResult)
        assertNotNull(successResult.image)
        assertNotNull(successResult.text)
    }
    
    @Test
    fun `generateContent should emit loading states`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.IMAGE_ONLY
        )
        
        whenever(mockDataSource.generateImage(any(), any(), any()))
            .thenReturn(mockBitmap)
        
        // When
        val results = repository.generateContent(request).toList()
        
        // Then
        assert(results.size >= 2) // At least one loading and one success
        assertIs<AIGenerationResult.Loading>(results[0])
    }
    
    @Test
    fun `generateContent should handle null results as error`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.IMAGE_ONLY
        )
        
        whenever(mockDataSource.generateImage(any(), any(), any()))
            .thenReturn(null)
        
        // When
        val results = repository.generateContent(request).toList()
        
        // Then
        val errorResult = results.last()
        assertIs<AIGenerationResult.Error>(errorResult)
    }
    
    @Test
    fun `generateContent should handle exceptions`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.IMAGE_ONLY
        )
        
        whenever(mockDataSource.generateImage(any(), any(), any()))
            .thenThrow(RuntimeException("Network error"))
        
        // When
        val results = repository.generateContent(request).toList()
        
        // Then
        val errorResult = results.last()
        assertIs<AIGenerationResult.Error>(errorResult)
        assert(errorResult.message.contains("Generation failed"))
    }
}
