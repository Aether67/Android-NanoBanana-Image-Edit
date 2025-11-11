package com.yunho.nanobanana.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.yunho.nanobanana.ai.AIOutputMode
import com.yunho.nanobanana.ai.AIOutputStyle
import com.yunho.nanobanana.ai.PromptManager
import com.yunho.nanobanana.animations.MotionTokens

/**
 * AI Parameter Controls - User-configurable parameters for AI output
 * 
 * Provides comprehensive controls for:
 * - Output style (formal, casual, technical, creative, balanced)
 * - Creativity level (0.0 - 1.0)
 * - Detail level (1-5)
 * - Reasoning depth (1-3)
 * - Output mode (image-only, text-only, combined)
 * 
 * All settings are persisted and applied to AI generation
 */
@Composable
fun AIParameterControls(
    promptManager: PromptManager,
    modifier: Modifier = Modifier,
    onParametersChanged: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "AI parameter controls for customizing output"
            }
    ) {
        // Expandable header
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️",
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = "AI Parameters",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Customize AI behavior",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = if (isExpanded) "▲" else "▼",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Expandable content
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_MEDIUM_3,
                    easing = MotionTokens.EasingEmphasizedDecelerate
                )
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_MEDIUM_2,
                    easing = MotionTokens.EasingEmphasizedAccelerate
                )
            ) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Output Mode Selection
                    OutputModeSelector(
                        currentMode = promptManager.outputMode,
                        onModeSelected = { 
                            promptManager.outputMode = it
                            onParametersChanged()
                        }
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    // Output Style Selection
                    OutputStyleSelector(
                        currentStyle = promptManager.outputStyle,
                        onStyleSelected = { 
                            promptManager.outputStyle = it
                            promptManager.saveSettings()
                            onParametersChanged()
                        }
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    
                    // Creativity Level Slider
                    ParameterSlider(
                        label = "Creativity Level",
                        value = promptManager.creativityLevel,
                        onValueChange = { 
                            promptManager.creativityLevel = it
                            promptManager.saveSettings()
                            onParametersChanged()
                        },
                        valueRange = 0f..1f,
                        steps = 9,
                        icon = "🎨",
                        description = "Controls AI's creative freedom"
                    )
                    
                    // Detail Level Selector
                    DetailLevelSelector(
                        currentLevel = promptManager.detailLevel,
                        onLevelSelected = { 
                            promptManager.detailLevel = it
                            promptManager.saveSettings()
                            onParametersChanged()
                        }
                    )
                    
                    // Reasoning Depth Selector
                    ReasoningDepthSelector(
                        currentDepth = promptManager.reasoningDepth,
                        onDepthSelected = { 
                            promptManager.reasoningDepth = it
                            promptManager.saveSettings()
                            onParametersChanged()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Output mode selector component
 */
@Composable
private fun OutputModeSelector(
    currentMode: AIOutputMode,
    onModeSelected: (AIOutputMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📤", fontSize = 20.sp)
            Text(
                text = "Output Mode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AIOutputMode.values().forEach { mode ->
                val isSelected = mode == currentMode
                val (icon, label) = when (mode) {
                    AIOutputMode.IMAGE_ONLY -> "🖼️" to "Image"
                    AIOutputMode.TEXT_ONLY -> "📝" to "Text"
                    AIOutputMode.COMBINED -> "🔗" to "Both"
                }
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onModeSelected(mode) },
                    label = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = icon, fontSize = 16.sp)
                            Text(text = label, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

/**
 * Output style selector component
 */
@Composable
private fun OutputStyleSelector(
    currentStyle: AIOutputStyle,
    onStyleSelected: (AIOutputStyle) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "✨", fontSize = 20.sp)
            Text(
                text = "Writing Style",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AIOutputStyle.values().chunked(2).forEach { styleRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    styleRow.forEach { style ->
                        val isSelected = style == currentStyle
                        
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStyleSelected(style) },
                            label = { Text(text = style.displayName, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                    // Add spacer if odd number of styles in row
                    if (styleRow.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Parameter slider component
 */
@Composable
private fun ParameterSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    icon: String,
    description: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 20.sp)
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
            
            Text(
                text = "%.1f".format(value),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

/**
 * Detail level selector component
 */
@Composable
private fun DetailLevelSelector(
    currentLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "📊", fontSize = 20.sp)
            Column {
                Text(
                    text = "Detail Level",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Amount of information in output",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            (1..5).forEach { level ->
                val isSelected = level == currentLevel
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onLevelSelected(level) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = level.toString(),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Reasoning depth selector component
 */
@Composable
private fun ReasoningDepthSelector(
    currentDepth: Int,
    onDepthSelected: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🧠", fontSize = 20.sp)
            Column {
                Text(
                    text = "Reasoning Depth",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Level of explanation and analysis",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                1 to "Brief",
                2 to "Detailed",
                3 to "In-Depth"
            ).forEach { (depth, label) ->
                val isSelected = depth == currentDepth
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onDepthSelected(depth) },
                    label = { Text(text = label, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
