package com.yunho.nanobanana.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.animations.MotionTokens
import kotlinx.coroutines.delay

/**
 * AI Reasoning Feedback - Real-time AI reasoning and guidance component
 * 
 * Provides dynamic, context-aware explanations and suggestions during image processing.
 * Features:
 * - Real-time feedback during AI processing
 * - Step-by-step reasoning visualization
 * - Hypothesis generation and explanation
 * - Interactive guidance for users
 * - Adaptive formatting based on reasoning depth
 */
@Composable
fun AIReasoningFeedback(
    reasoning: AIReasoning,
    modifier: Modifier = Modifier,
    isActive: Boolean = true
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(reasoning) {
        if (isActive) {
            delay(100)
            isVisible = true
        }
    }
    
    AnimatedVisibility(
        visible = isVisible && isActive,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_MEDIUM_3,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        ) + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ElevatedCard(
            modifier = modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "AI reasoning feedback for current operation"
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with icon
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PulsingThinkingIcon()
                    
                    Column {
                        Text(
                            text = "AI Reasoning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = reasoning.stage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Main reasoning content
                if (reasoning.steps.isNotEmpty()) {
                    ReasoningSteps(steps = reasoning.steps)
                }
                
                // Current thought/hypothesis
                if (reasoning.currentThought.isNotEmpty()) {
                    CurrentThought(thought = reasoning.currentThought)
                }
                
                // Suggestions or guidance
                if (reasoning.suggestions.isNotEmpty()) {
                    SuggestionsList(suggestions = reasoning.suggestions)
                }
                
                // Confidence indicator
                if (reasoning.confidence > 0f) {
                    ConfidenceIndicator(confidence = reasoning.confidence)
                }
            }
        }
    }
}

/**
 * Pulsing thinking icon animation
 */
@Composable
private fun PulsingThinkingIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinking")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_pulse"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_alpha"
    )
    
    Box(
        modifier = Modifier
            .size((32 * scale).dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🤔",
            fontSize = (20 * scale).sp
        )
    }
}

/**
 * Displays reasoning steps with staggered animation
 */
@Composable
private fun ReasoningSteps(steps: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Reasoning Steps:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        
        steps.forEachIndexed { index, step ->
            var isVisible by remember { mutableStateOf(false) }
            
            LaunchedEffect(step) {
                delay(index * 150L)
                isVisible = true
            }
            
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${index + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * Displays current AI thought/hypothesis
 */
@Composable
private fun CurrentThought(thought: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "💭", fontSize = 20.sp)
            Text(
                text = thought,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Displays AI suggestions list
 */
@Composable
private fun SuggestionsList(suggestions: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Suggestions:",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        
        suggestions.forEach { suggestion ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(text = "💡", fontSize = 16.sp)
                Text(
                    text = suggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * Displays AI confidence level
 */
@Composable
private fun ConfidenceIndicator(confidence: Float) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Confidence",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "${(confidence * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = when {
                    confidence >= 0.8f -> Color(0xFF4CAF50)
                    confidence >= 0.5f -> Color(0xFFFFA726)
                    else -> Color(0xFFEF5350)
                }
            )
        }
        
        LinearProgressIndicator(
            progress = { confidence },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = when {
                confidence >= 0.8f -> Color(0xFF4CAF50)
                confidence >= 0.5f -> Color(0xFFFFA726)
                else -> Color(0xFFEF5350)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Data class for AI reasoning information
 */
data class AIReasoning(
    val stage: String = "Processing",
    val steps: List<String> = emptyList(),
    val currentThought: String = "",
    val suggestions: List<String> = emptyList(),
    val confidence: Float = 0f
) {
    companion object {
        /**
         * Creates reasoning feedback for image generation
         */
        fun forImageGeneration(prompt: String): AIReasoning {
            return AIReasoning(
                stage = "Analyzing Image Generation Request",
                steps = listOf(
                    "Analyzing prompt requirements and creative intent",
                    "Determining optimal composition and visual elements",
                    "Planning color palette and lighting strategy",
                    "Generating high-quality image with artifact prevention"
                ),
                currentThought = "Creating a balanced composition that aligns with your vision while maintaining visual coherence and quality.",
                suggestions = listOf(
                    "Consider the emotional impact of chosen colors",
                    "Ensure visual hierarchy guides viewer attention",
                    "Maintain consistency with the requested style"
                ),
                confidence = 0.85f
            )
        }
        
        /**
         * Creates reasoning feedback for text generation
         */
        fun forTextGeneration(topic: String): AIReasoning {
            return AIReasoning(
                stage = "Generating Insightful Analysis",
                steps = listOf(
                    "Understanding the core concepts and context",
                    "Identifying key points and relationships",
                    "Structuring coherent and logical explanation",
                    "Refining language for clarity and engagement"
                ),
                currentThought = "Crafting a response that balances technical accuracy with accessibility and engagement.",
                confidence = 0.90f
            )
        }
        
        /**
         * Creates reasoning feedback for combined mode
         */
        fun forCombinedGeneration(prompt: String): AIReasoning {
            return AIReasoning(
                stage = "Coordinating Image and Text Generation",
                steps = listOf(
                    "Analyzing prompt for image and text requirements",
                    "Ensuring coherent alignment between visual and textual elements",
                    "Generating synchronized outputs with consistent messaging",
                    "Validating quality and contextual coherence"
                ),
                currentThought = "Ensuring that both image and text outputs tell a unified, compelling story.",
                suggestions = listOf(
                    "Visual elements should reinforce textual explanations",
                    "Text should provide context that enhances image understanding",
                    "Maintain consistent tone and style across both outputs"
                ),
                confidence = 0.80f
            )
        }
    }
}
