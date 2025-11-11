package com.yunho.nanobanana.accessibility

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Accessibility utilities for ensuring WCAG compliance
 * and dynamic typography scaling across screen sizes
 */
object AccessibilityUtils {
    
    /**
     * WCAG contrast ratios for different compliance levels
     */
    object ContrastRatios {
        const val WCAG_AA_NORMAL = 4.5f
        const val WCAG_AA_LARGE = 3.0f
        const val WCAG_AAA_NORMAL = 7.0f
        const val WCAG_AAA_LARGE = 4.5f
    }
    
    /**
     * Calculate the contrast ratio between two colors
     * Returns a value between 1 and 21, where higher is better contrast
     */
    fun calculateContrastRatio(color1: Color, color2: Color): Float {
        val lum1 = color1.luminance()
        val lum2 = color2.luminance()
        
        val lighter = maxOf(lum1, lum2)
        val darker = minOf(lum1, lum2)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Check if two colors meet WCAG AA compliance for normal text
     */
    fun meetsWCAG_AA(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= ContrastRatios.WCAG_AA_NORMAL
    }
    
    /**
     * Check if two colors meet WCAG AA compliance for large text
     */
    fun meetsWCAG_AA_Large(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= ContrastRatios.WCAG_AA_LARGE
    }
    
    /**
     * Check if two colors meet WCAG AAA compliance
     */
    fun meetsWCAG_AAA(foreground: Color, background: Color): Boolean {
        return calculateContrastRatio(foreground, background) >= ContrastRatios.WCAG_AAA_NORMAL
    }
    
    /**
     * Get accessible color alternative if contrast is insufficient
     * Returns either pure black or white depending on which has better contrast
     */
    fun getAccessibleColor(backgroundColor: Color): Color {
        val contrastWithBlack = calculateContrastRatio(Color.Black, backgroundColor)
        val contrastWithWhite = calculateContrastRatio(Color.White, backgroundColor)
        
        return if (contrastWithBlack > contrastWithWhite) Color.Black else Color.White
    }
    
    /**
     * Enhance color contrast to meet WCAG AA standards
     * Adjusts the foreground color to ensure sufficient contrast
     */
    fun enhanceContrast(
        foreground: Color,
        background: Color,
        targetRatio: Float = ContrastRatios.WCAG_AA_NORMAL
    ): Color {
        val currentRatio = calculateContrastRatio(foreground, background)
        if (currentRatio >= targetRatio) return foreground
        
        // Return accessible alternative
        return getAccessibleColor(background)
    }
}

/**
 * Dynamic typography utilities for responsive text sizing
 * Scales text based on screen size and user preferences
 */
object DynamicTypography {
    
    /**
     * Screen size categories
     */
    enum class ScreenSize {
        SMALL,    // < 360dp width
        MEDIUM,   // 360-600dp width
        LARGE,    // 600-840dp width
        XLARGE    // > 840dp width
    }
    
    /**
     * Get screen size category based on current configuration
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        
        return when {
            screenWidth < 360 -> ScreenSize.SMALL
            screenWidth < 600 -> ScreenSize.MEDIUM
            screenWidth < 840 -> ScreenSize.LARGE
            else -> ScreenSize.XLARGE
        }
    }
    
    /**
     * Get scaled text size based on screen size
     * Applies responsive scaling to maintain readability
     */
    @Composable
    fun getScaledTextSize(baseSize: TextUnit): TextUnit {
        val screenSize = getScreenSize()
        val scaleFactor = when (screenSize) {
            ScreenSize.SMALL -> 0.9f
            ScreenSize.MEDIUM -> 1.0f
            ScreenSize.LARGE -> 1.1f
            ScreenSize.XLARGE -> 1.2f
        }
        
        return baseSize * scaleFactor
    }
    
    /**
     * Get scaled spacing based on screen size
     * Ensures consistent visual hierarchy across devices
     */
    @Composable
    fun getScaledSpacing(baseSpacing: Dp): Dp {
        val screenSize = getScreenSize()
        val scaleFactor = when (screenSize) {
            ScreenSize.SMALL -> 0.85f
            ScreenSize.MEDIUM -> 1.0f
            ScreenSize.LARGE -> 1.15f
            ScreenSize.XLARGE -> 1.3f
        }
        
        return baseSpacing * scaleFactor
    }
    
    /**
     * Calculate optimal line height based on font size
     * Follows typography best practices (1.5x font size minimum)
     */
    fun calculateLineHeight(fontSize: TextUnit): TextUnit {
        return fontSize * 1.5f
    }
    
    /**
     * Get touch target size that meets accessibility guidelines
     * Minimum 48dp as per Material Design accessibility standards
     */
    @Composable
    fun getMinimumTouchTarget(): Dp {
        return 48.dp
    }
}

/**
 * Focus navigation helpers for keyboard and screen reader users
 */
object FocusNavigation {
    
    /**
     * Semantic labels for common UI elements
     */
    object SemanticLabels {
        const val BUTTON_GENERATE = "Generate image button. Double tap to start AI generation."
        const val BUTTON_SAVE = "Save image button. Double tap to save the generated image."
        const val BUTTON_RESET = "Reset button. Double tap to clear and start over."
        const val IMAGE_PREVIEW = "Image preview. Double tap to zoom, pinch to adjust zoom level."
        const val TEXT_OUTPUT = "AI generated text output. Swipe to read content."
        const val PROMPT_INPUT = "Prompt input field. Double tap to edit your AI generation prompt."
    }
    
    /**
     * Action labels for accessibility actions
     */
    object ActionLabels {
        const val ZOOM_IN = "Zoom in"
        const val ZOOM_OUT = "Zoom out"
        const val RESET_ZOOM = "Reset zoom to original size"
        const val UNDO = "Undo last action"
        const val REDO = "Redo previous action"
        const val SCROLL_UP = "Scroll up to see more"
        const val SCROLL_DOWN = "Scroll down to see more"
    }
}

/**
 * Composable utilities for accessible UI components
 */

/**
 * Get contrast-compliant text color based on background
 */
@Composable
fun rememberAccessibleTextColor(backgroundColor: Color = MaterialTheme.colorScheme.surface): Color {
    return AccessibilityUtils.getAccessibleColor(backgroundColor)
}

/**
 * Check if current theme colors meet accessibility standards
 */
@Composable
fun checkThemeAccessibility(): Map<String, Boolean> {
    val colorScheme = MaterialTheme.colorScheme
    
    return mapOf(
        "primary_on_primary_container" to AccessibilityUtils.meetsWCAG_AA(
            colorScheme.primary,
            colorScheme.primaryContainer
        ),
        "on_surface_on_surface" to AccessibilityUtils.meetsWCAG_AA(
            colorScheme.onSurface,
            colorScheme.surface
        ),
        "on_background_on_background" to AccessibilityUtils.meetsWCAG_AA(
            colorScheme.onBackground,
            colorScheme.background
        ),
        "secondary_on_secondary_container" to AccessibilityUtils.meetsWCAG_AA(
            colorScheme.secondary,
            colorScheme.secondaryContainer
        )
    )
}
