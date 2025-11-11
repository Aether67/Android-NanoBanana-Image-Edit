package com.yunho.nanobanana.integration

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.presentation.state.GenerationState
import com.yunho.nanobanana.presentation.state.MainUiState
import com.yunho.nanobanana.presentation.viewmodel.MainViewModel
import com.yunho.nanobanana.ui.screens.MainScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for enhancement feature end-to-end flow
 * Tests UI integration with ViewModel and state management
 */
@RunWith(AndroidJUnit4::class)
class EnhancementFeatureIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun enhanceButton_appearsWhenImageIsGenerated() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val mockViewModel = mockk<MainViewModel>(relaxed = true)
        val uiState = MutableStateFlow(
            MainUiState(
                generationState = GenerationState.Success(
                    image = testBitmap,
                    text = "Generated text",
                    reasoning = null
                )
            )
        )
        every { mockViewModel.uiState } returns uiState

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState.value,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = {},
                onSaveVariant = {},
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("✨ Enhance Image")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun enhanceButton_clickTriggersEnhancement() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        var enhanceClicked = false
        val uiState = MainUiState(
            generationState = GenerationState.Success(
                image = testBitmap,
                text = "Generated text",
                reasoning = null
            )
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = { enhanceClicked = true },
                onSaveVariant = {},
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("✨ Enhance Image")
            .performClick()
        
        assert(enhanceClicked)
    }

    @Test
    fun saveVariantButton_appearsAfterGeneration() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val uiState = MainUiState(
            generationState = GenerationState.Success(
                image = testBitmap,
                text = "Generated text",
                reasoning = null
            )
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = {},
                onSaveVariant = {},
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("💾 Save Variant")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun saveVariantButton_clickTriggersSaveVariant() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        var saveVariantClicked = false
        val uiState = MainUiState(
            generationState = GenerationState.Success(
                image = testBitmap,
                text = "Generated text",
                reasoning = null
            )
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = {},
                onSaveVariant = { saveVariantClicked = true },
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("💾 Save Variant")
            .performClick()
        
        assert(saveVariantClicked)
    }

    @Test
    fun loadingState_showsProgressIndicator() {
        // Given
        val uiState = MainUiState(
            generationState = GenerationState.Loading
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = {},
                onSaveVariant = {},
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithTag("LoadingIndicator")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        // Given
        val uiState = MainUiState(
            generationState = GenerationState.Error("Test error message")
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = {},
                onSaveVariant = {},
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Test error message")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun idleState_showsImagePickerAndPromptInput() {
        // Given
        val uiState = MainUiState(
            generationState = GenerationState.Idle
        )

        // When
        composeTestRule.setContent {
            MainScreen(
                uiState = uiState,
                onGenerateClick = {},
                onApiKeyChange = {},
                onPromptChange = {},
                onStyleChange = {},
                onImagesPicked = {},
                onRemoveImageAt = {},
                onSaveImage = {},
                onReset = {},
                onEnhanceClick = {},
                onSaveVariant = {},
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithTag("PromptInput")
            .assertExists()
            .assertIsDisplayed()
    }
}
