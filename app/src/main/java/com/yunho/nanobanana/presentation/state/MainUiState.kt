package com.yunho.nanobanana.presentation.state

import android.graphics.Bitmap
import com.yunho.nanobanana.domain.model.AIOutputMode
import com.yunho.nanobanana.domain.model.AIParameters

/**
 * UI state for the main screen
 * Immutable data class representing the complete UI state
 */
data class MainUiState(
    val apiKey: String = "",
    val selectedImages: List<Bitmap> = emptyList(),
    val currentPrompt: String = "",
    val selectedStyleIndex: Int = 0,
    val aiParameters: AIParameters = AIParameters(),
    val outputMode: AIOutputMode = AIOutputMode.COMBINED,
    val generationState: GenerationState = GenerationState.Idle
)

/**
 * State of AI generation process
 */
sealed class GenerationState {
    /**
     * No generation in progress
     */
    data object Idle : GenerationState()
    
    /**
     * Generation in progress
     */
    data class Loading(
        val progress: Float = 0f,
        val message: String = "Generating..."
    ) : GenerationState()
    
    /**
     * Generation completed successfully
     */
    data class Success(
        val image: Bitmap? = null,
        val text: String? = null,
        val reasoning: String? = null
    ) : GenerationState()
    
    /**
     * Generation failed
     */
    data class Error(
        val message: String
    ) : GenerationState()
}
