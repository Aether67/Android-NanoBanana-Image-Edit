# Implementation Guide - Remaining Work

## Overview

This document provides detailed, step-by-step instructions for completing the remaining improvements to the NanoBanana AI Image Editor. Each task includes code examples, file locations, and testing strategies.

## Priority 1: MainActivity Migration & Enhancement UI Integration

### Current State
- MainActivity uses deprecated `NanoBanana` and `NanoBananaService` classes
- EnhanceButton and EnhancedResultImage components created but not integrated
- MainViewModel fully functional with enhancement support

### Implementation Steps

#### Step 1: Create New Modern MainActivity

**File**: `app/src/main/java/com/yunho/nanobanana/MainActivity.kt`

Replace the entire file with:

```kotlin
package com.yunho.nanobanana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yunho.nanobanana.di.AppContainer
import com.yunho.nanobanana.presentation.viewmodel.MainViewModel
import com.yunho.nanobanana.ui.screens.MainScreen
import com.yunho.nanobanana.ui.theme.NanobananaTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var appContainer: AppContainer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependency injection
        appContainer = AppContainer(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            NanobananaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainApp(
                        appContainer = appContainer,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val viewModel: MainViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.Factory {
            appContainer.createMainViewModel()
        }
    )
    
    val uiState by viewModel.uiState.collectAsState()
    
    MainScreen(
        uiState = uiState,
        onApiKeySave = viewModel::saveApiKey,
        onImageSelect = viewModel::addSelectedImages,
        onPromptChange = viewModel::updatePrompt,
        onStyleSelect = viewModel::updateStyleIndex,
        onGenerateClick = { viewModel.generateContent(uiState.currentPrompt) },
        onEnhanceClick = { viewModel.enhanceImage() },
        onZoomEnhance = { region -> viewModel.enhanceImage(region) },
        onReset = viewModel::resetToIdle,
        modifier = modifier
    )
}
```

#### Step 2: Create MainScreen Composable

**File**: `app/src/main/java/com/yunho/nanobanana/ui/screens/MainScreen.kt` (NEW FILE)

```kotlin
package com.yunho.nanobanana.ui.screens

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.yunho.nanobanana.components.*
import com.yunho.nanobanana.domain.model.EnhancementResult
import com.yunho.nanobanana.extension.saveBitmapToGallery
import com.yunho.nanobanana.presentation.state.GenerationState
import com.yunho.nanobanana.presentation.state.MainUiState
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    uiState: MainUiState,
    onApiKeySave: (String) -> Unit,
    onImageSelect: (List<Bitmap>) -> Unit,
    onPromptChange: (String) -> Unit,
    onStyleSelect: (Int) -> Unit,
    onGenerateClick: () -> Unit,
    onEnhanceClick: () -> Unit,
    onZoomEnhance: (Rect?) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .semantics {
                contentDescription = "NanoBanana AI Image Editor main screen"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top)
    ) {
        when (val state = uiState.generationState) {
            is GenerationState.Idle -> {
                PickerContent(
                    uiState = uiState,
                    onApiKeySave = onApiKeySave,
                    onImageSelect = onImageSelect,
                    onPromptChange = onPromptChange,
                    onStyleSelect = onStyleSelect,
                    onGenerateClick = onGenerateClick
                )
            }
            
            is GenerationState.Loading -> {
                LoadingContent(state)
            }
            
            is GenerationState.Success -> {
                ResultContent(
                    state = state,
                    enhancementState = uiState.enhancementState,
                    onEnhanceClick = onEnhanceClick,
                    onZoomEnhance = onZoomEnhance,
                    onSave = { bitmap ->
                        scope.launch {
                            context.saveBitmapToGallery(bitmap)
                        }
                    },
                    onReset = onReset
                )
            }
            
            is GenerationState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = onGenerateClick,
                    onReset = onReset
                )
            }
        }
    }
}

@Composable
private fun PickerContent(
    uiState: MainUiState,
    onApiKeySave: (String) -> Unit,
    onImageSelect: (List<Bitmap>) -> Unit,
    onPromptChange: (String) -> Unit,
    onStyleSelect: (Int) -> Unit,
    onGenerateClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PickerTitle()
        ApiKeySetting(
            apiKey = uiState.apiKey,
            onApiKeyChange = onApiKeySave
        )
        SelectImages(onImagesSelected = onImageSelect)
        if (uiState.selectedImages.isNotEmpty()) {
            PickedImages(images = uiState.selectedImages)
        }
        StylePicker(
            selectedIndex = uiState.selectedStyleIndex,
            onStyleSelected = onStyleSelect
        )
        Prompt(
            prompt = uiState.currentPrompt,
            onPromptChange = onPromptChange
        )
        Generate(
            enabled = uiState.apiKey.isNotBlank() && uiState.selectedImages.isNotEmpty(),
            onClick = onGenerateClick
        )
    }
}

@Composable
private fun LoadingContent(state: GenerationState.Loading) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LoadingEffects()
        if (state.message.isNotEmpty()) {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ResultContent(
    state: GenerationState.Success,
    enhancementState: EnhancementResult?,
    onEnhanceClick: () -> Unit,
    onZoomEnhance: (Rect?) -> Unit,
    onSave: (Bitmap) -> Unit,
    onReset: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Show enhanced or original image
        state.image?.let { image ->
            EnhancedResultImage(
                bitmap = image,
                isEnhancing = enhancementState is EnhancementResult.Loading,
                onRequestEnhancement = onZoomEnhance
            )
        }
        
        // Show enhance button if we have an image
        if (state.image != null) {
            EnhanceButton(
                enabled = true,
                enhancementState = enhancementState,
                onEnhanceClick = onEnhanceClick
            )
        }
        
        // Show text output if available
        state.text?.let { text ->
            ElegantTextOutput(
                text = text,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Show reasoning if available
        state.reasoning?.let { reasoning ->
            AIReasoningFeedback(
                reasoning = reasoning,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.image?.let { image ->
                Save(onClick = { onSave(image) })
            }
            Reset(onClick = onReset)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "❌ Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Generate(enabled = true, onClick = onRetry)
            Reset(onClick = onReset)
        }
    }
}
```

