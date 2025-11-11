package com.yunho.nanobanana.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Material Design 3 motion specifications
 * Provides consistent animation timings across the app
 */
object MotionTokens {
    // Duration tokens
    const val DURATION_SHORT_1 = 50
    const val DURATION_SHORT_2 = 100
    const val DURATION_SHORT_3 = 150
    const val DURATION_SHORT_4 = 200
    const val DURATION_MEDIUM_1 = 250
    const val DURATION_MEDIUM_2 = 300
    const val DURATION_MEDIUM_3 = 350
    const val DURATION_MEDIUM_4 = 400
    const val DURATION_LONG_1 = 450
    const val DURATION_LONG_2 = 500
    const val DURATION_LONG_3 = 550
    const val DURATION_LONG_4 = 600
    const val DURATION_EXTRA_LONG_1 = 700
    const val DURATION_EXTRA_LONG_2 = 800
    const val DURATION_EXTRA_LONG_3 = 900
    const val DURATION_EXTRA_LONG_4 = 1000

    // Easing tokens
    val EasingStandard = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f)
    val EasingStandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val EasingStandardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EasingEmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EasingLegacy = CubicBezierEasing(0.4f, 0.0f, 0.6f, 1.0f)
    val EasingLegacyDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.2f, 1.0f)
    val EasingLegacyAccelerate = CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f)
}

/**
 * Spring animation specifications for natural motion
 */
object SpringSpecs {
    val bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val quick = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessHigh
    )
}

/**
 * Fade in/out with scale animation for modern appearance
 */
@Composable
fun FadeScaleEnterTransition(
    durationMillis: Int = MotionTokens.DURATION_MEDIUM_2
): EnterTransition {
    return fadeIn(
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = MotionTokens.EasingEmphasizedDecelerate
        )
    ) + scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = MotionTokens.EasingEmphasizedDecelerate
        )
    )
}

@Composable
fun FadeScaleExitTransition(
    durationMillis: Int = MotionTokens.DURATION_SHORT_4
): ExitTransition {
    return fadeOut(
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = MotionTokens.EasingEmphasizedAccelerate
        )
    ) + scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = MotionTokens.EasingEmphasizedAccelerate
        )
    )
}

/**
 * Slide in from bottom with fade for content entry
 */
@Composable
fun SlideUpEnterTransition(
    durationMillis: Int = MotionTokens.DURATION_MEDIUM_3
): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it / 2 },
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = MotionTokens.EasingEmphasizedDecelerate
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = MotionTokens.EasingEmphasizedDecelerate
        )
    )
}

/**
 * Extension function for applying press animation to clickable items
 */
fun Modifier.pressAnimation(
    pressed: Boolean,
    targetScale: Float = 0.95f
): Modifier = this.graphicsLayer {
    val scale = if (pressed) targetScale else 1f
    scaleX = scale
    scaleY = scale
}

/**
 * Modifier for applying a spring bounce effect
 */
fun Modifier.bounceClick(
    enabled: Boolean = true,
    scaleDown: Float = 0.9f
): Modifier = this.graphicsLayer {
    if (enabled) {
        scaleX = if (scaleDown < 1f) scaleDown else 1f
        scaleY = if (scaleDown < 1f) scaleDown else 1f
    }
}
