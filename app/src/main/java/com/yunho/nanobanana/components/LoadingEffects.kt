package com.yunho.nanobanana.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
 * Blur effect overlay for image processing states
 */
@Composable
fun BlurredLoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(radius = 8.dp)
            ) {
                content()
            }
            
            // Animated progress indicator overlay
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
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
