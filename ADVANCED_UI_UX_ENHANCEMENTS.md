# Advanced UI/UX Enhancements Documentation

## Overview
This document describes the comprehensive UI/UX enhancements implemented for professional, seamless image and text generation experiences in the NanoBanana AI Image Editor.

---

## Table of Contents
1. [Enhanced Skeleton Loaders](#enhanced-skeleton-loaders)
2. [Accessibility & WCAG Compliance](#accessibility--wcag-compliance)
3. [Dynamic Typography System](#dynamic-typography-system)
4. [Swipe Gestures for Undo/Redo](#swipe-gestures-for-undoredo)
5. [Synchronized Transitions](#synchronized-transitions)
6. [Focus Navigation](#focus-navigation)
7. [Testing](#testing)
8. [Usage Examples](#usage-examples)

---

## Enhanced Skeleton Loaders

### PromptInputSkeleton
A shimmer-based skeleton loader for prompt input areas during initialization.

**Location**: `components/LoadingEffects.kt`

```kotlin
@Composable
fun PromptInputSkeleton(
    modifier: Modifier = Modifier
)
```

**Features**:
- Shimmer effect animation
- Card-based layout matching actual prompt input
- Automatic sizing and spacing

**Usage**:
```kotlin
if (isInitializing) {
    PromptInputSkeleton()
} else {
    ActualPromptInput()
}
```

---

### AIOutputPanelSkeleton
Comprehensive skeleton for AI output panels showing both image and text placeholders.

**Location**: `components/LoadingEffects.kt`

```kotlin
@Composable
fun AIOutputPanelSkeleton(
    modifier: Modifier = Modifier,
    showImagePlaceholder: Boolean = true,
    showTextPlaceholder: Boolean = true
)
```

**Features**:
- Conditional image/text placeholders
- Synchronized shimmer animations
- Proper aspect ratios and spacing
- Matches actual output panel layout

**Usage**:
```kotlin
// Show both image and text loading
AIOutputPanelSkeleton(
    showImagePlaceholder = true,
    showTextPlaceholder = true
)

// Image only
AIOutputPanelSkeleton(
    showImagePlaceholder = true,
    showTextPlaceholder = false
)
```

---

### SynchronizedTransition
Smooth cross-fade transitions between loading and content states.

**Location**: `components/LoadingEffects.kt`

```kotlin
@Composable
fun SynchronizedTransition(
    isLoading: Boolean,
    loadingContent: @Composable () -> Unit,
    actualContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
)
```

**Features**:
- 400ms cross-fade animation
- Material Design easing curves
- Zero layout shift between states
- Maintains visual continuity

**Usage**:
```kotlin
SynchronizedTransition(
    isLoading = viewModel.isGenerating,
    loadingContent = { RippleProcessingIndicator() },
    actualContent = { GeneratedImageView(image) }
)
```

---

## Accessibility & WCAG Compliance

### AccessibilityUtils
Comprehensive utilities for ensuring WCAG AA/AAA compliance.

**Location**: `accessibility/AccessibilityUtils.kt`

### Contrast Ratio Calculation

```kotlin
fun calculateContrastRatio(color1: Color, color2: Color): Float
```

Calculates the contrast ratio between two colors (1-21 scale).

**Example**:
```kotlin
val ratio = AccessibilityUtils.calculateContrastRatio(
    Color.Black,
    Color.White
)
// Returns ~21.0 (maximum contrast)
```

---

### WCAG Compliance Checking

```kotlin
fun meetsWCAG_AA(foreground: Color, background: Color): Boolean
fun meetsWCAG_AA_Large(foreground: Color, background: Color): Boolean
fun meetsWCAG_AAA(foreground: Color, background: Color): Boolean
```

**Compliance Levels**:
- **AA Normal Text**: 4.5:1 contrast ratio
- **AA Large Text**: 3.0:1 contrast ratio
- **AAA Normal Text**: 7.0:1 contrast ratio
- **AAA Large Text**: 4.5:1 contrast ratio

**Example**:
```kotlin
val backgroundColor = MaterialTheme.colorScheme.surface
val textColor = MaterialTheme.colorScheme.onSurface

if (!AccessibilityUtils.meetsWCAG_AA(textColor, backgroundColor)) {
    // Use enhanced contrast
    val accessibleColor = AccessibilityUtils.enhanceContrast(
        textColor,
        backgroundColor
    )
}
```

---

### Enhanced Contrast

```kotlin
fun enhanceContrast(
    foreground: Color,
    background: Color,
    targetRatio: Float = ContrastRatios.WCAG_AA_NORMAL
): Color
```

Automatically adjusts colors to meet WCAG standards.

**Example**:
```kotlin
val accessibleTextColor = AccessibilityUtils.enhanceContrast(
    foreground = Color.Gray,
    background = Color.LightGray,
    targetRatio = 4.5f
)
// Returns either pure black or white for sufficient contrast
```

---

### Get Accessible Color

```kotlin
fun getAccessibleColor(backgroundColor: Color): Color
```

Returns either black or white depending on which has better contrast.

**Example**:
```kotlin
val textColor = AccessibilityUtils.getAccessibleColor(
    MaterialTheme.colorScheme.primary
)
// Automatically selects black or white
```

---

## Dynamic Typography System

### Screen Size Detection

**Location**: `accessibility/AccessibilityUtils.kt`

```kotlin
enum class ScreenSize {
    SMALL,    // < 360dp width
    MEDIUM,   // 360-600dp width
    LARGE,    // 600-840dp width
    XLARGE    // > 840dp width
}

@Composable
fun getScreenSize(): ScreenSize
```

**Usage**:
```kotlin
val screenSize = DynamicTypography.getScreenSize()
when (screenSize) {
    ScreenSize.SMALL -> // Compact layout
    ScreenSize.MEDIUM -> // Standard layout
    ScreenSize.LARGE -> // Expanded layout
    ScreenSize.XLARGE -> // Wide layout
}
```

---

### Scaled Text Size

```kotlin
@Composable
fun getScaledTextSize(baseSize: TextUnit): TextUnit
```

Automatically scales text based on screen size.

**Scale Factors**:
- Small: 0.9x
- Medium: 1.0x (baseline)
- Large: 1.1x
- XLarge: 1.2x

**Example**:
```kotlin
Text(
    text = "Responsive text",
    fontSize = DynamicTypography.getScaledTextSize(16.sp)
)
```

---

### Scaled Spacing

```kotlin
@Composable
fun getScaledSpacing(baseSpacing: Dp): Dp
```

Scales spacing to maintain visual hierarchy across devices.

**Example**:
```kotlin
Column(
    modifier = Modifier.padding(
        DynamicTypography.getScaledSpacing(20.dp)
    )
) {
    // Content
}
```

---

### Line Height Calculation

```kotlin
fun calculateLineHeight(fontSize: TextUnit): TextUnit
```

Calculates optimal line height (1.5x font size) for readability.

**Example**:
```kotlin
val fontSize = 16.sp
val lineHeight = DynamicTypography.calculateLineHeight(fontSize)
// Returns 24.sp

Text(
    text = "Readable text",
    fontSize = fontSize,
    lineHeight = lineHeight
)
```

---

### Minimum Touch Target

```kotlin
@Composable
fun getMinimumTouchTarget(): Dp
```

Returns 48.dp as per Material Design accessibility guidelines.

**Example**:
```kotlin
Button(
    modifier = Modifier.size(
        DynamicTypography.getMinimumTouchTarget()
    ),
    onClick = { }
) {
    Text("Tap")
}
```

---

## Swipe Gestures for Undo/Redo

### Basic Swipe Gesture

**Location**: `gestures/SwipeGestures.kt`

```kotlin
fun Modifier.swipeToUndoRedo(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    swipeThreshold: Float = 200f,
    hapticFeedback: Boolean = true
): Modifier
```

**Features**:
- Left swipe triggers undo
- Right swipe triggers redo
- Configurable threshold distance
- Haptic feedback at threshold crossing
- Spring-based return animation
- Subtle rotation effect during swipe

**Usage**:
```kotlin
Box(
    modifier = Modifier.swipeToUndoRedo(
        onSwipeLeft = { viewModel.undo() },
        onSwipeRight = { viewModel.redo() },
        swipeThreshold = 150f
    )
) {
    // Your content
}
```

---

### Enhanced Swipe with Indicators

```kotlin
fun Modifier.swipeWithIndicators(
    enabled: Boolean = true,
    onUndo: (() -> Unit)? = null,
    onRedo: (() -> Unit)? = null,
    undoAvailable: Boolean = true,
    redoAvailable: Boolean = true
): Modifier
```

**Features**:
- Visual feedback during swipe
- Dampened movement (0.5x translation)
- Alpha fade based on swipe distance
- Conditional enabling based on undo/redo availability
- Strong haptic on action execution

**Usage**:
```kotlin
Card(
    modifier = Modifier.swipeWithIndicators(
        enabled = true,
        onUndo = { historyManager.undo() },
        onRedo = { historyManager.redo() },
        undoAvailable = historyManager.canUndo(),
        redoAvailable = historyManager.canRedo()
    )
) {
    ResultImage(bitmap)
}
```

---

### Swipe Gesture State

```kotlin
data class SwipeGestureState(
    val offsetX: Float = 0f,
    val direction: SwipeDirection = SwipeDirection.NONE,
    val isActive: Boolean = false
)

@Composable
fun rememberSwipeGestureState(): MutableState<SwipeGestureState>
```

For coordinating swipe gestures with custom visual feedback.

---

## Synchronized Transitions

### Material Motion Integration

All transitions use Material Design 3 motion tokens:

```kotlin
object MotionTokens {
    const val DURATION_MEDIUM_2 = 300
    const val DURATION_MEDIUM_3 = 350
    const val DURATION_MEDIUM_4 = 400
    
    val EasingEmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
}
```

### Cross-Fade Transitions

400ms cross-fade with `FastOutSlowInEasing` for state changes:

```kotlin
Crossfade(
    targetState = isLoading,
    animationSpec = tween(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )
)
```

### Fade + Scale Entrance

Combined fade and scale for elegant content appearance:

```kotlin
AnimatedVisibility(
    visible = isVisible,
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
)
```

---

## Focus Navigation

### Semantic Labels

**Location**: `accessibility/AccessibilityUtils.kt`

Pre-defined semantic labels for screen readers:

```kotlin
object SemanticLabels {
    const val BUTTON_GENERATE = "Generate image button. Double tap to start AI generation."
    const val BUTTON_SAVE = "Save image button. Double tap to save the generated image."
    const val BUTTON_RESET = "Reset button. Double tap to clear and start over."
    const val IMAGE_PREVIEW = "Image preview. Double tap to zoom, pinch to adjust zoom level."
    const val TEXT_OUTPUT = "AI generated text output. Swipe to read content."
    const val PROMPT_INPUT = "Prompt input field. Double tap to edit your AI generation prompt."
}
```

**Usage**:
```kotlin
Button(
    onClick = { generate() },
    modifier = Modifier.semantics {
        contentDescription = FocusNavigation.SemanticLabels.BUTTON_GENERATE
    }
) {
    Text("Generate")
}
```

---

### Action Labels

Custom action labels for accessibility services:

```kotlin
object ActionLabels {
    const val ZOOM_IN = "Zoom in"
    const val ZOOM_OUT = "Zoom out"
    const val RESET_ZOOM = "Reset zoom to original size"
    const val UNDO = "Undo last action"
    const val REDO = "Redo previous action"
}
```

---

## Testing

### Accessibility Tests

**File**: `app/src/androidTest/java/com/yunho/nanobanana/AccessibilityEnhancementTest.kt`

#### WCAG Compliance Tests
```kotlin
@Test
fun wcag_contrastRatios_calculatedCorrectly()

@Test
fun wcag_aa_compliance_verifiedForNormalText()

@Test
fun accessibleColor_returnsHighContrastOption()
```

#### Component Tests
```kotlin
@Test
fun promptInputSkeleton_rendersCorrectly()

@Test
fun aiOutputPanelSkeleton_showsImageAndTextPlaceholders()

@Test
fun synchronizedTransition_smoothlySwitchesBetweenStates()
```

---

### Transition Animation Tests

```kotlin
@Test
fun synchronizedTransition_animatesCorrectly()

@Test
fun rippleProcessingIndicator_animatesInfinitely()

@Test
fun textGenerationShimmer_animatesDots()
```

---

### Skeleton Loading Tests

```kotlin
@Test
fun promptInputSkeleton_displaysShimmerEffect()

@Test
fun aiOutputPanelSkeleton_showsOnlyImagePlaceholder()

@Test
fun skeletonImageCard_pulsesCorrectly()
```

---

### Swipe Gesture Tests

**File**: `app/src/androidTest/java/com/yunho/nanobanana/SwipeGestureTest.kt`

```kotlin
@Test
fun swipeToUndoRedo_triggersCallbacks()

@Test
fun swipeWithIndicators_rendersCorrectly()

@Test
fun swipeWithIndicators_disabledState()
```

---

## Usage Examples

### Complete Loading State with Skeleton

```kotlin
@Composable
fun GenerationScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    SynchronizedTransition(
        isLoading = uiState.isGenerating,
        loadingContent = {
            AIOutputPanelSkeleton(
                showImagePlaceholder = true,
                showTextPlaceholder = true
            )
        },
        actualContent = {
            Column {
                if (uiState.generatedImage != null) {
                    ResultImage(uiState.generatedImage)
                }
                if (uiState.generatedText.isNotEmpty()) {
                    ElegantTextOutput(
                        text = uiState.generatedText,
                        title = "AI Response"
                    )
                }
            }
        }
    )
}
```

---

### Accessible Text Display

```kotlin
@Composable
fun AccessibleContent() {
    val backgroundColor = MaterialTheme.colorScheme.surface
    val textColor = rememberAccessibleTextColor(backgroundColor)
    
    val scaledFontSize = DynamicTypography.getScaledTextSize(16.sp)
    val lineHeight = DynamicTypography.calculateLineHeight(scaledFontSize)
    
    Text(
        text = "Accessible content",
        fontSize = scaledFontSize,
        lineHeight = lineHeight,
        color = textColor,
        modifier = Modifier.semantics {
            contentDescription = "Important accessible information"
        }
    )
}
```

---

### Swipeable Result Card

```kotlin
@Composable
fun SwipeableResultCard(
    bitmap: Bitmap,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .swipeWithIndicators(
                enabled = true,
                onUndo = onUndo,
                onRedo = onRedo,
                undoAvailable = canUndo,
                redoAvailable = canRedo
            )
    ) {
        ResultImage(bitmap = bitmap)
    }
}
```

---

### Dynamic Layout Adaptation

```kotlin
@Composable
fun ResponsiveLayout() {
    val screenSize = DynamicTypography.getScreenSize()
    val padding = DynamicTypography.getScaledSpacing(20.dp)
    val fontSize = DynamicTypography.getScaledTextSize(16.sp)
    
    when (screenSize) {
        ScreenSize.SMALL, ScreenSize.MEDIUM -> {
            // Single column layout
            Column(modifier = Modifier.padding(padding)) {
                CompactContent(fontSize)
            }
        }
        ScreenSize.LARGE, ScreenSize.XLARGE -> {
            // Two column layout
            Row(modifier = Modifier.padding(padding)) {
                Column(modifier = Modifier.weight(1f)) {
                    ExpandedContent(fontSize)
                }
                Column(modifier = Modifier.weight(1f)) {
                    SideContent(fontSize)
                }
            }
        }
    }
}
```

---

## Best Practices

### Accessibility
1. **Always verify contrast**: Use `AccessibilityUtils.meetsWCAG_AA()` before applying colors
2. **Provide semantic descriptions**: Add `contentDescription` to all interactive elements
3. **Support system font sizes**: Use `DynamicTypography.getScaledTextSize()`
4. **Maintain touch targets**: Ensure minimum 48dp touch targets
5. **Test with TalkBack**: Verify screen reader compatibility

### Animations
1. **Use Material motion tokens**: Ensure consistency with `MotionTokens`
2. **Respect accessibility settings**: Check for reduced motion preferences
3. **Keep transitions smooth**: Use appropriate easing curves
4. **Avoid layout shifts**: Use `SynchronizedTransition` for state changes

### Gestures
1. **Provide haptic feedback**: Use appropriate intensity levels
2. **Show visual indicators**: Give users feedback during gestures
3. **Set reasonable thresholds**: 150-200px swipe distance works well
4. **Handle edge cases**: Disable gestures when actions unavailable

---

## Performance Considerations

### Skeleton Loaders
- Shimmer animations use `rememberInfiniteTransition` (efficiently reused)
- Skeleton components are lightweight and don't impact performance
- No bitmap loading during skeleton display

### Dynamic Typography
- Screen size calculation is cached per composition
- Scale factors are constant and computed once
- No runtime overhead for text scaling

### Swipe Gestures
- Spring animations are hardware-accelerated
- Haptic feedback is debounced at threshold
- Minimal memory allocation during gestures

---

## Migration Guide

### From Legacy Text Display
```kotlin
// Before
Text(
    text = content,
    fontSize = 16.sp,
    lineHeight = 24.sp
)

// After
Text(
    text = content,
    fontSize = DynamicTypography.getScaledTextSize(16.sp),
    lineHeight = DynamicTypography.calculateLineHeight(16.sp)
)
```

### From Manual Loading States
```kotlin
// Before
if (isLoading) {
    CircularProgressIndicator()
} else {
    Content()
}

// After
SynchronizedTransition(
    isLoading = isLoading,
    loadingContent = { AIOutputPanelSkeleton() },
    actualContent = { Content() }
)
```

---

## Troubleshooting

### Contrast Issues
**Problem**: Text not readable in certain themes
**Solution**: Use `AccessibilityUtils.enhanceContrast()` or `getAccessibleColor()`

### Swipe Not Working
**Problem**: Swipe gestures not triggering
**Solution**: 
- Ensure no conflicting `pointerInput` modifiers
- Check that callbacks are not null
- Verify swipe threshold is appropriate for screen size

### Layout Shifts
**Problem**: Content jumps during state transitions
**Solution**: Use `SynchronizedTransition` with matched dimensions

---

## Additional Resources

- [Material Design 3 Accessibility](https://m3.material.io/foundations/accessible-design)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)
- [Compose Gestures](https://developer.android.com/jetpack/compose/gestures)

---

**Last Updated**: 2025-11-11
**Version**: 1.0.0
**Maintainer**: NanoBanana Development Team
