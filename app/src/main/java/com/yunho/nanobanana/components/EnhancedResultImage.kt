package com.yunho.nanobanana.components

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.animations.MotionTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Enhanced result image component with zoom and AI enhancement support
 * Includes pinch-to-zoom, double-tap zoom, and localized enhancement on zoom
 */
@Composable
fun EnhancedResultImage(
    bitmap: Bitmap,
    isEnhancing: Boolean,
    onRequestEnhancement: (Rect?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    
    // Zoom state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var isZooming by remember { mutableStateOf(false) }
    var lastZoomScale by remember { mutableFloatStateOf(1f) }
    
    // Debounce enhancement requests during continuous zoom
    var zoomStopTime by remember { mutableLongStateOf(0L) }
    
    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(bitmap) {
        isVisible = true
    }
    
    // Monitor zoom stop for enhancement trigger
    LaunchedEffect(scale) {
        if (scale > 1.5f && scale != lastZoomScale) {
            zoomStopTime = System.currentTimeMillis()
            isZooming = true
            
            // Wait for zoom to stop (no changes for 500ms)
            delay(500)
            if (System.currentTimeMillis() - zoomStopTime >= 500 && !isEnhancing) {
                isZooming = false
                lastZoomScale = scale
                
                // Calculate visible region for enhancement
                val visibleRegion = calculateVisibleRegion(
                    imageSize = imageSize,
                    scale = scale,
                    offset = offset,
                    density = density
                )
                
                // Only enhance if zoomed beyond threshold
                if (scale > 2f && visibleRegion != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onRequestEnhancement(visibleRegion)
                }
            }
        }
    }
    
    // Animated scale for entrance
    val entranceScale by animateFloatAsState(
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
                scaleX = entranceScale
                scaleY = entranceScale
            }
            .semantics {
                contentDescription = "Generated AI image with zoom and enhancement support"
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
                        text = "✨ Enhanced Image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Pinch to zoom • Double-tap to reset",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .shadow(4.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { size ->
                            imageSize = size
                        }
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "AI generated result image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    // Update scale
                                    val newScale = (scale * zoom).coerceIn(1f, 3f)
                                    
                                    if (newScale != scale) {
                                        scale = newScale
                                        
                                        // Haptic feedback when hitting zoom limits
                                        if (newScale == 1f || newScale == 3f) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                    
                                    // Update offset with bounds checking
                                    if (scale > 1f) {
                                        val maxOffset = (imageSize.width * (scale - 1)) / 2
                                        offset = Offset(
                                            x = (offset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                            y = (offset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                        )
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        // Reset zoom on double-tap
                                        coroutineScope.launch {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            scale = 1f
                                            offset = Offset.Zero
                                        }
                                    }
                                )
                            }
                    )
                    
                    // Enhancement indicator
                    if (isEnhancing || isZooming) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (isEnhancing) "Enhancing..." else "Preparing...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Zoom level indicator
            if (scale > 1.1f) {
                Text(
                    text = "${(scale * 100).toInt()}% zoom",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Calculate the visible region of the image for localized enhancement
 */
private fun calculateVisibleRegion(
    imageSize: IntSize,
    scale: Float,
    offset: Offset,
    density: androidx.compose.ui.unit.Density
): Rect? {
    if (imageSize.width == 0 || imageSize.height == 0) return null
    
    val visibleWidth = imageSize.width / scale
    val visibleHeight = imageSize.height / scale
    
    val centerX = imageSize.width / 2 - offset.x / scale
    val centerY = imageSize.height / 2 - offset.y / scale
    
    val left = (centerX - visibleWidth / 2).toInt().coerceIn(0, imageSize.width)
    val top = (centerY - visibleHeight / 2).toInt().coerceIn(0, imageSize.height)
    val right = (centerX + visibleWidth / 2).toInt().coerceIn(0, imageSize.width)
    val bottom = (centerY + visibleHeight / 2).toInt().coerceIn(0, imageSize.height)
    
    return Rect(left, top, right, bottom)
}
