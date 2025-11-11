package com.yunho.nanobanana.integration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.data.datasource.SettingsDataSource
import com.yunho.nanobanana.data.repository.AIRepositoryImpl
import com.yunho.nanobanana.data.repository.SettingsRepositoryImpl
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.usecase.GenerateAIContentUseCase
import com.yunho.nanobanana.domain.usecase.ManageAIParametersUseCase
import com.yunho.nanobanana.domain.usecase.ManageApiKeyUseCase
import com.yunho.nanobanana.presentation.state.GenerationState
import com.yunho.nanobanana.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * End-to-end integration test for complete generation flow
 * Tests from ViewModel down to Repository level
 * 
 * Note: This uses a fake AIDataSource since we can't make real API calls in tests
 */
@RunWith(AndroidJUnit4::class)
class CompleteGenerationFlowTest {
    
    private lateinit var context: Context
    private lateinit var viewModel: MainViewModel
    private lateinit var fakeAIDataSource: FakeAIDataSource
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Clear previous data
        context.getSharedPreferences("nanobanana_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
        
        // Setup dependencies with fake data source
        val settingsDataSource = SettingsDataSource(context)
        val settingsRepository = SettingsRepositoryImpl(settingsDataSource)
        
        fakeAIDataSource = FakeAIDataSource()
        val aiRepository = AIRepositoryImpl(fakeAIDataSource)
        
        val generateUseCase = GenerateAIContentUseCase(aiRepository)
        val apiKeyUseCase = ManageApiKeyUseCase(settingsRepository)
        val parametersUseCase = ManageAIParametersUseCase(settingsRepository)
        
        viewModel = MainViewModel(generateUseCase, apiKeyUseCase, parametersUseCase)
    }
    
    @Test
    fun completeFlow_withApiKeyAndImages_succeeds() = runTest {
        // Given - Setup API key
        viewModel.saveApiKey("test-api-key")
        
        // Create test bitmap
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        testBitmap.eraseColor(Color.RED)
        
        viewModel.addSelectedImages(listOf(testBitmap))
        
        // Configure fake data source to return success
        fakeAIDataSource.shouldSucceed = true
        fakeAIDataSource.resultBitmap = testBitmap
        
        // When - Generate content
        viewModel.generateContent("Test prompt")
        
        // Wait for flow to complete
        kotlinx.coroutines.delay(100)
        
        // Then - Verify success state
        val state = viewModel.uiState.first()
        assertIs<GenerationState.Success>(state.generationState)
    }
    
    @Test
    fun completeFlow_withoutApiKey_fails() = runTest {
        // Given - No API key, but with images
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        viewModel.addSelectedImages(listOf(testBitmap))
        
        // When - Try to generate
        viewModel.generateContent("Test prompt")
        
        kotlinx.coroutines.delay(100)
        
        // Then - Should be in error state
        val state = viewModel.uiState.first()
        assertIs<GenerationState.Error>(state.generationState)
        assertTrue((state.generationState as GenerationState.Error).message.contains("API key"))
    }
    
    @Test
    fun completeFlow_withoutImages_fails() = runTest {
        // Given - API key but no images
        viewModel.saveApiKey("test-api-key")
        
        // When - Try to generate
        viewModel.generateContent("Test prompt")
        
        kotlinx.coroutines.delay(100)
        
        // Then - Should be in error state
        val state = viewModel.uiState.first()
        assertIs<GenerationState.Error>(state.generationState)
        assertTrue((state.generationState as GenerationState.Error).message.contains("image"))
    }
    
    @Test
    fun resetToIdle_clearsAllState() = runTest {
        // Given - Setup with data
        viewModel.saveApiKey("test-api-key")
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        viewModel.addSelectedImages(listOf(testBitmap))
        viewModel.updatePrompt("Test prompt")
        
        // When - Reset
        viewModel.resetToIdle()
        
        kotlinx.coroutines.delay(100)
        
        // Then - State should be cleared
        val state = viewModel.uiState.first()
        assertIs<GenerationState.Idle>(state.generationState)
        assertTrue(state.selectedImages.isEmpty())
        assertEquals("", state.currentPrompt)
    }
}

/**
 * Fake AI Data Source for testing
 * Simulates AI service responses without making real API calls
 */
class FakeAIDataSource : com.yunho.nanobanana.data.datasource.AIDataSource {
    
    var shouldSucceed = true
    var resultBitmap: Bitmap? = null
    var resultText: String? = null
    var throwException = false
    
    override suspend fun generateImage(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): Bitmap? {
        if (throwException) throw RuntimeException("Fake exception")
        return if (shouldSucceed) resultBitmap else null
    }
    
    override suspend fun generateText(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): String? {
        if (throwException) throw RuntimeException("Fake exception")
        return if (shouldSucceed) resultText else null
    }
    
    override suspend fun generateCombined(
        prompt: String,
        bitmaps: List<Bitmap>,
        temperature: Float
    ): Pair<Bitmap?, String?> {
        if (throwException) throw RuntimeException("Fake exception")
        return if (shouldSucceed) {
            Pair(resultBitmap, resultText)
        } else {
            Pair(null, null)
        }
    }
}
