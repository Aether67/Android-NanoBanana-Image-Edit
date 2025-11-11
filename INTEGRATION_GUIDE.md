# MainActivity AI Integration Guide

This guide provides step-by-step instructions for integrating the new AI capabilities into the existing MainActivity.kt file.

## Overview

The AI capabilities enhancement adds three main features to the app:
1. **AI Parameter Controls** - User-configurable settings for creativity, detail level, reasoning depth, and output style
2. **AI Reasoning Feedback** - Real-time display of AI's reasoning process during generation
3. **Multi-modal Output** - Support for generating both images and text analysis

## Integration Steps

### Step 1: Add Imports

Add the following imports to the top of `MainActivity.kt`:

```kotlin
import com.yunho.nanobanana.ai.PromptManager
import com.yunho.nanobanana.components.AIParameterControls
import com.yunho.nanobanana.components.AIReasoningFeedback
import com.yunho.nanobanana.components.AIReasoning
import com.yunho.nanobanana.components.ElegantTextOutput
```

### Step 2: Update NanoBanana Composable

In the `NanoBanana` composable function, modify the initialization:

**Before:**
```kotlin
@Composable
fun NanoBanana(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nanoBananaService = rememberNanoBananaService(context)
    val nanoBanana = rememberNanoBanana(nanoBananaService)
    val content by nanoBanana.content
    ...
}
```

**After:**
```kotlin
@Composable
fun NanoBanana(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nanoBananaService = rememberNanoBananaService(context)
    val promptManager = remember { PromptManager(context) }  // ADD THIS
    val nanoBanana = rememberNanoBanana(nanoBananaService, promptManager)  // UPDATE THIS
    val content by nanoBanana.content
    ...
}
```

### Step 3: Update PickerContent Function Signature

**Before:**
```kotlin
@Composable
private fun PickerContent(
    state: NanoBanana.Content.Picker,
    nanoBananaService: NanoBananaService,
    onGenerate: () -> Unit
) {
    ...
}
```

**After:**
```kotlin
@Composable
private fun PickerContent(
    state: NanoBanana.Content.Picker,
    nanoBananaService: NanoBananaService,
    promptManager: PromptManager,  // ADD THIS
    onGenerate: () -> Unit
) {
    ...
}
```

### Step 4: Add AI Controls to PickerContent

In the `PickerContent` function, after the `ApiKeySetting` component, add:

```kotlin
ApiKeySetting(
    service = nanoBananaService,
    modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
)

// ADD THIS:
AIParameterControls(
    promptManager = promptManager,
    modifier = Modifier.fillMaxWidth()
)

StylePicker(
    content = state,
    ...
)
```

### Step 5: Update PickerContent Call in AnimatedContent

In the `when (state)` block inside `AnimatedContent`, update the `PickerContent` call:

**Before:**
```kotlin
is NanoBanana.Content.Picker -> {
    PickerContent(
        state = state,
        nanoBananaService = nanoBananaService,
        onGenerate = { scope.launch { state.request() } }
    )
}
```

**After:**
```kotlin
is NanoBanana.Content.Picker -> {
    PickerContent(
        state = state,
        nanoBananaService = nanoBananaService,
        promptManager = promptManager,  // ADD THIS
        onGenerate = { scope.launch { state.request() } }
    )
}
```

### Step 6: Update LoadingContent Function

**Before:**
```kotlin
@Composable
private fun LoadingContent() {
    Column(
        ...
    ) {
        // Progress indicator code
        ...
    }
}
```

**After:**
```kotlin
@Composable
private fun LoadingContent(reasoning: AIReasoning = AIReasoning()) {  // ADD PARAMETER
    Column(
        ...
    ) {
        // Progress indicator code
        ...
        
        // ADD THIS at the end:
        Spacer(modifier = Modifier.height(24.dp))
        AIReasoningFeedback(
            reasoning = reasoning,
            modifier = Modifier.fillMaxWidth(),
            isActive = true
        )
    }
}
```

### Step 7: Update LoadingContent Call

In the `when (state)` block:

**Before:**
```kotlin
NanoBanana.Content.Loading -> LoadingContent()
```

**After:**
```kotlin
is NanoBanana.Content.Loading -> LoadingContent(state.reasoning)  // UPDATE THIS
```