#### Step 3: Remove Deprecated Files

After testing the new implementation, remove:
- `app/src/main/java/com/yunho/nanobanana/NanoBanana.kt`
- `app/src/main/java/com/yunho/nanobanana/NanoBananaService.kt`

#### Step 4: Testing Checklist

- [ ] App launches without crashes
- [ ] API key can be entered and saved
- [ ] Images can be selected
- [ ] Generation works with all styles
- [ ] Loading states display correctly
- [ ] Results show properly
- [ ] Enhancement button appears and works
- [ ] Zoom enhancement triggers at >2x
- [ ] Save to gallery works
- [ ] Reset returns to picker state
- [ ] Error states display properly

## Priority 2: Code Quality Tooling

### Add ktlint

**File**: `build.gradle.kts` (root)

Add at the top:

```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
}
```

**File**: `app/build.gradle.kts`

Add to plugins section:

```kotlin
plugins {
    // existing plugins
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    version.set("1.0.1")
    android.set(true)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}
```

Run: `./gradlew ktlintCheck` to check
Run: `./gradlew ktlintFormat` to auto-fix

### Add Detekt

**File**: `gradle/libs.versions.toml`

Add version:
```toml
[versions]
detekt = "1.23.4"

[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```

**File**: `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.detekt)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("config/detekt/detekt.yml"))
    baseline = file("config/detekt/baseline.xml")
}

dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
}
```

Create config file at `config/detekt/detekt.yml`:

```yaml
build:
  maxIssues: 0

complexity:
  active: true
  ComplexMethod:
    threshold: 15
  LongMethod:
    threshold: 60
  LongParameterList:
    functionThreshold: 6

formatting:
  active: true
  MaxLineLength:
    maxLineLength: 120

naming:
  active: true
  FunctionNaming:
    functionPattern: '^([a-z][a-zA-Z0-9]*)|(`.*`)$'

style:
  active: true
  MagicNumber:
    active: false
```

Run: `./gradlew detekt` to check

## Priority 3: Image Variant Management

### Data Model

**File**: `app/src/main/java/com/yunho/nanobanana/domain/model/ImageVariant.kt` (NEW)

