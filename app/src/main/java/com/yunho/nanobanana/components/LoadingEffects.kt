package com.yunho.nanobanana.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.aspectRatio
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Shimmer loading effect component for displaying loading states
 * Uses Material Design 3 colors and smooth animations
 */
@Composable
fun ShimmerLoadingBox(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    contentAfterLoading: @Composable () -> Unit = {}
) {
    if (isLoading) {
        val shimmerColors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )

        Box(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = shimmerColors,
                        start = Offset(translateAnim - 1000f, translateAnim - 1000f),
                        end = Offset(translateAnim, translateAnim)
                    )
                )
        )
    } else {
        contentAfterLoading()
    }
}

/**
 * Skeleton loader for image cards with smooth pulsing animation
 */
@Composable
fun SkeletonImageCard(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

/**
 * Multi-layered blur effect overlay with adaptive intensity
 * Dynamically adjusts blur based on image size and loading duration
 */
@Composable
fun BlurredLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    imageSize: Int = 1000,
    content: @Composable () -> Unit
) {
    // Adaptive blur intensity based on image size
    val baseBlurRadius = when {
        imageSize > 2000 -> 12.dp
        imageSize > 1000 -> 10.dp
        else -> 8.dp
    }
    
    // Animated blur intensity that increases over time
    val infiniteTransition = rememberInfiniteTransition(label = "blur_transition")
    val blurMultiplier by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blur_multiplier"
    )
    
    Box(modifier = modifier) {
        if (isLoading) {
            // Multi-layered blur effect
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(radius = baseBlurRadius * blurMultiplier)
            ) {
                content()
            }
            
            // Semi-transparent overlay with gradient
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedLoadingIndicator()
                }
            }
        } else {
            content()
        }
    }
}

/**
 * Custom animated loading indicator with Material Motion
 */
@Composable
fun AnimatedLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_rotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer circle with rotation
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.primary
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

/**
 * Shimmer effect for text placeholders
 */
@Composable
fun ShimmerTextPlaceholder(
    modifier: Modifier = Modifier,
    widthFraction: Float = 0.7f
) {
    ShimmerLoadingBox(
        modifier = modifier
            .fillMaxWidth(widthFraction)
            .height(20.dp)
    )
}

/**
 * Progressive loading skeleton for the style picker carousel
 */
@Composable
fun StylePickerSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerTextPlaceholder(widthFraction = 0.4f)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) {
                    SkeletonImageCard(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Ripple animation effect for AI processing indication
 * Creates expanding circular ripples synchronized with generation
 */
@Composable
fun RippleProcessingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ripple_animation")
    
    // Multiple ripples with staggered delays
    val rippleScale1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_1"
    )
    
    val rippleScale2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_2"
    )
    
    val rippleScale3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_3"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // First ripple
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = rippleScale1
                    scaleY = rippleScale1
                    alpha = 1f - (rippleScale1 / 2f)
                }
                .background(
                    color = color.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
        )
        
        // Second ripple
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = rippleScale2
                    scaleY = rippleScale2
                    alpha = 1f - (rippleScale2 / 2f)
                }
                .background(
                    color = color.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
        )
        
        // Third ripple
        Box(
            modifier = Modifier
                .size(60.dp)
                .graphicsLayer {
                    scaleX = rippleScale3
                    scaleY = rippleScale3
                    alpha = 1f - (rippleScale3 / 2f)
                }
                .background(
                    color = color.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(50)
                )
        )
    }
}

/**
 * Enhanced shimmer effect with synchronized wave animation
 * Indicates ongoing AI text generation processing
 */
@Composable
fun TextGenerationShimmer(
    modifier: Modifier = Modifier,
    text: String = "Generating response..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_shimmer")
    
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = shimmerAlpha)
        )
        
        // Animated dots
        repeat(3) { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 200,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = dotAlpha),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

/**
 * Skeleton loader for prompt input area with shimmer effect
 * Provides visual feedback during initialization or loading
 */
@Composable
fun PromptInputSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerTextPlaceholder(widthFraction = 0.3f)
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerLoadingBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
        }
    }
}

/**
 * Synchronized cross-fade transition between loading and content states
 * Provides smooth visual continuity during state changes
 */
@Composable
fun SynchronizedTransition(
    isLoading: Boolean,
    loadingContent: @Composable () -> Unit,
    actualContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.animation.Crossfade(
        targetState = isLoading,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        modifier = modifier,
        label = "state_transition"
    ) { loading ->
        if (loading) {
            loadingContent()
        } else {
            actualContent()
        }
    }
}

/**
 * Enhanced AI output panel skeleton with shimmer
 * Shows placeholder for both image and text output regions
 */
@Composable
fun AIOutputPanelSkeleton(
    modifier: Modifier = Modifier,
    showImagePlaceholder: Boolean = true,
    showTextPlaceholder: Boolean = true
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showImagePlaceholder) {
                ShimmerTextPlaceholder(widthFraction = 0.5f)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonImageCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }
            
            if (showTextPlaceholder) {
                ShimmerTextPlaceholder(widthFraction = 0.4f)
                Spacer(modifier = Modifier.height(4.dp))
                repeat(4) {
                    ShimmerTextPlaceholder(
                        widthFraction = if (it % 2 == 0) 0.95f else 0.8f
                    )
                }
            }
        }
    }
}
