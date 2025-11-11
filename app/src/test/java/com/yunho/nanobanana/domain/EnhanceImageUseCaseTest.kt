package com.yunho.nanobanana.domain

import android.graphics.Bitmap
import com.yunho.nanobanana.domain.model.EnhancementType
import com.yunho.nanobanana.domain.model.ImageEnhancementRequest
import com.yunho.nanobanana.domain.repository.AIRepository
import com.yunho.nanobanana.domain.usecase.EnhanceImageUseCase
import com.yunho.nanobanana.presentation.state.EnhancementErrorReason
import com.yunho.nanobanana.presentation.state.EnhancementResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EnhanceImageUseCase
 * Tests validation logic, error handling, and repository integration
 */
class EnhanceImageUseCaseTest {

    private lateinit var mockRepository: AIRepository
    private lateinit var useCase: EnhanceImageUseCase
    private lateinit var testBitmap: Bitmap

    @Before
    fun setup() {
        mockRepository = mockk()
        useCase = EnhanceImageUseCase(mockRepository)
        testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
    }

    @Test
    fun `invoke emits loading then success when enhancement succeeds`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = testBitmap,
            enhancementType = EnhancementType.DETAIL_SHARPEN,
            prompt = "Enhance details"
        )
        val enhancedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        coEvery { mockRepository.enhanceImage(request) } returns enhancedBitmap

        // When
        val results = useCase(request).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is EnhancementResult.Loading)
        assertTrue(results[1] is EnhancementResult.Success)
        
        val success = results[1] as EnhancementResult.Success
        assertEquals(enhancedBitmap, success.enhancedImage)
        assertTrue(success.processingTimeMs >= 0)
    }

    @Test
    fun `invoke emits loading then error when image is null`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = null,
            enhancementType = EnhancementType.DETAIL_SHARPEN,
            prompt = "Enhance"
        )

        // When
        val results = useCase(request).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is EnhancementResult.Loading)
        assertTrue(results[1] is EnhancementResult.Error)
        
        val error = results[1] as EnhancementResult.Error
        assertEquals(EnhancementErrorReason.INVALID_IMAGE, error.reason)
        assertTrue(error.message.contains("No image"))
    }

    @Test
    fun `invoke emits loading then error when image is too large`() = runTest {
        // Given
        val largeBitmap = Bitmap.createBitmap(5000, 5000, Bitmap.Config.ARGB_8888)
        val request = ImageEnhancementRequest(
            image = largeBitmap,
            enhancementType = EnhancementType.DETAIL_SHARPEN,
            prompt = "Enhance"
        )

        // When
        val results = useCase(request).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is EnhancementResult.Loading)
        assertTrue(results[1] is EnhancementResult.Error)
        
        val error = results[1] as EnhancementResult.Error
        assertEquals(EnhancementErrorReason.IMAGE_TOO_LARGE, error.reason)
        assertTrue(error.message.contains("too large"))
        
        largeBitmap.recycle()
    }

    @Test
    fun `invoke emits loading then error when repository throws exception`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = testBitmap,
            enhancementType = EnhancementType.DETAIL_SHARPEN,
            prompt = "Enhance"
        )
        
        coEvery { mockRepository.enhanceImage(request) } throws RuntimeException("Network error")

        // When
        val results = useCase(request).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[0] is EnhancementResult.Loading)
        assertTrue(results[1] is EnhancementResult.Error)
        
        val error = results[1] as EnhancementResult.Error
        assertEquals(EnhancementErrorReason.API_ERROR, error.reason)
        assertTrue(error.message.contains("Network error"))
    }

    @Test
    fun `invoke handles localized enhancement correctly`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = testBitmap,
            enhancementType = EnhancementType.LOCALIZED_ENHANCE,
            prompt = "Enhance visible region",
            visibleRegion = android.graphics.Rect(10, 10, 50, 50),
            zoomLevel = 2.5f
        )
        val enhancedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        coEvery { mockRepository.enhanceImage(request) } returns enhancedBitmap

        // When
        val results = useCase(request).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[1] is EnhancementResult.Success)
    }

    @Test
    fun `invoke handles super resolution enhancement correctly`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = testBitmap,
            enhancementType = EnhancementType.SUPER_RESOLUTION,
            prompt = "Apply super resolution"
        )
        val enhancedBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        
        coEvery { mockRepository.enhanceImage(request) } returns enhancedBitmap

        // When
        val results = useCase(request).toList()

        // Then
        assertEquals(2, results.size)
        assertTrue(results[1] is EnhancementResult.Success)
        
        val success = results[1] as EnhancementResult.Success
        assertEquals(200, success.enhancedImage.width)
        assertEquals(200, success.enhancedImage.height)
        
        enhancedBitmap.recycle()
    }

    @Test
    fun `invoke provides progress updates during loading`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = testBitmap,
            enhancementType = EnhancementType.DETAIL_SHARPEN,
            prompt = "Enhance"
        )
        val enhancedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        coEvery { mockRepository.enhanceImage(request) } returns enhancedBitmap

        // When
        val results = useCase(request).toList()

        // Then
        val loading = results[0] as EnhancementResult.Loading
        assertTrue(loading.progress >= 0f)
        assertTrue(loading.progress <= 1f)
        assertTrue(loading.message.isNotEmpty())
    }

    @Test
    fun `invoke measures processing time accurately`() = runTest {
        // Given
        val request = ImageEnhancementRequest(
            image = testBitmap,
            enhancementType = EnhancementType.DETAIL_SHARPEN,
            prompt = "Enhance"
        )
        val enhancedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        
        coEvery { mockRepository.enhanceImage(request) } coAnswers {
            kotlinx.coroutines.delay(100) // Simulate processing time
            enhancedBitmap
        }

        // When
        val results = useCase(request).toList()

        // Then
        val success = results[1] as EnhancementResult.Success
        assertTrue(success.processingTimeMs >= 100)
    }
}
