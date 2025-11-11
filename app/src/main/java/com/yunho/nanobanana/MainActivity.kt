package com.yunho.nanobanana

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.NanoBanana.Companion.rememberNanoBanana
import com.yunho.nanobanana.NanoBananaService.Companion.rememberNanoBananaService
import com.yunho.nanobanana.animations.MotionTokens
import com.yunho.nanobanana.components.ApiKeySetting
import com.yunho.nanobanana.components.Generate
import com.yunho.nanobanana.components.PickedImages
import com.yunho.nanobanana.components.PickerTitle
import com.yunho.nanobanana.components.Prompt
import com.yunho.nanobanana.components.Reset
import com.yunho.nanobanana.components.ResultImage
import com.yunho.nanobanana.components.Save
import com.yunho.nanobanana.components.SelectImages
import com.yunho.nanobanana.components.StylePicker
import com.yunho.nanobanana.components.RippleProcessingIndicator
import com.yunho.nanobanana.components.TextGenerationShimmer
import com.yunho.nanobanana.extension.saveBitmapToGallery
import com.yunho.nanobanana.performance.PerformanceMetrics
import com.yunho.nanobanana.ui.theme.NanobananaTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NanobananaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NanoBanana(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Log performance metrics on app close
        PerformanceMetrics.getInstance().logSummary()
        PerformanceMetrics.getInstance().stopTelemetry()
    }
}

/**
 * Main composable for the NanoBanana AI Image Editor
 * Enhanced with Material 3 animations, loading states, and accessibility
 */
@Composable
fun NanoBanana(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nanoBananaService = rememberNanoBananaService(context)
    val nanoBanana = rememberNanoBanana(nanoBananaService)
    val content by nanoBanana.content

    LaunchedEffect(Unit) {
        nanoBanana.launch()
        // Start telemetry collection
        PerformanceMetrics.getInstance().startTelemetry(scope)
    }

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
        // Animated content transitions
        AnimatedContent(
            targetState = content,
            transitionSpec = {
                fadeIn(
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
                ) togetherWith fadeOut(
                    animationSpec = tween(
                        durationMillis = MotionTokens.DURATION_SHORT_4,
                        easing = MotionTokens.EasingEmphasizedAccelerate
                    )
                ) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DURATION_SHORT_4,
                        easing = MotionTokens.EasingEmphasizedAccelerate
                    )
                )
            },
            label = "content_transition"
        ) { state ->
            when (state) {
                is NanoBanana.Content.Picker -> {
                    PickerContent(
                        state = state,
                        nanoBananaService = nanoBananaService,
                        onGenerate = { scope.launch { state.request() } }
                    )
                }

                NanoBanana.Content.Loading -> LoadingContent()
                
                is NanoBanana.Content.Result -> {
                    ResultContent(
                        state = state,
                        onSave = {
                            val result = context.saveBitmapToGallery(state.result)
                            Toast.makeText(
                                context,
                                if (result) "✓ Image saved to gallery!" else "✗ Failed to save image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                is NanoBanana.Content.Error -> {
                    ErrorContent(state = state)
                }
            }
        }
    }
}

/**
 * Picker screen content with all input controls
 */
@Composable
private fun PickerContent(
    state: NanoBanana.Content.Picker,
    nanoBananaService: NanoBananaService,
    onGenerate: () -> Unit
) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        state.select(context = context, uris = uris)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        PickerTitle(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        ApiKeySetting(
            service = nanoBananaService,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        StylePicker(
            content = state,
            modifier = Modifier
                .height(400.dp)
                .fillMaxWidth()
        )

        Prompt(
            prompt = state.prompt,
            modifier = Modifier.fillMaxWidth()
        )

        SelectImages(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp),
            onClick = { galleryLauncher.launch("image/*") }
        )

        AnimatedVisibility(
            visible = state.selectedBitmaps.isNotEmpty(),
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_MEDIUM_2,
                    easing = MotionTokens.EasingEmphasizedDecelerate
                )
            ) + expandVertically(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_MEDIUM_3,
                    easing = MotionTokens.EasingEmphasizedDecelerate
                )
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_SHORT_3,
                    easing = MotionTokens.EasingEmphasizedAccelerate
                )
            ) + shrinkVertically(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_SHORT_3,
                    easing = MotionTokens.EasingEmphasizedAccelerate
                )
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PickedImages(
                    selectedBitmaps = state.selectedBitmaps,
                    modifier = Modifier.fillMaxWidth()
                )

                Generate(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    onClick = onGenerate
                )
            }
        }
    }
}

/**
 * Enhanced loading screen with blur and shimmer effects
 */
@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics {
                contentDescription = "Generating AI image, please wait"
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated pulsing glow effect
        val infiniteTransition = rememberInfiniteTransition(label = "loading_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_alpha"
        )

        Box(
            contentAlignment = Alignment.Center
        ) {
            // Glow background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .blur(24.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                        CircleShape
                    )
            )
            
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        AnimatedVisibility(
            visible = true,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_MEDIUM_2,
                    delayMillis = 200
                )
            )
        ) {
            Column(
                modifier = Modifier.padding(top = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✨ Generating AI Image",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Using Gemini 2.0 Flash",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Result screen with save and reset options
 */
@Composable
private fun ResultContent(
    state: NanoBanana.Content.Result,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ResultImage(
            bitmap = state.result,
            modifier = Modifier.fillMaxWidth()
        )

        Save(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp),
            onClick = onSave
        )

        Reset(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            onClick = state::reset
        )
    }
}

/**
 * Error screen with retry option
 */
@Composable
private fun ErrorContent(state: NanoBanana.Content.Error) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics {
                contentDescription = "Error occurred: ${state.message}"
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp
        )
        Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Reset(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(56.dp),
            onClick = state::reset
        )
    }
}

