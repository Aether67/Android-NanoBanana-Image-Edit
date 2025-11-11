package com.yunho.nanobanana.domain

import android.graphics.Bitmap
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.repository.AIRepository
import com.yunho.nanobanana.domain.usecase.GenerateAIContentUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for GenerateAIContentUseCase
 * Tests business logic for AI content generation
 */
class GenerateAIContentUseCaseTest {
    
    private lateinit var mockRepository: AIRepository
    private lateinit var useCase: GenerateAIContentUseCase
    
    @Before
    fun setup() {
        mockRepository = mock()
        useCase = GenerateAIContentUseCase(mockRepository)
    }
    
    @Test
    fun `invoke should call repository with correct request`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap),
            outputMode = AIOutputMode.IMAGE_ONLY,
            parameters = AIParameters()
        )
        
        val expectedResult = AIGenerationResult.Success(image = mockBitmap)
        whenever(mockRepository.generateContent(any())).thenReturn(flowOf(expectedResult))
        
        // When
        val results = useCase(request).toList()
        
        // Then
        verify(mockRepository).generateContent(request)
        assertEquals(1, results.size)
        assertIs<AIGenerationResult.Success>(results[0])
    }
    
    @Test
    fun `invoke should emit loading then success states`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap)
        )
        
        val loadingResult = AIGenerationResult.Loading(0.5f, "Processing...")
        val successResult = AIGenerationResult.Success(image = mockBitmap)
        
        whenever(mockRepository.generateContent(any()))
            .thenReturn(flowOf(loadingResult, successResult))
        
        // When
        val results = useCase(request).toList()
        
        // Then
        assertEquals(2, results.size)
        assertIs<AIGenerationResult.Loading>(results[0])
        assertIs<AIGenerationResult.Success>(results[1])
    }
    
    @Test
    fun `invoke should handle error state`() = runTest {
        // Given
        val mockBitmap = mock<Bitmap>()
        val request = ImageGenerationRequest(
            prompt = "Test prompt",
            bitmaps = listOf(mockBitmap)
        )
        
        val errorResult = AIGenerationResult.Error("Test error")
        whenever(mockRepository.generateContent(any())).thenReturn(flowOf(errorResult))
        
        // When
        val results = useCase(request).toList()
        
        // Then
        assertEquals(1, results.size)
        assertIs<AIGenerationResult.Error>(results[0])
        assertEquals("Test error", (results[0] as AIGenerationResult.Error).message)
    }
}
