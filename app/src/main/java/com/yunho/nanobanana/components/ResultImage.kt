package com.yunho.nanobanana.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.animations.MotionTokens

/**
 * Enhanced result image component with Material 3 animations, haptics, and gesture support
 * Includes pinch-to-zoom and interactive elevation feedback
 */
@Composable
fun ResultImage(
    bitmap: Bitmap,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    var currentZoom by remember { mutableStateOf(1f) }
    
    LaunchedEffect(bitmap) {
        isVisible = true
    }
    
    // Animated scale for entrance with spring physics
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "result_scale"
    )
    
    ElevatedCard(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics {
                contentDescription = "Generated AI image result card with zoom support"
            },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = MotionTokens.DURATION_MEDIUM_2,
                        easing = MotionTokens.EasingEmphasizedDecelerate
                    )
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "✨ Generated Image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (currentZoom > 1f) {
                        Text(
                            text = "Zoom: ${String.format("%.1f", currentZoom)}x",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(
                    animationSpec = tween(
                        durationMillis = MotionTokens.DURATION_MEDIUM_4,
                        delayMillis = 100,
                        easing = MotionTokens.EasingEmphasizedDecelerate
                    )
                ) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DURATION_MEDIUM_4,
                        delayMillis = 100,
                        easing = MotionTokens.EasingEmphasizedDecelerate
                    )
                )
            ) {
                Card(
                    modifier = Modifier
                        .size(320.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp))
                        .zoomableImage(
                            minScale = 1f,
                            maxScale = 3f,
                            onZoomChange = { zoom -> currentZoom = zoom }
                        )
                        .semantics {
                            contentDescription = "AI generated high resolution image. Double tap to zoom, pinch to adjust. Ready to save."
                        },
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated AI image showing the transformed result",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Helper text for gestures
            if (isVisible) {
                AnimatedVisibility(
                    visible = currentZoom == 1f,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = MotionTokens.DURATION_MEDIUM_2,
                            delayMillis = 500
                        )
                    )
                ) {
                    Text(
                        text = "💡 Double tap or pinch to zoom",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