```kotlin
package com.yunho.nanobanana.domain.model

import android.graphics.Bitmap

/**
 * Represents a variant of a generated image
 */
data class ImageVariant(
    val id: String,
    val image: Bitmap,
    val prompt: String,
    val timestamp: Long,
    val isEnhanced: Boolean = false,
    val metadata: VariantMetadata = VariantMetadata()
)

/**
 * Metadata for a variant
 */
data class VariantMetadata(
    val generationTimeMs: Long = 0,
    val enhancementTimeMs: Long = 0,
    val style: String = "",
    val parameters: AIParameters = AIParameters()
)

/**
 * Collection of variants for comparison
 */
data class VariantCollection(
    val variants: List<ImageVariant> = emptyList(),
    val selectedVariantId: String? = null
) {
    val selectedVariant: ImageVariant?
        get() = variants.find { it.id == selectedVariantId }
    
    fun selectVariant(id: String): VariantCollection {
        return copy(selectedVariantId = id)
    }
    
    fun addVariant(variant: ImageVariant): VariantCollection {
        return copy(variants = variants + variant)
    }
    
    fun removeVariant(id: String): VariantCollection {
        val newVariants = variants.filter { it.id != id }
        val newSelectedId = if (selectedVariantId == id) {
            newVariants.firstOrNull()?.id
        } else {
            selectedVariantId
        }
        return copy(variants = newVariants, selectedVariantId = newSelectedId)
    }
}
```

### UI Component

**File**: `app/src/main/java/com/yunho/nanobanana/components/VariantComparison.kt` (NEW)

```kotlin
package com.yunho.nanobanana.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.yunho.nanobanana.domain.model.ImageVariant

@Composable
fun VariantComparison(
    variants: List<ImageVariant>,
    selectedVariantId: String?,
    onVariantSelect: (String) -> Unit,
    onVariantDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Variants (${variants.size})",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(variants, key = { it.id }) { variant ->
                VariantCard(
                    variant = variant,
                    isSelected = variant.id == selectedVariantId,
                    onSelect = { onVariantSelect(variant.id) },
                    onDelete = { onVariantDelete(variant.id) }
                )
            }
        }
    }
}

@Composable
private fun VariantCard(
    variant: ImageVariant,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clickable(onClick = onSelect)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 4.dp
        )
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Image(
                    bitmap = variant.image.asImageBitmap(),
                    contentDescription = "Variant ${variant.id}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Text(
                    text = if (variant.isEnhanced) "Enhanced" else "Original",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Selected indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                )
            }
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
```

### Integration into MainUiState

Update `MainUiState.kt`:

```kotlin
data class MainUiState(
    // ... existing fields
    val variants: VariantCollection = VariantCollection()
)
```

Add methods to MainViewModel:

```kotlin
fun saveAsVariant() {
    val currentState = _uiState.value
    val image = (currentState.generationState as? GenerationState.Success)?.image ?: return
    
    val variant = ImageVariant(
        id = UUID.randomUUID().toString(),
        image = image,
        prompt = currentState.currentPrompt,
        timestamp = System.currentTimeMillis(),
        isEnhanced = currentState.enhancementState is EnhancementResult.Success
    )
    
    _uiState.update {
        it.copy(variants = it.variants.addVariant(variant))
    }
}

fun selectVariant(id: String) {
    _uiState.update {
        it.copy(variants = it.variants.selectVariant(id))
    }
}

fun deleteVariant(id: String) {
    _uiState.update {
        it.copy(variants = it.variants.removeVariant(id))
    }
}
```

## Priority 4: UI/UX Consistency Audit

### Checklist

#### Material Design 3 Compliance
- [ ] All colors use MaterialTheme.colorScheme
- [ ] All typography uses MaterialTheme.typography
- [ ] Proper elevation levels (0dp, 1dp, 3dp, 6dp, 8dp, 12dp, 16dp, 24dp)
- [ ] Consistent corner radius (8dp for small, 12dp for medium, 16dp for large)
- [ ] Proper spacing units (4dp, 8dp, 12dp, 16dp, 24dp, 32dp)

#### Accessibility
- [ ] All interactive elements have contentDescription
- [ ] Minimum touch target size of 48dp
- [ ] Contrast ratios meet WCAG AA (4.5:1 for text, 3:1 for UI)
- [ ] Text scales properly with system font size
- [ ] Screen reader navigation is logical
- [ ] Focus indicators visible

