package com.yunho.nanobanana.ai

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * AI Prompt Manager - Flexible, context-aware prompt management system
 * 
 * Supports seamless generation of images and text with coherent contextual alignment.
 * Manages prompt templates, context tracking, and dynamic prompt construction.
 * 
 * Features:
 * - Multi-modal support (image-only, text-only, combined)
 * - Context-aware prompt construction
 * - Template-based prompt generation
 * - User-configurable parameters integration
 */
class PromptManager(context: Context) {
    
    private val sharedPreferences = context.getSharedPreferences("ai_prompt_settings", Context.MODE_PRIVATE)
    
    // AI output mode configuration
    var outputMode by mutableStateOf(AIOutputMode.COMBINED)
    
    // User-configurable parameters
    var creativityLevel by mutableStateOf(
        sharedPreferences.getFloat("creativity_level", 0.7f)
    )
    var detailLevel by mutableStateOf(
        sharedPreferences.getInt("detail_level", 3) // 1-5 scale
    )
    var reasoningDepth by mutableStateOf(
        sharedPreferences.getInt("reasoning_depth", 2) // 1-3 scale
    )
    var outputStyle by mutableStateOf(
        AIOutputStyle.valueOf(
            sharedPreferences.getString("output_style", AIOutputStyle.BALANCED.name) ?: AIOutputStyle.BALANCED.name
        )
    )
    
    // Context tracking for coherent prompt generation
    private var conversationContext = mutableListOf<PromptContext>()
    
    /**
     * Generates a context-aware prompt for AI processing
     * 
     * @param basePrompt The user's base prompt
     * @param mode The output mode (image, text, or combined)
     * @param includeReasoning Whether to request reasoning in the output
     * @return Enhanced prompt optimized for the requested mode
     */
    fun generatePrompt(
        basePrompt: String,
        mode: AIOutputMode = outputMode,
        includeReasoning: Boolean = true
    ): String {
        val enhancedPrompt = buildString {
            // Add mode-specific instructions
            when (mode) {
                AIOutputMode.IMAGE_ONLY -> {
                    append(buildImagePrompt(basePrompt))
                }
                AIOutputMode.TEXT_ONLY -> {
                    append(buildTextPrompt(basePrompt, includeReasoning))
                }
                AIOutputMode.COMBINED -> {
                    append(buildCombinedPrompt(basePrompt, includeReasoning))
                }
            }
            
            // Add quality directives
            append("\n\n")
            append(getQualityDirectives(mode))
            
            // Add context from previous interactions
            if (conversationContext.isNotEmpty()) {
                append("\n\nContext from previous interactions: ")
                append(getRelevantContext())
            }
        }
        
        // Track this prompt in context
        addToContext(basePrompt, mode)
        
        return enhancedPrompt
    }
    
    /**
     * Builds an image-focused prompt with quality directives
     */
    private fun buildImagePrompt(basePrompt: String): String {
        val qualityModifier = when (detailLevel) {
            1 -> "simple, minimalist"
            2 -> "clean, well-composed"
            3 -> "detailed, high-quality"
            4 -> "highly detailed, professional-grade"
            5 -> "hyper-realistic, extremely detailed, masterpiece-quality"
            else -> "high-quality"
        }
        
        return buildString {
            append(basePrompt)
            append(" Generate a $qualityModifier image with proper resolution and clarity.")
            append(" Ensure artifact-free output with natural colors and lighting.")
        }
    }
    
    /**
     * Builds a text-focused prompt with reasoning directives
     */
    private fun buildTextPrompt(basePrompt: String, includeReasoning: Boolean): String {
        return buildString {
            when (outputStyle) {
                AIOutputStyle.FORMAL -> append("In a formal, professional tone: ")
                AIOutputStyle.CASUAL -> append("In a friendly, conversational tone: ")
                AIOutputStyle.TECHNICAL -> append("With technical precision and detail: ")
                AIOutputStyle.CREATIVE -> append("With creative flair and vivid language: ")
                AIOutputStyle.BALANCED -> append("") // No prefix for balanced
            }
            
            append(basePrompt)
            
            if (includeReasoning) {
                append("\n\nProvide ")
                append(when (reasoningDepth) {
                    1 -> "a brief explanation"
                    2 -> "a detailed explanation with reasoning"
                    3 -> "an in-depth analysis with step-by-step reasoning and hypotheses"
                    else -> "an explanation"
                })
                append(" of your response.")
            }
            
            when (detailLevel) {
                1 -> append(" Keep it concise.")
                2 -> append(" Provide moderate detail.")
                3 -> append(" Include comprehensive details.")
                4 -> append(" Provide extensive detail and examples.")
                5 -> append(" Give exhaustive detail with multiple perspectives and examples.")
            }
        }
    }
    
    /**
     * Builds a combined prompt for both image and text generation
     */
    private fun buildCombinedPrompt(basePrompt: String, includeReasoning: Boolean): String {
        return buildString {
            append("Task: ")
            append(basePrompt)
            append("\n\n")
            
            // Image generation instructions
            append("Image Generation:\n")
            append(buildImagePrompt(basePrompt))
            append("\n\n")
            
            // Text Analysis:\n")
            append("Provide ")
            when (outputStyle) {
                AIOutputStyle.FORMAL -> append("a professional analysis")
                AIOutputStyle.CASUAL -> append("a friendly explanation")
                AIOutputStyle.TECHNICAL -> append("a technical breakdown")
                AIOutputStyle.CREATIVE -> append("an insightful commentary")
                AIOutputStyle.BALANCED -> append("a clear explanation")
            }
            append(" of the generated image, including:")
            append("\n- What changes were made and why")
            append("\n- Key visual elements and their significance")
            append("\n- Artistic or technical considerations")
            
            if (includeReasoning) {
                append("\n- Reasoning behind creative decisions")
                append("\n- Hypotheses about viewer perception")
            }
        }
    }
    
