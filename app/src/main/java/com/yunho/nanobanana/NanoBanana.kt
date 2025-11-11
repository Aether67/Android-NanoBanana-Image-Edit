package com.yunho.nanobanana

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import com.yunho.nanobanana.NanoBanana.Content.Picker
import com.yunho.nanobanana.ai.PromptManager
import com.yunho.nanobanana.ai.EnhancedAIService
import com.yunho.nanobanana.ai.AIGenerationResult
import com.yunho.nanobanana.ai.AIOutputMode
import com.yunho.nanobanana.components.AIReasoning
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow

/**
 * Enhanced NanoBanana core logic with Kotlin Flow and StateFlow
 * Provides real-time UI updates with reactive state management
 * Now includes AI reasoning capabilities and multi-modal output support
 */
class NanoBanana(
    private val nanoBananaService: NanoBananaService,
    val promptManager: PromptManager,
) : Channel<NanoBanana.Request> by Channel() {
    
    // Enhanced AI Service for multi-modal generation
    private var enhancedAIService: EnhancedAIService? = null
    
    // StateFlow for reactive state management
    private val _contentState = MutableStateFlow<Content>(Picker(queue = this))
    val contentState: StateFlow<Content> = _contentState.asStateFlow()
    
    // Legacy mutableState for backward compatibility
    val content = _contentState
    
    suspend fun launch() {
        consumeAsFlow().collect { request ->
            // Initialize enhanced service if not already done
            if (enhancedAIService == null && nanoBananaService.apiKey.isNotBlank()) {
                enhancedAIService = EnhancedAIService(nanoBananaService.apiKey, promptManager)
            }
            
            // Update state to loading with reasoning
            val reasoning = when (promptManager.outputMode) {
                AIOutputMode.IMAGE_ONLY -> AIReasoning.forImageGeneration(request.prompt)
                AIOutputMode.TEXT_ONLY -> AIReasoning.forTextGeneration(request.prompt)
                AIOutputMode.COMBINED -> AIReasoning.forCombinedGeneration(request.prompt)
            }
            _contentState.value = Content.Loading(reasoning = reasoning)
            
            // Try enhanced service first if available, fallback to legacy
            val result = if (enhancedAIService != null && promptManager.outputMode != AIOutputMode.IMAGE_ONLY) {
                // Use enhanced service for multi-modal support
                val enhancedResult = enhancedAIService!!.generateWithMode(
                    basePrompt = request.prompt,
                    bitmaps = request.selectedBitmaps,
                    mode = promptManager.outputMode
                )
                
                when (enhancedResult) {
                    is AIGenerationResult.Success -> enhancedResult
                    is AIGenerationResult.Error -> {
                        Log.e("NanoBanana", "Enhanced service failed: ${enhancedResult.message}")
                        null
                    }
                }
            } else {
                // Use legacy service for image-only mode
                val bitmap = nanoBananaService.editImage(
                    prompt = request.prompt,
                    bitmaps = request.selectedBitmaps
                )
                if (bitmap != null) {
                    AIGenerationResult.Success(image = bitmap)
                } else {
                    null
                }
            }
            
            // Update state with result or error
            _contentState.value = when (result) {
                is AIGenerationResult.Success -> {
                    Content.Result(
                        resultImage = result.image,
                        resultText = result.text,
                        contentState = _contentState,
                        queue = this
                    )
                }
                else -> {
                    Content.Error(
                        message = "Failed to generate output. Please check your API key and try again.",
                        contentState = _contentState,
                        queue = this
                    )
                }
            }
        }
    }

    data class Request(
        val prompt: String,
        val selectedBitmaps: List<Bitmap>
    )

    sealed interface Content {

        interface Reset {
            val contentState: MutableStateFlow<Content>
            val queue: Channel<Request>

            fun reset() {
                contentState.value = Picker(queue = queue)
            }
        }

        data class Picker(
            val queue: Channel<Request>
        ) : Content {
            val selectedBitmaps = mutableStateListOf<Bitmap>()
            val pagerState = PagerState { PROMPTS.size }

            fun getImage(index: Int) = IMAGES[index]
            val prompt get() = PROMPTS[pagerState.currentPage]

            suspend fun request() {
                queue.send(
                    element = Request(
                        prompt = PROMPTS[pagerState.currentPage],
                        selectedBitmaps = selectedBitmaps
                    )
                )
            }

            fun select(
                context: Context,
                uris: List<Uri>
            ) {
                val bitmaps = uris.mapNotNull { uri ->
                    try {
                        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                    } catch (e: Exception) {
                        Log.e("ImagePicker", "Error converting URI to Bitmap", e)
                        null
                    }
                }

                selectedBitmaps.addAll(bitmaps)
            }

            companion object {
                private val PROMPTS = listOf(
                    "Transform the photo as if it were taken in Korea in the 1900s, the chosun dynasty",
                    "Create a hyper-realistic photo of a collectible figure placed on a computer desk,  with its packaging box visible in the background.",
                    "Create an image of the person from image 1 and the person from image 2 playing rock-paper-scissors. The person from image 1 shows 'rock' and is the winner, cheering excitedly. The person from image 2 shows 'scissors' and is the loser, looking very disappointed.",
                    "two people are shopping in costco (3d)"
                )

                private val IMAGES = listOf(
                    R.drawable.chosen,
                    R.drawable.figure,
                    R.drawable.rsp,
                    R.drawable.costco
                )
            }
        }

        data class Loading(
            val reasoning: AIReasoning = AIReasoning()
        ) : Content
        
        data class Result(
            val resultImage: Bitmap? = null,
            val resultText: String? = null,
            override val contentState: MutableStateFlow<Content>,
            override val queue: Channel<Request>
        ) : Content, Reset {
            // Backward compatibility
            val result: Bitmap? get() = resultImage
        }

        data class Error(
            val message: String,
            override val contentState: MutableStateFlow<Content>,
            override val queue: Channel<Request>
        ) : Content, Reset
    }

    companion object {
        @Composable
        fun rememberNanoBanana(
            nanoBananaService: NanoBananaService,
            promptManager: PromptManager
        ): NanoBanana = remember { 
            NanoBanana(
                nanoBananaService = nanoBananaService,
                promptManager = promptManager
            )
        }
    }
}
