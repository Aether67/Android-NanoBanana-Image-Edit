package com.yunho.nanobanana.presentation

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.usecase.GenerateAIContentUseCase
import com.yunho.nanobanana.domain.usecase.ManageAIParametersUseCase
import com.yunho.nanobanana.domain.usecase.ManageApiKeyUseCase
import com.yunho.nanobanana.presentation.state.GenerationState
import com.yunho.nanobanana.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for MainViewModel
 * Tests presentation logic and state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var mockGenerateUseCase: GenerateAIContentUseCase
    private lateinit var mockApiKeyUseCase: ManageApiKeyUseCase
    private lateinit var mockParametersUseCase: ManageAIParametersUseCase
    private lateinit var viewModel: MainViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockGenerateUseCase = mock()
        mockApiKeyUseCase = mock()
        mockParametersUseCase = mock()
        
        // Setup default flows
        whenever(mockApiKeyUseCase.getApiKey()).thenReturn(flowOf("test-api-key"))
        whenever(mockParametersUseCase.getParameters()).thenReturn(flowOf(AIParameters()))
        
        viewModel = MainViewModel(
            generateContentUseCase = mockGenerateUseCase,
            manageApiKeyUseCase = mockApiKeyUseCase,
            manageParametersUseCase = mockParametersUseCase
        )
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state should be idle`() = runTest {
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Idle>(state.generationState)
            assertTrue(state.selectedImages.isEmpty())
            assertEquals("", state.currentPrompt)
        }
    }
    
    @Test
    fun `addSelectedImages should update state`() = runTest {
        advanceUntilIdle()
        
        val mockBitmap = mock<Bitmap>()
        viewModel.addSelectedImages(listOf(mockBitmap))
        
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(1, state.selectedImages.size)
        }
    }
    
    @Test
    fun `updatePrompt should update state`() = runTest {
        advanceUntilIdle()
        
        viewModel.updatePrompt("Test prompt")
        
        advanceUntilIdle()
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Test prompt", state.currentPrompt)
        }
    }
    
    @Test
    fun `generateContent should fail if API key is blank`() = runTest {
        // Given - setup with blank API key
        whenever(mockApiKeyUseCase.getApiKey()).thenReturn(flowOf(""))
        
        val viewModelWithBlankKey = MainViewModel(
            generateContentUseCase = mockGenerateUseCase,
            manageApiKeyUseCase = mockApiKeyUseCase,
            manageParametersUseCase = mockParametersUseCase
        )
        
        advanceUntilIdle()
        
        val mockBitmap = mock<Bitmap>()
        viewModelWithBlankKey.addSelectedImages(listOf(mockBitmap))
        advanceUntilIdle()
        
        // When
        viewModelWithBlankKey.generateContent("Test prompt")
        advanceUntilIdle()
        
        // Then
        viewModelWithBlankKey.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Error>(state.generationState)
            assertTrue((state.generationState as GenerationState.Error).message.contains("API key"))
        }
    }
    
    @Test
    fun `generateContent should fail if no images selected`() = runTest {
        advanceUntilIdle()
        
        // When - generate without selecting images
        viewModel.generateContent("Test prompt")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Error>(state.generationState)
            assertTrue((state.generationState as GenerationState.Error).message.contains("image"))
        }
    }
    
    @Test
    fun `generateContent should emit loading then success states`() = runTest {
        advanceUntilIdle()
        
        // Given
        val mockBitmap = mock<Bitmap>()
        viewModel.addSelectedImages(listOf(mockBitmap))
        advanceUntilIdle()
        
        val loadingResult = AIGenerationResult.Loading(0.5f, "Processing...")
        val successResult = AIGenerationResult.Success(image = mockBitmap)
        
        whenever(mockGenerateUseCase.invoke(any()))
            .thenReturn(flowOf(loadingResult, successResult))
        
        // When
        viewModel.generateContent("Test prompt")
        advanceUntilIdle()
        
        // Then
        verify(mockGenerateUseCase).invoke(any())
        
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Success>(state.generationState)
        }
    }
    
    @Test
    fun `generateContent should handle error state`() = runTest {
        advanceUntilIdle()
        
        // Given
        val mockBitmap = mock<Bitmap>()
        viewModel.addSelectedImages(listOf(mockBitmap))
        advanceUntilIdle()
        
        val errorResult = AIGenerationResult.Error("Generation failed")
        whenever(mockGenerateUseCase.invoke(any())).thenReturn(flowOf(errorResult))
        
        // When
        viewModel.generateContent("Test prompt")
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Error>(state.generationState)
            assertEquals("Generation failed", (state.generationState as GenerationState.Error).message)
        }
    }
    
    @Test
    fun `resetToIdle should clear state`() = runTest {
        advanceUntilIdle()
        
        // Given - setup state with images
        val mockBitmap = mock<Bitmap>()
        viewModel.addSelectedImages(listOf(mockBitmap))
        viewModel.updatePrompt("Test prompt")
        advanceUntilIdle()
        
        // When
        viewModel.resetToIdle()
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<GenerationState.Idle>(state.generationState)
            assertTrue(state.selectedImages.isEmpty())
            assertEquals("", state.currentPrompt)
        }
    }
    
    @Test
    fun `saveApiKey should call use case`() = runTest {
        advanceUntilIdle()
        
        // When
        viewModel.saveApiKey("new-api-key")
        advanceUntilIdle()
        
        // Then
        verify(mockApiKeyUseCase).saveApiKey("new-api-key")
    }
    
    @Test
    fun `updateParameters should call use case`() = runTest {
        advanceUntilIdle()
        
        // Given
        val newParameters = AIParameters(creativityLevel = 0.9f)
        
        // When
        viewModel.updateParameters(newParameters)
        advanceUntilIdle()
        
        // Then
        verify(mockParametersUseCase).updateParameters(newParameters)
    }
    
    @Test
    fun `updateOutputMode should update state`() = runTest {
        advanceUntilIdle()
        
        // When
        viewModel.updateOutputMode(AIOutputMode.TEXT_ONLY)
        advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(AIOutputMode.TEXT_ONLY, state.outputMode)
        }
    }
}
