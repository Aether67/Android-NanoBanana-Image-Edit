package com.yunho.nanobanana.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView

/**
 * Haptic feedback utilities for enhanced user interaction
 * Provides tactile responses for various UI interactions
 */
object HapticFeedback {
    /**
     * Trigger light haptic feedback for subtle interactions
     */
    fun performLight(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
    
    /**
     * Trigger medium haptic feedback for standard interactions
     */
    fun performMedium(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * Trigger strong haptic feedback for important interactions
     */
    fun performStrong(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
    
    /**
     * Trigger success haptic feedback
     */
    fun performSuccess(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
    
    /**
     * Trigger error haptic feedback
     */
    fun performError(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    }
}

/**
 * Modifier for adding haptic feedback on click/tap
 */
fun Modifier.hapticClick(
    intensity: HapticIntensity = HapticIntensity.MEDIUM,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    
    this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                when (intensity) {
                    HapticIntensity.LIGHT -> HapticFeedback.performLight(view)
                    HapticIntensity.MEDIUM -> HapticFeedback.performMedium(view)
                    HapticIntensity.STRONG -> HapticFeedback.performStrong(view)
                }
                onClick()
            }
        )
    }
}

/**
 * Enhanced modifier for interactive elements with shadow elevation animation
 */
fun Modifier.interactiveElevation(
    defaultElevation: Float = 4f,
    pressedElevation: Float = 8f
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) pressedElevation else defaultElevation,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation"
    )
    
    this
        .graphicsLayer {
            shadowElevation = elevation
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Modifier for pinch-to-zoom and pan gestures on images
 */
fun Modifier.zoomableImage(
    minScale: Float = 1f,
    maxScale: Float = 3f,
    onZoomChange: ((Float) -> Unit)? = null
): Modifier = composed {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val view = LocalView.current
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            translationX = offsetX
            translationY = offsetY
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                
                if (newScale != scale) {
                    // Haptic feedback on zoom
                    HapticFeedback.performLight(view)
                    scale = newScale
                    onZoomChange?.invoke(newScale)
                }
                
                // Allow panning only when zoomed
                if (scale > 1f) {
                    offsetX += pan.x
                    offsetY += pan.y
                }
            }
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onDoubleTap = {
                    // Double tap to reset zoom
                    HapticFeedback.performMedium(view)
                    scale = if (scale > 1f) 1f else 2f
                    if (scale == 1f) {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            )
        }
}

/**
 * Modifier for responsive ripple effect with haptic feedback
 */
fun Modifier.responsiveRipple(
    intensity: HapticIntensity = HapticIntensity.LIGHT,
    onClick: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    var rippleScale by remember { mutableStateOf(1f) }
    
    val scale by animateFloatAsState(
        targetValue = rippleScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "ripple_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    rippleScale = 0.95f
                    when (intensity) {
                        HapticIntensity.LIGHT -> HapticFeedback.performLight(view)
                        HapticIntensity.MEDIUM -> HapticFeedback.performMedium(view)
                        HapticIntensity.STRONG -> HapticFeedback.performStrong(view)
                    }
                    tryAwaitRelease()
                    rippleScale = 1f
                },
                onTap = { onClick() }
            )
        }
}

/**
 * Haptic feedback intensity levels
 */
enum class HapticIntensity {
    LIGHT,
    MEDIUM,
    STRONG
}

/**
 * Modifier for long-press with haptic feedback and visual response
 */
fun Modifier.longPressHaptic(
    onLongPress: () -> Unit
): Modifier = composed {
    val view = LocalView.current
    var isLongPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isLongPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "long_press_scale"
    )
    
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = {
                    isLongPressed = true
                    HapticFeedback.performStrong(view)
                    onLongPress()
                    kotlinx.coroutines.delay(100)
                    isLongPressed = false
                }
            )
        }
}
