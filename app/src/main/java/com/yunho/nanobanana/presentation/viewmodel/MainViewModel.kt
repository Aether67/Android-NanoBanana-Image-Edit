package com.yunho.nanobanana.presentation.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yunho.nanobanana.domain.model.AIGenerationResult
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters
import com.yunho.nanobanana.domain.model.ImageGenerationRequest
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
     * Reset to idle state
     */
    fun resetToIdle() {
        _uiState.update { 
            it.copy(
                generationState = GenerationState.Idle,
                selectedImages = emptyList(),
                currentPrompt = ""
            )
        }
    }
}
