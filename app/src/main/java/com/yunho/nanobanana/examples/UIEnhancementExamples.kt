package com.yunho.nanobanana.examples

import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.accessibility.DynamicTypography
import com.yunho.nanobanana.accessibility.rememberAccessibleTextColor
import com.yunho.nanobanana.components.*
import com.yunho.nanobanana.gestures.swipeWithIndicators

/**
 * Example demonstrating complete UI/UX enhancement integration
 * Shows skeleton loaders, accessibility features, swipe gestures, and dynamic typography
 */
@Composable
fun EnhancedAIGenerationExample(
    isGenerating: Boolean,
    generatedImage: Bitmap?,
    generatedText: String,
    onGenerate: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean
) {
    // Dynamic typography
    val scaledPadding = DynamicTypography.getScaledSpacing(16.dp)
    val scaledTitleSize = DynamicTypography.getScaledTextSize(20.sp)
    
    // Accessible colors
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = rememberAccessibleTextColor(backgroundColor)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaledPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title with accessible text
        Text(
            text = "AI Generation Demo",
            fontSize = scaledTitleSize,
            color = textColor,
            modifier = Modifier.semantics {
                contentDescription = "AI Generation demonstration screen"
            }
        )
        
        // Generation button
        Button(
            onClick = onGenerate,
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = DynamicTypography.getMinimumTouchTarget())
        ) {
            Text(if (isGenerating) "Generating..." else "Generate")
        }
        
        // Output with synchronized transition
        SynchronizedTransition(
            isLoading = isGenerating,
            loadingContent = {
                // Show skeleton during generation
                AIOutputPanelSkeleton(
                    showImagePlaceholder = true,
                    showTextPlaceholder = true
                )
            },
            actualContent = {
                // Show actual content when ready
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.swipeWithIndicators(
                        enabled = true,
                        onUndo = onUndo,
                        onRedo = onRedo,
                        undoAvailable = canUndo,
                        redoAvailable = canRedo
                    )
                ) {
                    // Image output
                    if (generatedImage != null) {
                        ResultImage(bitmap = generatedImage)
                    }
                    
                    // Text output with dynamic typography
                    if (generatedText.isNotEmpty()) {
                        ElegantTextOutput(
                            text = generatedText,
                            title = "AI Response",
                            isLoading = false,
                            maxHeight = 300
                        )
                    }
                }
            }
        )
        
        // Swipe instruction
        if (!isGenerating && (canUndo || canRedo)) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "💡 Tip: Swipe left to undo, right to redo",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Example showing skeleton loaders for different states
 */
@Composable
fun SkeletonLoadingExample() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Skeleton Loaders Demo",
            style = MaterialTheme.typography.titleLarge
        )
        
        // Prompt input skeleton
        Text(
            text = "Prompt Input Loading:",
            style = MaterialTheme.typography.titleSmall
        )
        PromptInputSkeleton()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // AI output skeleton
        Text(
            text = "AI Output Loading:",
            style = MaterialTheme.typography.titleSmall
        )
        AIOutputPanelSkeleton(
            showImagePlaceholder = true,
            showTextPlaceholder = true
        )
    }
}

/**
 * Example demonstrating accessibility features
 */
@Composable
fun AccessibilityFeaturesExample() {
    val screenSize = DynamicTypography.getScreenSize()
    val scaledSpacing = DynamicTypography.getScaledSpacing(20.dp)
    val scaledFontSize = DynamicTypography.getScaledTextSize(16.sp)
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(scaledSpacing),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Accessibility Features Demo",
            style = MaterialTheme.typography.titleLarge
        )
        
        // Screen size info
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Current Screen Size: $screenSize",
                    fontSize = scaledFontSize
                )
                Text(
                    text = "Scaled Font: ${scaledFontSize.value}sp",
                    fontSize = scaledFontSize
                )
                Text(
                    text = "Scaled Spacing: ${scaledSpacing.value}dp",
                    fontSize = scaledFontSize
                )
            }
        }
        
        // High contrast text
        Text(
            text = "High Contrast Text:",
            style = MaterialTheme.typography.titleSmall
        )
        HighContrastTextDisplay(
            text = "This text has maximum contrast for accessibility",
            fontSize = 18,
            contrastLevel = 1.5f
        )
        
        // Regular accessible text
        Text(
            text = "Standard Accessible Text:",
            style = MaterialTheme.typography.titleSmall
        )
        ElegantTextOutput(
            text = "This text uses dynamic typography and verified WCAG AA contrast ratios for optimal readability across all screen sizes and themes.",
            title = "Accessible Content",
            isLoading = false
        )
    }
}

/**
 * Example showing swipe gesture functionality
 */
@Composable
fun SwipeGestureExample() {
    var actionHistory by remember { mutableStateOf(listOf<String>()) }
    var historyIndex by remember { mutableStateOf(-1) }
    
    val canUndo = historyIndex >= 0
    val canRedo = historyIndex < actionHistory.size - 1
    
    fun addAction(action: String) {
        // Remove any actions after current position
        actionHistory = actionHistory.take(historyIndex + 1) + action
        historyIndex = actionHistory.lastIndex
    }
    
    fun undo() {
        if (canUndo) {
            historyIndex--
        }
    }
    
    fun redo() {
        if (canRedo) {
            historyIndex++
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Swipe Gestures Demo",
            style = MaterialTheme.typography.titleLarge
        )
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { addAction("Action ${actionHistory.size + 1}") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add Action")
            }
            Button(
                onClick = { undo() },
                enabled = canUndo,
                modifier = Modifier.weight(1f)
            ) {
                Text("Undo")
            }
            Button(
                onClick = { redo() },
                enabled = canRedo,
                modifier = Modifier.weight(1f)
            ) {
                Text("Redo")
            }
        }
        
        // Swipeable card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .swipeWithIndicators(
                    enabled = true,
                    onUndo = { undo() },
                    onRedo = { redo() },
                    undoAvailable = canUndo,
                    redoAvailable = canRedo
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "← Swipe to Undo | Swipe to Redo →",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Current Action: ${if (historyIndex >= 0) actionHistory[historyIndex] else "None"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Text(
                    text = "History: ${actionHistory.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        // Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Can Undo: ${if (canUndo) "✓" else "✗"}",
                color = if (canUndo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = "Can Redo: ${if (canRedo) "✓" else "✗"}",
                color = if (canRedo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
