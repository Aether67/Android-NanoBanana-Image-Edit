package com.yunho.nanobanana.integration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yunho.nanobanana.ai.PromptManager
import com.yunho.nanobanana.components.AIParameterControls
import com.yunho.nanobanana.components.AIReasoningFeedback
import com.yunho.nanobanana.components.AIReasoning
import com.yunho.nanobanana.components.ElegantTextOutput

/**
 * AI Integration Extensions for MainActivity
 * 
 * These composables extend the existing MainActivity with AI capabilities:
 * - AIParameterControls for user configuration
 * - AIReasoningFeedback for real-time guidance
 * - ElegantTextOutput for AI-generated text display
 * 
 * Usage: Add these components to the existing PickerContent, LoadingContent,
 * and ResultContent composables in MainActivity.kt
 */

/**
 * AI Controls Section
 * Add this to PickerContent after ApiKeySetting
 */
@Composable
fun AIControlsSection(
    promptManager: PromptManager,
    modifier: Modifier = Modifier
) {
    AIParameterControls(
        promptManager = promptManager,
        modifier = modifier
    )
}

/**
 * AI Reasoning Display
 * Add this to LoadingContent to show real-time AI reasoning
 */
@Composable
fun AIReasoningDisplay(
    reasoning: AIReasoning,
    modifier: Modifier = Modifier
) {
    AIReasoningFeedback(
        reasoning = reasoning,
        modifier = modifier,
        isActive = true
    )
}

/**
 * AI Text Result Display
 * Add this to ResultContent when text output is available
 */
@Composable
fun AITextResultDisplay(
    text: String?,
    modifier: Modifier = Modifier
) {
    text?.let {
        Spacer(modifier = Modifier.height(16.dp))
        ElegantTextOutput(
            text = it,
            title = "AI Analysis",
            modifier = modifier
        )
    }
}

/**
 * Helper function to create PromptManager
 */
@Composable
fun rememberPromptManager(): PromptManager {
    val context = LocalContext.current
    return remember { PromptManager(context) }
}