Note: Change `NanoBanana.Content.Loading` to `is NanoBanana.Content.Loading` because it's now a data class.

### Step 8: Update ResultContent for Text Output

In the `ResultContent` function, after displaying the result image, add:

```kotlin
ResultImage(
    bitmap = state.result,
    ...
)

// ADD THIS to display text analysis if available:
state.resultText?.let { text ->
    Spacer(modifier = Modifier.height(16.dp))
    ElegantTextOutput(
        text = text,
        title = "AI Analysis",
        modifier = Modifier.fillMaxWidth()
    )
}
```

### Step 9: Update Result Save Handler

In the `ResultContent` call's `onSave` callback:

**Before:**
```kotlin
onSave = {
    val result = context.saveBitmapToGallery(state.result)
    Toast.makeText(
        context,
        if (result) "✓ Image saved to gallery!" else "✗ Failed to save image",
        Toast.LENGTH_SHORT
    ).show()
}
```

**After:**
```kotlin
onSave = {
    // Save image if available
    state.resultImage?.let { bitmap ->  // UPDATE THIS
        val result = context.saveBitmapToGallery(bitmap)
        Toast.makeText(
            context,
            if (result) "✓ Image saved to gallery!" else "✗ Failed to save image",
            Toast.LENGTH_SHORT
        ).show()
    }
}
```

Note: `state.result` is changed to `state.resultImage` to support multi-modal output.

## Verification

After making these changes, verify:

1. ✓ App compiles without errors
2. ✓ AI Parameter Controls appear after API Key settings
3. ✓ AI Reasoning Feedback displays during generation
4. ✓ Text output displays when available (in combined mode)
5. ✓ Existing image generation still works
6. ✓ Save functionality works for generated images

## Testing the New Features

1. **Test AI Parameter Controls:**
   - Open the app
   - Expand "AI Parameters" section
   - Change output mode to "Combined"
   - Adjust creativity, detail level, and reasoning depth
   - Verify settings are saved

2. **Test AI Reasoning Feedback:**
   - Select an image
   - Tap "Generate"
   - Verify reasoning feedback appears with steps and confidence

3. **Test Multi-modal Output:**
   - Set output mode to "Combined"
   - Generate an image
   - Verify both image and text analysis appear

4. **Test Text-Only Mode:**
   - Set output mode to "Text"
   - Generate without selecting images (or with images for analysis)
   - Verify text output appears with proper formatting

## Troubleshooting

### Import Errors
If you get import errors, make sure all new files are in the project:
- `app/src/main/java/com/yunho/nanobanana/ai/PromptManager.kt`
- `app/src/main/java/com/yunho/nanobanana/ai/EnhancedAIService.kt`
- `app/src/main/java/com/yunho/nanobanana/components/AIParameterControls.kt`
- `app/src/main/java/com/yunho/nanobanana/components/AIReasoningFeedback.kt`

### Compilation Errors
Common issues:
- **Missing promptManager parameter**: Make sure to add it to all relevant function signatures
- **Loading is data object vs data class**: Change `NanoBanana.Content.Loading` to `is NanoBanana.Content.Loading`
- **result vs resultImage**: Update `state.result` to `state.resultImage` where appropriate

### Runtime Issues
- **Settings not persisting**: Check that `promptManager.saveSettings()` is called after parameter changes
- **No reasoning showing**: Verify `isActive = true` in AIReasoningFeedback
- **Text not displaying**: Ensure output mode is set to TEXT_ONLY or COMBINED

## Alternative: Minimal Integration

If you prefer a minimal integration without modifying existing code extensively, you can use the integration helpers:

```kotlin
import com.yunho.nanobanana.integration.*

// In your composables:
AIControlsSection(promptManager, modifier)
AIReasoningDisplay(reasoning, modifier)
AITextResultDisplay(text, modifier)
```

This approach allows you to add components incrementally.

## Next Steps

After successful integration:
1. Run the test suite to ensure all tests pass
2. Test the app on physical devices
3. Request code review
4. Run CodeQL security scan
5. Update README.md with new features

## Support

For issues or questions, refer to:
- `AI_PROMPT_ENGINEERING.md` - Detailed documentation on AI features
- `README.md` - General app documentation
- GitHub Issues - Report bugs or request features