    /**
     * Gets quality directives based on output mode
     */
    private fun getQualityDirectives(mode: AIOutputMode): String {
        return when (mode) {
            AIOutputMode.IMAGE_ONLY -> {
                "Ensure high-resolution output without artifacts, compression issues, or visual anomalies."
            }
            AIOutputMode.TEXT_ONLY -> {
                "Ensure coherent, well-structured text without logical inconsistencies or contradictions."
            }
            AIOutputMode.COMBINED -> {
                "Ensure both image and text outputs are of high quality, with coherent alignment between visual and textual content."
            }
        }
    }
    
    /**
     * Adds a prompt to the conversation context
     */
    private fun addToContext(prompt: String, mode: AIOutputMode) {
        conversationContext.add(
            PromptContext(
                prompt = prompt,
                mode = mode,
                timestamp = System.currentTimeMillis()
            )
        )
        
        // Keep only the last 5 contexts for relevance
        if (conversationContext.size > 5) {
            conversationContext.removeAt(0)
        }
    }
    
    /**
     * Gets relevant context from previous interactions
     */
    private fun getRelevantContext(): String {
        return conversationContext.takeLast(2)
            .joinToString("; ") { "Previous: ${it.prompt}" }
    }
    
    /**
     * Clears the conversation context
     */
    fun clearContext() {
        conversationContext.clear()
    }
    
    /**
     * Saves current settings to persistent storage
     */
    fun saveSettings() {
        sharedPreferences.edit().apply {
            putFloat("creativity_level", creativityLevel)
            putInt("detail_level", detailLevel)
            putInt("reasoning_depth", reasoningDepth)
            putString("output_style", outputStyle.name)
            apply()
        }
    }
    
    /**
     * Gets a template prompt for a specific use case
     */
    fun getTemplatePrompt(template: PromptTemplate, customInput: String = ""): String {
        return when (template) {
            PromptTemplate.IMAGE_EXPLANATION -> 
                "Analyze this image and explain what you see, including key visual elements, composition, and artistic choices."
            PromptTemplate.IMAGE_TRANSFORMATION_GUIDE -> 
                "Explain how to transform this image to achieve ${customInput.ifEmpty { "[desired result]" }}, including step-by-step reasoning and expected outcomes."
            PromptTemplate.CREATIVE_SUGGESTION -> 
                "Suggest creative ways to enhance this image, with reasoning for each suggestion."
            PromptTemplate.TECHNICAL_ANALYSIS -> 
                "Provide a technical analysis of this image, including resolution, composition, lighting, and potential improvements."
            PromptTemplate.ARTISTIC_COMMENTARY -> 
                "Offer artistic commentary on this image, discussing style, mood, color theory, and visual impact."
            PromptTemplate.QUALITY_IMPROVEMENT ->
                "Analyze this image for potential quality improvements. Identify any artifacts, composition issues, or color inconsistencies, and suggest specific enhancements."
            PromptTemplate.STYLE_TRANSFER ->
                "Transform this image to ${customInput.ifEmpty { "[target style]" }} style, maintaining the core subject while adapting visual elements to match the new aesthetic."
            PromptTemplate.IMAGE_ENHANCEMENT ->
                "Enhance this image by improving clarity, color balance, and overall visual appeal while preserving natural appearance."
            PromptTemplate.COMPARATIVE_ANALYSIS ->
                "Compare and analyze the differences between the original and transformed versions of this image, highlighting improvements and changes."
        }
    }
    
    /**
     * Applies a template with current parameters
     */
    fun applyTemplate(template: PromptTemplate, customInput: String = ""): String {
        val basePrompt = getTemplatePrompt(template, customInput)
        return generatePrompt(basePrompt, outputMode, includeReasoning = true)
    }
    
    /**
     * Data class for tracking conversation context
     */
    private data class PromptContext(
        val prompt: String,
        val mode: AIOutputMode,
        val timestamp: Long
    )
}

/**
 * AI output modes
 */
enum class AIOutputMode {
    IMAGE_ONLY,      // Generate only image
    TEXT_ONLY,       // Generate only text/reasoning
    COMBINED         // Generate both image and text
}

/**
 * AI output styles for text generation
 */
enum class AIOutputStyle(val displayName: String) {
    FORMAL("Formal & Professional"),
    CASUAL("Casual & Friendly"),
    TECHNICAL("Technical & Precise"),
    CREATIVE("Creative & Vivid"),
    BALANCED("Balanced & Clear")
}

/**
 * Predefined prompt templates
 */
enum class PromptTemplate(val displayName: String) {
    IMAGE_EXPLANATION("Image Explanation"),
    IMAGE_TRANSFORMATION_GUIDE("Transformation Guide"),
    CREATIVE_SUGGESTION("Creative Suggestions"),
    TECHNICAL_ANALYSIS("Technical Analysis"),
    ARTISTIC_COMMENTARY("Artistic Commentary"),
    QUALITY_IMPROVEMENT("Quality Improvement"),
    STYLE_TRANSFER("Style Transfer"),
    IMAGE_ENHANCEMENT("Image Enhancement"),
    COMPARATIVE_ANALYSIS("Comparative Analysis")
}
