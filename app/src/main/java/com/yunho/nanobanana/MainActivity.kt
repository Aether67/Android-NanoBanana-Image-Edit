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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yunho.nanobanana.di.AppContainer
import com.yunho.nanobanana.presentation.viewmodel.MainViewModel
import com.yunho.nanobanana.ui.screens.MainScreen
import com.yunho.nanobanana.ui.theme.NanobananaTheme

/**
 * Main Activity for NanoBanana AI Image Editor
 * 
 * Migrated to modern MVVM architecture using:
 * - MainViewModel for business logic
 * - MainUiState for reactive state management
 * - Clean Architecture with proper layer separation
 * - Dependency Injection via AppContainer
 * 
 * Features:
 * - AI-powered image generation with Gemini 2.5 Flash Image Preview
 * - AI-powered image enhancement with localized zoom processing
 * - Dual-stream outputs (visual + textual)
 * - Asynchronous processing with Kotlin Coroutines
 * - Material Design 3 UI components
 */
class MainActivity : ComponentActivity() {
    
    private lateinit var appContainer: AppContainer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependency injection container
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

/**
 * Main composable that sets up the ViewModel and connects it to the UI
 */
@Composable
fun MainApp(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    // Create ViewModel with dependency injection
    val viewModel: MainViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return appContainer.createMainViewModel() as T
            }
        }
    )
    
    // Collect UI state as Compose State
    val uiState by viewModel.uiState.collectAsState()
    
    // Render main screen with state and event handlers
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