#### Performance
- [ ] No unnecessary recompositions
- [ ] Images loaded efficiently
- [ ] Animations use remember and derivedStateOf
- [ ] Large lists use LazyColumn/LazyRow

## Priority 5: Testing Expansion

### Unit Test Example

**File**: `app/src/test/java/com/yunho/nanobanana/presentation/viewmodel/MainViewModelEnhancementTest.kt` (NEW)

```kotlin
package com.yunho.nanobanana.presentation.viewmodel

import android.graphics.Bitmap
import app.cash.turbine.test
import com.yunho.nanobanana.domain.model.EnhancementResult
import com.yunho.nanobanana.domain.model.ImageEnhancementRequest
import com.yunho.nanobanana.domain.usecase.EnhanceImageUseCase
import com.yunho.nanobanana.presentation.state.GenerationState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelEnhancementTest {
    
    private lateinit var viewModel: MainViewModel
    private lateinit var enhanceImageUseCase: EnhanceImageUseCase
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        enhanceImageUseCase = mockk()
        // Initialize viewModel with mocked dependencies
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `enhance image updates state correctly`() = runTest {
        // Given
        val mockBitmap = mockk<Bitmap>()
        val enhancedBitmap = mockk<Bitmap>()
        
        coEvery { enhanceImageUseCase(any()) } returns flowOf(
            EnhancementResult.Success(enhancedBitmap, 1000L)
        )
        
        // Set initial state with generated image
        // ... setup code
        
        // When
        viewModel.enhanceImage()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.enhancementState is EnhancementResult.Success)
            val success = state.generationState as GenerationState.Success
            assertEquals(enhancedBitmap, success.image)
        }
    }
}
```

### Integration Test Template

Create tests for:
- Full generation flow
- Full enhancement flow
- Variant management flow
- Error recovery flows

### UI Test Template

Create Compose UI tests for:
- All major components
- Navigation flows
- Loading states
- Error states

## Priority 6: Bug Hunting Checklist

### Memory Leaks
- [ ] ViewModels cleared properly
- [ ] Bitmaps recycled
- [ ] Flows collected with lifecycle
- [ ] No static references to Context
- [ ] Coroutines cancelled properly

### Race Conditions
- [ ] StateFlow updates use .update {}
- [ ] No direct mutableState modifications from multiple threads
- [ ] Proper dispatcher usage (IO, Main, Default)
- [ ] Synchronized access to shared resources

### Edge Cases
- [ ] Empty prompt handling
- [ ] No images selected
- [ ] Invalid API key
- [ ] Network timeout
- [ ] API quota exceeded
- [ ] Very large images
- [ ] Rapid button taps
- [ ] Orientation changes during processing
- [ ] App backgrounding during generation

## Testing Strategy

### Manual Testing Checklist
1. Fresh install
2. API key entry
3. Image selection (single, multiple)
4. All style variations
5. Custom prompt
6. Generation success path
7. Enhancement flow
8. Zoom enhancement
9. Variant creation
10. Variant comparison
11. Save to gallery
12. Reset functionality
13. Error scenarios
14. Low memory conditions
15. No network
16. Background/foreground transitions

### Automated Testing
- Unit tests: 95%+ coverage target
- Integration tests: All critical flows
- UI tests: All user journeys
- Performance tests: Build time, APK size, memory usage

## Success Criteria

### Completion Checklist
- [ ] No deprecated code in use
- [ ] All features accessible in UI
- [ ] 95%+ test coverage
- [ ] ktlint passes
- [ ] Detekt passes with 0 critical issues
- [ ] All manual tests pass
- [ ] Documentation complete
- [ ] Code review passed
- [ ] Performance targets met

### Performance Targets
- Cold start: <2s
- Generation response: <30s
- Enhancement response: <20s
- Memory usage: <150MB
- APK size: <20MB
- Build time (clean): <120s
- Build time (incremental): <20s

---

**Document Version**: 1.0  
**Last Updated**: November 11, 2024  
**Estimated Effort**: 40-53 hours  
**Owner**: NanoBanana Development Team
