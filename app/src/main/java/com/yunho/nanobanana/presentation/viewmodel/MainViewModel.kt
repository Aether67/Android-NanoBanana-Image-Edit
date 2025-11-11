package com.yunho.nanobanana.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.EnhancementResult
import com.yunho.nanobanana.domain.model.ImageEnhancementRequest
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
import com.yunho.nanobanana.domain.usecase.EnhanceImageUseCase
import com.yunho.nanobanana.domain.usecase.GenerateAIContentUseCase
import com.yunho.nanobanana.domain.usecase.ManageAIParametersUseCase
import com.yunho.nanobanana.domain.usecase.ManageApiKeyUseCase
import com.yunho.nanobanana.presentation.state.GenerationState
import com.yunho.nanobanana.presentation.state.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for main screen
 * Manages UI state and coordinates use cases
 * Follows MVVM pattern with single state flow
 */
class MainViewModel @Inject constructor(
    private val generateContentUseCase: GenerateAIContentUseCase,
    private val enhanceImageUseCase: EnhanceImageUseCase,
    private val manageApiKeyUseCase: ManageApiKeyUseCase,
    private val manageParametersUseCase: ManageAIParametersUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        observeApiKey()
        observeParameters()
    }
    
    /**
     * Observe API key changes
     */
    private fun observeApiKey() {
        manageApiKeyUseCase.getApiKey()
            .onEach { apiKey ->
                _uiState.update { it.copy(apiKey = apiKey) }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Observe AI parameters changes
     */
    private fun observeParameters() {
        manageParametersUseCase.getParameters()
            .onEach { parameters ->
                _uiState.update { it.copy(aiParameters = parameters) }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Save API key
     */
    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            try {
                manageApiKeyUseCase.saveApiKey(apiKey)
            } catch (e: Exception) {
                // Handle error - could emit to a separate error flow
            }
        }
    }
    
    /**
     * Update AI parameters
     */
    fun updateParameters(parameters: AIParameters) {
        viewModelScope.launch {
            try {
                manageParametersUseCase.updateParameters(parameters)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    /**
     * Add selected images
     */
    fun addSelectedImages(images: List<Bitmap>) {
        _uiState.update { 
            it.copy(selectedImages = it.selectedImages + images)
        }
    }
    
    /**
     * Clear selected images
     */
    fun clearSelectedImages() {
        _uiState.update { 
            it.copy(selectedImages = emptyList())
        }
    }
    
    /**
     * Update current prompt
     */
    fun updatePrompt(prompt: String) {
        _uiState.update { 
            it.copy(currentPrompt = prompt)
        }
    }
    
    /**
     * Update selected style index
     */
    fun updateStyleIndex(index: Int) {
        _uiState.update { 
            it.copy(selectedStyleIndex = index)
        }
    }
    
    /**
     * Update output mode
     */
    fun updateOutputMode(mode: AIOutputMode) {
        _uiState.update { 
            it.copy(outputMode = mode)
        }
    }
    
    /**
     * Generate AI content
     */
    fun generateContent(prompt: String) {
        val currentState = _uiState.value
        
        if (currentState.apiKey.isBlank()) {
            _uiState.update {
                it.copy(generationState = GenerationState.Error("API key is required"))
            }
            return
        }
        
        if (currentState.selectedImages.isEmpty()) {
            _uiState.update {
                it.copy(generationState = GenerationState.Error("Please select at least one image"))
            }
            return
        }
        
        val request = ImageGenerationRequest(
            prompt = prompt,
            bitmaps = currentState.selectedImages,
            outputMode = currentState.outputMode,
            parameters = currentState.aiParameters
        )
        
        generateContentUseCase(request)
            .onEach { result ->
                when (result) {
                    is AIGenerationResult.Loading -> {
                        _uiState.update {
                            it.copy(
                                generationState = GenerationState.Loading(
                                    progress = result.progress,
                                    message = result.message
                                )
                            )
                        }
                    }
                    is AIGenerationResult.Success -> {
                        _uiState.update {
                            it.copy(
                                generationState = GenerationState.Success(
                                    image = result.image,
                                    text = result.text,
                                    reasoning = result.reasoning
                                )
                            )
                        }
                    }
                    is AIGenerationResult.Error -> {
                        _uiState.update {
                            it.copy(
                                generationState = GenerationState.Error(result.message)
                            )
                        }
                    }
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        generationState = GenerationState.Error(
                            e.message ?: "Unknown error occurred"
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Enhance the currently generated image
     */
    fun enhanceImage(targetRegion: android.graphics.Rect? = null) {
        val currentState = _uiState.value
        
        // Get the generated image from current state
        val imageToEnhance = when (val genState = currentState.generationState) {
            is GenerationState.Success -> genState.image
            else -> null
        }
        
        if (imageToEnhance == null) {
            _uiState.update {
                it.copy(
                    enhancementState = EnhancementResult.Error(
                        "No image available to enhance",
                        com.yunho.nanobanana.domain.model.EnhancementErrorReason.UNKNOWN
                    )
                )
            }
            return
        }
        
        // Create enhancement request
        val enhancementType = if (targetRegion != null) {
            com.yunho.nanobanana.domain.model.EnhancementType.LOCALIZED_ENHANCE
        } else {
            com.yunho.nanobanana.domain.model.EnhancementType.DETAIL_SHARPEN
        }
        
        val request = ImageEnhancementRequest(
            image = imageToEnhance,
            enhancementType = enhancementType,
            targetRegion = targetRegion,
            intensity = 0.7f
        )
        
        // Use the enhancement use case
        enhanceImageUseCase(request)
            .onEach { result ->
                _uiState.update { state ->
                    when (result) {
                        is EnhancementResult.Success -> {
                            // Update the generated image with enhanced version
                            state.copy(
                                generationState = (state.generationState as? GenerationState.Success)?.copy(
                                    image = result.enhancedImage
                                ) ?: state.generationState,
                                enhancementState = result
                            )
                        }
                        else -> state.copy(enhancementState = result)
                    }
                }
            }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        enhancementState = EnhancementResult.Error(
                            e.message ?: "Enhancement failed",
                            com.yunho.nanobanana.domain.model.EnhancementErrorReason.UNKNOWN
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }
    
    /**
     * Clear enhancement state
     */
    fun clearEnhancementState() {
        _uiState.update {
            it.copy(enhancementState = null)
        }
    }
    
    /**
     * Reset to idle state
     */
    fun resetToIdle() {
        _uiState.update { 
            it.copy(
                generationState = GenerationState.Idle,
                selectedImages = emptyList(),
                currentPrompt = "",
                enhancementState = null
            )
        }
    }
    
    // ========== Variant Management ==========
    
    /**
     * Save current image as a variant
     * Creates a new variant from the currently generated or enhanced image
     */
    fun saveAsVariant() {
        val currentState = _uiState.value
        
        // Get the current image from generation state
        val image = when (val genState = currentState.generationState) {
            is GenerationState.Success -> genState.image
            else -> null
        }
        
        if (image == null) return
        
        // Determine if this is an enhanced version
        val isEnhanced = currentState.enhancementState is EnhancementResult.Success
        val enhancementTime = if (isEnhanced) {
            (currentState.enhancementState as? EnhancementResult.Success)?.processingTimeMs ?: 0
        } else {
            0
        }
        
        // Create variant
        val variant = com.yunho.nanobanana.domain.model.ImageVariant(
            image = image,
            prompt = currentState.currentPrompt,
            isEnhanced = isEnhanced,
            metadata = com.yunho.nanobanana.domain.model.VariantMetadata(
                enhancementTimeMs = enhancementTime,
                style = getStyleName(currentState.selectedStyleIndex),
                parameters = currentState.aiParameters
            )
        )
        
        // Add to collection
        _uiState.update {
            it.copy(variants = it.variants.addVariant(variant))
        }
    }
    
    /**
     * Select a variant by ID
     */
    fun selectVariant(id: String) {
        _uiState.update {
            it.copy(variants = it.variants.selectVariant(id))
        }
        
        // Optionally, update the generated image to show the selected variant
        val selectedVariant = _uiState.value.variants.selectedVariant
        if (selectedVariant != null) {
            _uiState.update {
                it.copy(
                    generationState = GenerationState.Success(
                        image = selectedVariant.image,
                        text = null,
                        reasoning = null
                    )
                )
            }
        }
    }
    
    /**
     * Delete a variant by ID
     */
    fun deleteVariant(id: String) {
        _uiState.update {
            it.copy(variants = it.variants.removeVariant(id))
        }
    }
    
    /**
     * Clear all variants
     */
    fun clearVariants() {
        _uiState.update {
            it.copy(variants = it.variants.clear())
        }
    }
    
    /**
     * Get style name from index
     */
    private fun getStyleName(index: Int): String {
        return when (index) {
            0 -> "Photorealistic"
            1 -> "Cartoon"
            2 -> "Anime"
            3 -> "Watercolor"
            4 -> "Oil Painting"
            5 -> "Sketch"
            else -> "Unknown"
        }
    }
}
