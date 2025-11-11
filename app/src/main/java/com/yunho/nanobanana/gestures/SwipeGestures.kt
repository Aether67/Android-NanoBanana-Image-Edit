package com.yunho.nanobanana.gestures

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import com.yunho.nanobanana.components.HapticFeedback
import kotlinx.coroutines.launch

/**
 * Swipe gesture utilities for undo/redo actions
 * Provides intuitive horizontal swipe interactions with haptic feedback
 */

/**
 * Direction of swipe gesture
 */
enum class SwipeDirection {
    LEFT,
    RIGHT,
    NONE
}

/**
 * Swipe gesture state
 */
data class SwipeGestureState(
    val offsetX: Float = 0f,
    val direction: SwipeDirection = SwipeDirection.NONE,
    val isActive: Boolean = false
)

/**
 * Modifier for swipe-to-undo/redo gestures
 * 
 * @param onSwipeLeft Callback when user swipes left (undo)
 * @param onSwipeRight Callback when user swipes right (redo)
 * @param swipeThreshold Minimum swipe distance to trigger action (in pixels)
 * @param hapticFeedback Whether to provide haptic feedback
 */
fun Modifier.swipeToUndoRedo(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    swipeThreshold: Float = 200f,
    hapticFeedback: Boolean = true
): Modifier = composed {
    val view = LocalView.current
    var offsetX by remember { mutableStateOf(0f) }
    var hasTriggered by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // Animate back to center
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )
    
    this
        .graphicsLayer {
            translationX = animatedOffsetX
            
            // Add slight rotation for visual feedback
            rotationZ = (animatedOffsetX / 2000f) * 5f
        }
        .pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragStart = {
                    hasTriggered = false
                },
                onDragEnd = {
                    // Check if swipe threshold was met
                    if (!hasTriggered) {
                        if (offsetX < -swipeThreshold && onSwipeLeft != null) {
                            if (hapticFeedback) {
                                HapticFeedback.performMedium(view)
                            }
                            scope.launch {
                                onSwipeLeft()
                            }
                            hasTriggered = true
                        } else if (offsetX > swipeThreshold && onSwipeRight != null) {
                            if (hapticFeedback) {
                                HapticFeedback.performMedium(view)
                            }
                            scope.launch {
                                onSwipeRight()
                            }
                            hasTriggered = true
                        }
                    }
                    
                    // Reset to center
                    offsetX = 0f
                },
                onDragCancel = {
                    offsetX = 0f
                },
                onHorizontalDrag = { _, dragAmount ->
                    offsetX += dragAmount
                    
                    // Provide light haptic feedback when crossing threshold
                    if (!hasTriggered && hapticFeedback) {
                        if ((offsetX < -swipeThreshold && onSwipeLeft != null) ||
                            (offsetX > swipeThreshold && onSwipeRight != null)) {
                            HapticFeedback.performLight(view)
                            hasTriggered = true
                        }
                    }
                    
                    // Limit drag distance for visual effect
                    offsetX = offsetX.coerceIn(-swipeThreshold * 1.5f, swipeThreshold * 1.5f)
                }
            )
        }
}

/**
 * Enhanced swipe gesture with visual indicators
 * Shows arrow indicators when approaching threshold
 * 
 * @param enabled Whether swipe gestures are enabled
 * @param onUndo Callback for undo action (swipe left)
 * @param onRedo Callback for redo action (swipe right)
 * @param undoAvailable Whether undo action is available
 * @param redoAvailable Whether redo action is available
 */
fun Modifier.swipeWithIndicators(
    enabled: Boolean = true,
    onUndo: (() -> Unit)? = null,
    onRedo: (() -> Unit)? = null,
    undoAvailable: Boolean = true,
    redoAvailable: Boolean = true
): Modifier = composed {
    val view = LocalView.current
    var swipeState by remember { mutableStateOf(SwipeGestureState()) }
    val scope = rememberCoroutineScope()
    val swipeThreshold = 150f
    
    val animatedOffsetX by animateFloatAsState(
        targetValue = swipeState.offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "swipe_indicator_offset"
    )
    
    if (!enabled) {
        return@composed this
    }
    
    this
        .graphicsLayer {
            translationX = animatedOffsetX * 0.5f // Dampened movement
            alpha = 1f - (kotlin.math.abs(animatedOffsetX) / (swipeThreshold * 2))
                .coerceIn(0f, 0.3f)
        }
        .pointerInput(enabled, undoAvailable, redoAvailable) {
            detectHorizontalDragGestures(
                onDragStart = {
                    swipeState = swipeState.copy(isActive = true, offsetX = 0f)
                },
                onDragEnd = {
                    // Execute action if threshold met
                    when {
                        swipeState.offsetX < -swipeThreshold && undoAvailable && onUndo != null -> {
                            HapticFeedback.performStrong(view)
                            scope.launch { onUndo() }
                        }
                        swipeState.offsetX > swipeThreshold && redoAvailable && onRedo != null -> {
                            HapticFeedback.performStrong(view)
                            scope.launch { onRedo() }
                        }
                    }
                    
                    // Reset state
                    swipeState = SwipeGestureState()
                },
                onDragCancel = {
                    swipeState = SwipeGestureState()
                },
                onHorizontalDrag = { _, dragAmount ->
                    val newOffset = (swipeState.offsetX + dragAmount)
                        .coerceIn(-swipeThreshold * 2, swipeThreshold * 2)
                    
                    val newDirection = when {
                        newOffset < -20f -> SwipeDirection.LEFT
                        newOffset > 20f -> SwipeDirection.RIGHT
                        else -> SwipeDirection.NONE
                    }
                    
                    // Haptic feedback at threshold
                    if (kotlin.math.abs(newOffset) >= swipeThreshold && 
                        kotlin.math.abs(swipeState.offsetX) < swipeThreshold) {
                        HapticFeedback.performLight(view)
                    }
                    
                    swipeState = swipeState.copy(
                        offsetX = newOffset,
                        direction = newDirection
                    )
                }
            )
        }
}

/**
 * Remember swipe gesture state
 * Useful for coordinating swipe gestures with visual feedback
 */
@Composable
fun rememberSwipeGestureState(): MutableState<SwipeGestureState> {
    return remember { mutableStateOf(SwipeGestureState()) }
}
