package com.yunho.nanobanana.ui.screens

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yunho.nanobanana.components.*
import com.yunho.nanobanana.domain.model.EnhancementResult
import com.yunho.nanobanana.extension.saveBitmapToGallery
import com.yunho.nanobanana.presentation.state.GenerationState
import com.yunho.nanobanana.presentation.state.MainUiState
import kotlinx.coroutines.launch

/**
 * Main screen for NanoBanana AI Image Editor
 * Follows Clean Architecture with MVVM pattern
 * Integrates AI image generation, enhancement, and text generation
 */
@Composable
fun MainScreen(
    uiState: MainUiState,
    onApiKeySave: (String) -> Unit,
    onImageSelect: (List<Bitmap>) -> Unit,
    onPromptChange: (String) -> Unit,
    onStyleSelect: (Int) -> Unit,
    onGenerateClick: () -> Unit,
    onEnhanceClick: () -> Unit,
    onZoomEnhance: (Rect?) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics {
                contentDescription = "NanoBanana AI Image Editor main screen"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
    ) {
        when (val state = uiState.generationState) {
            is GenerationState.Idle -> {
                PickerContent(
                    uiState = uiState,
                    onApiKeySave = onApiKeySave,
                    onImageSelect = onImageSelect,
                    onPromptChange = onPromptChange,
                    onStyleSelect = onStyleSelect,
                    onGenerateClick = onGenerateClick
                )
            }
            
            is GenerationState.Loading -> {
                LoadingContent(state)
            }
            
            is GenerationState.Success -> {
                ResultContent(
                    state = state,
                    enhancementState = uiState.enhancementState,
                    onEnhanceClick = onEnhanceClick,
                    onZoomEnhance = onZoomEnhance,
                    onSave = { bitmap ->
                        scope.launch {
                            context.saveBitmapToGallery(bitmap)
                        }
                    },
                    onReset = onReset
                )
            }
            
            is GenerationState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = onGenerateClick,
                    onReset = onReset
                )
            }
        }
    }
}

@Composable
private fun PickerContent(
    uiState: MainUiState,
    onApiKeySave: (String) -> Unit,
    onImageSelect: (List<Bitmap>) -> Unit,
    onPromptChange: (String) -> Unit,
    onStyleSelect: (Int) -> Unit,
    onGenerateClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        PickerTitle()
        
        ApiKeySetting(
            apiKey = uiState.apiKey,
            onApiKeyChange = onApiKeySave
        )
        
        SelectImages(onImagesSelected = onImageSelect)
        
        if (uiState.selectedImages.isNotEmpty()) {
            PickedImages(images = uiState.selectedImages)
        }
        
        StylePicker(
            selectedIndex = uiState.selectedStyleIndex,
            onStyleSelected = onStyleSelect
        )
        
        Prompt(
            prompt = uiState.currentPrompt,
            onPromptChange = onPromptChange
        )
        
        Generate(
            enabled = uiState.apiKey.isNotBlank() && uiState.selectedImages.isNotEmpty(),
            onClick = onGenerateClick
        )
    }
}

@Composable
private fun LoadingContent(state: GenerationState.Loading) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LoadingEffects()
        
        if (state.message.isNotEmpty()) {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ResultContent(
    state: GenerationState.Success,
    enhancementState: EnhancementResult?,
    onEnhanceClick: () -> Unit,
    onZoomEnhance: (Rect?) -> Unit,
    onSave: (Bitmap) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Show enhanced or original image with zoom capabilities
        state.image?.let { image ->
            EnhancedResultImage(
                bitmap = image,
                isEnhancing = enhancementState is EnhancementResult.Loading,
                onRequestEnhancement = onZoomEnhance,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Show enhance button if we have an image
        if (state.image != null) {
            EnhanceButton(
                enabled = true,
                enhancementState = enhancementState,
                onEnhanceClick = onEnhanceClick
            )
        }
        
        // Show text output if available
        state.text?.let { text ->
            ElegantTextOutput(
                text = text,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Show reasoning if available
        state.reasoning?.let { reasoning ->
            AIReasoningFeedback(
                reasoning = reasoning,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.image?.let { image ->
                Save(onClick = { onSave(image) })
            }
            Reset(onClick = onReset)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "❌ Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Generate(enabled = true, onClick = onRetry)
            Reset(onClick = onReset)
        }
    }
}
