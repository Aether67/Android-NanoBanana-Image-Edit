package com.yunho.nanobanana.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.animations.MotionTokens
import com.yunho.nanobanana.accessibility.DynamicTypography
import com.yunho.nanobanana.accessibility.AccessibilityUtils

/**
 * Elegant text output component with smooth scrolling and accessibility
 * Handles long text responses with adaptive formatting for dark mode
 * Supports text selection and high-contrast accessibility
 * Now includes dynamic typography scaling for responsive text sizing
 */
@Composable
fun ElegantTextOutput(
    text: String,
    modifier: Modifier = Modifier,
    title: String = "AI Response",
    isLoading: Boolean = false,
    maxHeight: Int = 400
) {
    val scrollState = rememberScrollState()
    val isDarkTheme = isSystemInDarkTheme()
    var isVisible by remember { mutableStateOf(false) }
    
    // Dynamic typography scaling
    val scaledTitleSize = DynamicTypography.getScaledTextSize(18.sp)
    val scaledBodySize = DynamicTypography.getScaledTextSize(16.sp)
    val scaledLineHeight = DynamicTypography.calculateLineHeight(scaledBodySize)
    val scaledPadding = DynamicTypography.getScaledSpacing(20.dp)
    
    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            kotlinx.coroutines.delay(100)
            isVisible = true
        }
    }
    
    // Adaptive colors for accessibility
    val backgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val textColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    // Verify color contrast for accessibility
    val accessibleTextColor = remember(textColor, backgroundColor) {
        if (AccessibilityUtils.meetsWCAG_AA(textColor, backgroundColor)) {
            textColor
        } else {
            AccessibilityUtils.enhanceContrast(textColor, backgroundColor)
        }
    }
    
    AnimatedVisibility(
        visible = isVisible || isLoading,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_MEDIUM_3,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_MEDIUM_3,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        )
    ) {
        ElevatedCard(
            modifier = modifier
                .semantics {
                    contentDescription = "Text output card showing: $title"
                },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(scaledPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title with icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = scaledTitleSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (isLoading) {
                        TextGenerationShimmer()
                    }
                }
                
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                
                // Scrollable text content with selection support
                if (isLoading) {
                    // Loading placeholder
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(maxHeight.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(5) {
                            ShimmerTextPlaceholder(
                                widthFraction = if (it % 3 == 0) 0.9f else 1f
                            )
                        }
                    }
                } else {
                    SelectionContainer {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = maxHeight.dp)
                                .verticalScroll(scrollState)
                                .semantics {
                                    contentDescription = "Scrollable text content with ${text.length} characters"
                                }
                        ) {
                            Text(
                                text = text,
                                fontSize = scaledBodySize,
                                color = accessibleTextColor,
                                lineHeight = scaledLineHeight,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    
                    // Scroll indicator
                    if (scrollState.canScrollForward || scrollState.canScrollBackward) {
                        Text(
                            text = "↕ Scroll for more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact text output for short responses
 * Optimized for quick display without scrolling
 */
@Composable
fun CompactTextOutput(
    text: String,
    modifier: Modifier = Modifier,
    icon: String = "💬"
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(text) {
        if (text.isNotEmpty()) {
            isVisible = true
        }
    }
    
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_MEDIUM_2,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        )
    ) {
        Card(
            modifier = modifier.semantics {
                contentDescription = "Compact text message: $text"
            },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
                
                SelectionContainer {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

/**
 * High-contrast accessible text display
 * Optimized for users with visual impairments
 */
@Composable
fun HighContrastTextDisplay(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 18,
    contrastLevel: Float = 1.2f
) {
    val highContrastColors = CardDefaults.cardColors(
        containerColor = Color.Black,
        contentColor = Color.White
    )
    
    Card(
        modifier = modifier.semantics {
            contentDescription = "High contrast text display"
        },
        colors = highContrastColors,
        shape = RoundedCornerShape(8.dp)
    ) {
        SelectionContainer {
            Text(
                text = text,
                fontSize = fontSize.sp,
                color = Color.White,
                lineHeight = (fontSize * 1.5f).sp,
                modifier = Modifier.padding(20.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
