# NanoBanana - AI Image Editor

An advanced Android app that uses Google's Gemini 2.0 Flash AI to transform and edit your images with creative prompts.

<br>
<img src="https://github.com/user-attachments/assets/fa7e4138-fc0c-48aa-b8cf-02e9756f6455" width="250"/>
<img src="https://github.com/user-attachments/assets/97fa5bf1-c46d-4e6a-8da4-1ff7b98d07b3" width="250"/>
<img src="https://github.com/user-attachments/assets/3b38d3fa-ca3e-4bd2-a431-954347e6f22d" width="250"/>

## ✨ Features

### AI-Powered Image Editing
- **Gemini 2.0 Flash Integration**: Transform your photos using Google's latest AI model
- **High-Resolution Output**: Artifact-free, high-quality image generation
- **Intelligent Retry**: Automatic retry mechanism with exponential backoff for reliability
- **Smart Error Handling**: Comprehensive error detection and user-friendly feedback

### Modern UI/UX 💎
- **Material Design 3**: Beautiful, modern interface with dynamic color theming
- **Smooth Animations**: Professional-grade animations throughout the app
  - Spring physics for natural motion
  - Staggered entrance animations
  - Interactive press effects on all buttons
  - Smooth content transitions
- **Advanced Loading States**: Multi-layered, adaptive visual feedback
  - Animated progress indicators with ripple effects
  - Pulsing glow effects synchronized with processing
  - Adaptive blur intensity based on image size and duration
  - Skeleton loaders for content with shimmer effects
- **Haptic Feedback**: Tactile responses for all interactions
  - Light feedback for subtle actions
  - Medium feedback for standard interactions
  - Strong feedback for important actions
- **Gesture Support**: Enhanced touch interactions
  - Pinch-to-zoom on generated images (1x to 3x)
  - Double-tap to reset zoom
  - Pan support when zoomed
  - Interactive elevation animations
- **Elegant Text Display**: Professional text output presentation
  - Smooth scrolling for long responses
  - Text selection support
  - Adaptive formatting for dark mode
  - High-contrast accessibility mode
- **Accessibility First**: Comprehensive accessibility features
  - Full screen reader support with semantic descriptions
  - Adjustable font sizes respecting system settings
  - WCAG AA/AAA compliant contrast ratios with automatic verification
  - Dynamic typography scaling across screen sizes
  - High-contrast display option
  - Focus navigation with semantic labels
- **Advanced Gesture Controls**: Intuitive interaction patterns
  - Swipe-to-undo/redo with haptic feedback
  - Visual indicators for gesture thresholds
  - Configurable sensitivity and availability states

### Creative Transformation Styles
- **Korean Historical**: Transform to Chosun Dynasty 1900s style
- **Collectible Figure**: Create hyper-realistic figure with packaging
- **Rock-Paper-Scissors**: Interactive game scenarios with two people
- **Shopping Scene**: 3D style Costco shopping experience
- **Custom Prompts**: Enter your own creative transformation ideas

### User-Friendly Features
- **Multi-Image Support**: Select and transform multiple images at once
- **Gallery Integration**: Save edited images directly to your device's gallery
- **Responsive Design**: Optimized for all Android screen sizes
- **Edge-to-Edge**: Modern edge-to-edge display support

## 🚀 Technical Highlights

### Advanced Async Architecture 🔥 NEW!
- **Parallel Processing Pipeline**: Simultaneous image and text generation using Kotlin Coroutines
- **Intelligent Caching**: LRU cache with JPEG compression (60-70% memory reduction)
- **Task Prioritization**: 5-level priority system ensuring UI responsiveness
- **Exponential Backoff Retry**: Automatic retry with jitter for transient failures
- **Device Adaptation**: Automatic quality adjustment based on device tier (HIGH/MID/LOW_END)
- **Graceful Degradation**: 4-mode system (NORMAL → REDUCED → MINIMAL → EMERGENCY)
- **Comprehensive Telemetry**: Real-time monitoring of latency, errors, cache performance
- **Memory Safety**: Automatic memory pressure detection and optimization

### Modern Architecture
- **MVVM Pattern**: Clean separation of UI, business logic, and data
- **Jetpack Compose**: Modern declarative UI toolkit
- **Kotlin Coroutines & Flow**: Smooth asynchronous operations with reactive state management
- **StateFlow Integration**: Real-time UI updates with minimal perceived latency
- **Material Motion**: Official Material Design 3 motion specifications

### Performance Optimizations
- **Async Processing**: Non-blocking image generation with parallel pipelines
- **Reactive Updates**: StateFlow for instant UI synchronization
- **Progressive Loading**: Multi-layered adaptive blur effects
- **Optimized Bitmap Handling**: Efficient image compression and encoding
- **Smart Caching**: Thread-safe LRU cache with automatic eviction

### Code Quality
- **Comprehensive Documentation**: KDoc comments throughout
- **Clean Code**: Well-organized, maintainable codebase
- **Error Resilience**: Robust error handling at all layers
- **Extensible Design**: Easy to add new AI features
- **UI Component Tests**: Automated testing for critical user flows

## 📋 Requirements

- Android 9.0 (API level 28) or higher
- Google AI API key (Gemini API)
- Internet connection for AI processing
- 100MB+ free storage for high-resolution images

## 🛠️ Setup

1. Clone this repository
   ```bash
   git clone https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
   ```

2. Open the project in Android Studio

3. Get a Google AI API key:
    - Visit [Google AI Studio](https://ai.google.dev/)
    - Create a new API key
    - Copy the key for use in the app

4. Build and run the app

## 📖 Usage

### 1. API Key Setup
- Open the app and enter your Google AI API key in the settings section
- Tap "Save" to store the key securely (persisted locally)

### 2. Select Images
- Tap "Select Images" to choose photos from your gallery
- You can select multiple images for transformation
- Selected images appear with smooth animations

### 3. Choose Style
- Swipe through the available style presets
- Each style has a preview image and description
- Or scroll down to enter a custom prompt

### 4. Generate
- Tap the animated "Generate Image" button to start the AI transformation
- Watch the elegant loading animation with pulsing glow effect
- The app will automatically retry if the first attempt fails

### 5. Save Result
- Once generated, your image appears with a smooth reveal animation
- Tap "Save" to store the result in your gallery
- Use "Reset" (with rotating icon) to start over with new images

## 🏗️ Architecture

NanoBanana follows **Clean Architecture** principles with **MVVM (Model-View-ViewModel)** pattern, ensuring:
- Clear separation of concerns
- Testability at all layers
- Scalability for future features
- Maintainability through well-defined boundaries

### Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌────────────────────────────────────────────────────┐ │
│  │  UI (Compose) ◄─► ViewModel ◄─► UiState           │ │
│  └────────────────────────────────────────────────────┘ │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                      Domain Layer                        │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Use Cases ◄─► Models ◄─► Repository Interfaces   │ │
│  └────────────────────────────────────────────────────┘ │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                       Data Layer                         │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Repositories ◄─► Data Sources (API, Storage)     │ │
│  └────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### Project Structure
```
app/src/main/java/com/yunho/nanobanana/
├── presentation/                # Presentation Layer (UI State & ViewModels)
│   ├── viewmodel/
│   │   └── MainViewModel.kt    # State management with single StateFlow
│   └── state/
│       └── MainUiState.kt      # Immutable UI state data classes
├── domain/                      # Domain Layer (Business Logic - No Android deps)
│   ├── model/                   # Domain models and enums
│   │   ├── ImageGenerationRequest.kt
│   │   └── AIGenerationResult.kt
│   ├── usecase/                 # Business logic use cases
│   │   ├── GenerateAIContentUseCase.kt
│   │   └── SettingsUseCases.kt
│   └── repository/              # Repository interfaces
│       ├── AIRepository.kt
│       └── SettingsRepository.kt
├── data/                        # Data Layer (Data Management)
│   ├── repository/              # Repository implementations
│   │   ├── AIRepositoryImpl.kt
│   │   └── SettingsRepositoryImpl.kt
│   └── datasource/              # Data source implementations
│       ├── GeminiAIDataSource.kt  # Gemini API integration
│       └── SettingsDataSource.kt  # SharedPreferences wrapper
├── di/                          # Dependency Injection
│   └── AppContainer.kt         # Manual DI container
├── components/                  # Reusable UI Components
│   ├── Generate.kt             # Animated generation button
│   ├── ResultImage.kt          # Image display with gestures
│   ├── LoadingEffects.kt       # Advanced loading animations
│   └── ...                     # Other UI components
├── ai/                          # Legacy AI services (being phased out)
│   ├── PromptManager.kt
│   └── EnhancedAIService.kt
├── animations/                  # Animation utilities
│   └── MaterialMotion.kt       # Material Design 3 motion specs
├── extension/                   # Kotlin extensions
│   └── Context.kt
├── ui/theme/                    # Material Design 3 theming
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── MainActivity.kt              # App entry point
```

### Key Architectural Benefits

1. **Testability**: Each layer can be tested independently with mocks
2. **Scalability**: Easy to add new features without modifying existing code
3. **Maintainability**: Clear boundaries make code easier to understand
4. **Reusability**: Business logic in domain layer is platform-independent
5. **Flexibility**: Easy to swap implementations (e.g., different AI providers)

## 🎨 Key Improvements

### Animation System
- **Material Motion Tokens**: Consistent timing and easing across all animations
- **Spring Physics**: Natural, bouncy interactions
- **Staggered Animations**: Progressive reveal of list items
- **Press Feedback**: Immediate visual and haptic response to user interactions
- **Ripple Effects**: Synchronized multi-layer ripples for AI processing indication

### Loading States
- **Adaptive Blur**: Dynamic intensity based on image size and loading duration
- **Shimmer Effects**: Smooth gradient loading placeholders
- **Ripple Animations**: Three-layer synchronized ripples for processing indication
- **Blur Overlays**: Professional multi-layered blur during image processing
- **Skeleton Loaders**: Pulsing placeholders for cards and images
- **Progress Indicators**: Custom animated loading spinners with glow effects

### Gesture & Haptic Feedback
- **Pinch-to-Zoom**: Full zoom support (1x to 3x) on generated images
- **Double-Tap**: Quick zoom toggle
- **Pan Gestures**: Navigate when zoomed
- **Haptic Intensity Levels**: Light, medium, and strong feedback
- **Interactive Elevation**: Shadow animations on press
- **Long-Press Support**: Extended press actions with feedback

### Text Presentation
- **Smooth Scrolling**: Vertical scroll for long text responses
- **Text Selection**: Full copy/paste support
- **Adaptive Formatting**: Dark mode and high-contrast support
- **Scroll Indicators**: Visual cues for scrollable content
- **Loading Shimmer**: Animated placeholders during text generation

### State Management
- **Kotlin StateFlow**: Reactive state updates with minimal latency
- **Real-time Sync**: Instant UI updates on state changes
- **Thread-Safe**: Concurrent-safe state management
- **Backward Compatible**: Legacy mutableState support maintained

### Error Handling
- **Retry Logic**: Up to 3 attempts with exponential backoff
- **User Feedback**: Clear error messages with helpful guidance
- **API Logging**: Detailed logs for debugging
- **Graceful Degradation**: App remains responsive during errors

## 📦 Dependencies

### Core
- **Jetpack Compose**: UI framework with Material 3
- **Kotlin Coroutines**: Async programming
- **OkHttp**: HTTP client with timeout configuration

### Google Services
- **Firebase BOM**: Firebase integration
- **Gemini AI**: Google's latest AI model

### Utilities
- **Kotlinx Serialization**: JSON handling
- **AndroidX Core KTX**: Kotlin extensions

## 🧪 Testing

### Comprehensive Test Coverage

NanoBanana includes extensive testing at all architectural layers:

#### Unit Tests (app/src/test/)
- **Domain Layer Tests**: Pure business logic testing
  - `GenerateAIContentUseCaseTest`: Use case validation
  - Model validation and business rules
- **Data Layer Tests**: Repository and data source testing
  - `AIRepositoryImplTest`: Repository logic verification
  - Mock-based testing with Mockito
- **Presentation Layer Tests**: ViewModel state management
  - `MainViewModelTest`: UI state transitions
  - Flow testing with Turbine library
  - Coroutine testing

#### Integration Tests (app/src/androidTest/)
- Complete feature flow testing
- AI service integration tests
- End-to-end user journey validation

#### Test Technologies
- **JUnit 4**: Test framework
- **Mockito Kotlin**: Mocking framework
- **Turbine**: Flow testing library
- **Coroutines Test**: Async testing utilities
- **Compose Test**: UI component testing

### Running Tests

```bash
# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests MainViewModelTest

# Run all instrumented tests
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew testDebugUnitTestCoverage
```

### CI/CD Pipeline

Automated testing via GitHub Actions:
- ✅ Unit tests on every push
- ✅ Lint checks
- ✅ Build verification
- ✅ Instrumented tests on emulator
- ✅ Code quality checks

See [TESTING.md](TESTING.md) for detailed testing strategies and guidelines.

## 📐 Build Information

- **Compile SDK**: 36
- **Min SDK**: 28
- **Target SDK**: 36
- **Version**: 1.0
- **Kotlin**: 2.0.21
- **Gradle**: 8.13
- **AGP**: 8.1.4

## 📚 Documentation

Comprehensive documentation for developers and contributors:

### Architecture & Design
- **[ARCHITECTURE.md](ARCHITECTURE.md)**: Complete architecture guide
  - Layer responsibilities and boundaries
  - Data flow diagrams
  - Dependency injection setup
  - Best practices and patterns
  - AI integration architecture
  
- **[AI_INTEGRATION_STRATEGY.md](AI_INTEGRATION_STRATEGY.md)**: 🔥 NEW! Detailed AI architecture
  - Dual data streams (visual + semantic outputs)
  - Asynchronous processing pipeline
  - Caching and retry strategies
  - Device adaptation and performance
  - Security considerations
  
- **[ARCHITECTURAL_REFACTORING_PLAN.md](ARCHITECTURAL_REFACTORING_PLAN.md)**: 🔥 NEW! Future roadmap
  - 16-month evolution plan (Q4 2024 - Q1 2026)
  - Hilt DI migration strategy
  - Multi-module architecture
  - MVI pattern enhancement
  - Kotlin Multiplatform support

### Testing Strategy
- **[TESTING.md](TESTING.md)**: Current testing strategy
  - Unit, integration, and UI testing
  - Test organization and patterns
  - CI/CD pipeline configuration
  - Coverage goals and metrics
  
- **[TESTING_ROADMAP.md](TESTING_ROADMAP.md)**: 🔥 NEW! Comprehensive testing roadmap
  - 14-month testing strategy (Q1 2025 - Q1 2026)
  - Edge cases and error scenarios
  - Multi-modal AI integration testing
  - Performance and load testing
  - UI/UX test automation
  - CI/CD enhancement
  
### Development & Quality
- **[CONTRIBUTING.md](CONTRIBUTING.md)**: 🔥 NEW! Contribution guidelines
  - Code of conduct
  - Kotlin coding standards
  - Architecture best practices
  - Testing requirements
  - Pull request process
  - Code review guidelines

- **[BUILD_OPTIMIZATION.md](BUILD_OPTIMIZATION.md)**: 🔥 NEW! Build optimization guide
  - Gradle performance optimizations
  - Kotlin compiler settings
  - APK size reduction strategies
  - CI/CD configuration
  - Troubleshooting guide

### Implementation Guides
- **[ASYNC_ARCHITECTURE.md](ASYNC_ARCHITECTURE.md)**: Async architecture guide
  - Parallel processing pipelines
  - Caching strategies
  - Task prioritization
  - Device adaptation and graceful degradation
  - Performance optimization techniques
  - Monitoring and telemetry
  
- **[ASYNC_IMPLEMENTATION_SUMMARY.md](ASYNC_IMPLEMENTATION_SUMMARY.md)**: Implementation summary
  - Architecture overview
  - Performance metrics
  - Security considerations
  - Production readiness checklist
  
- **[SECURITY_SUMMARY.md](SECURITY_SUMMARY.md)**: Security review
  - Vulnerability analysis
  - Thread safety
  - Data security
  - Best practices
  
- **[MODERNIZATION.md](MODERNIZATION.md)**: Complete modernization details
  - Animation specifications
  - API integration details
  - Best practices implemented
  
### UI/UX Documentation
- **[UI_ENHANCEMENTS.md](UI_ENHANCEMENTS.md)**: UI/UX enhancements
  - Multi-layered adaptive blur effects
  - Shimmer and ripple animations
  - Haptic feedback and gesture support

- **[ADVANCED_UI_UX_ENHANCEMENTS.md](ADVANCED_UI_UX_ENHANCEMENTS.md)**: Advanced UI/UX features
  - WCAG AA/AAA accessibility compliance
  - Dynamic typography and responsive design
  - Swipe gestures for undo/redo
  - Enhanced skeleton loaders and transitions

### Roadmap & Planning
- **[ROADMAP.md](ROADMAP.md)**: Product roadmap
  - Completed features (Phase 1)
  - In-progress items (Phase 2)
  - Planned features (Phases 3-6)
  - Success metrics and timeline

- **[REFACTORING_COMPLETE_SUMMARY.md](REFACTORING_COMPLETE_SUMMARY.md)**: 🔥 NEW! Refactoring summary
  - Executive summary of all refactoring work
  - Accomplishments and status
  - Architecture principles
  - Success metrics and next steps

## 📦 Contributing

We welcome contributions! Please see our [CONTRIBUTING.md](CONTRIBUTING.md) guide for:
- Code of conduct
- Development setup
- Kotlin coding standards
- Architecture guidelines
- Testing requirements
- Pull request process

### Quick Start

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Follow the coding standards in [CONTRIBUTING.md](CONTRIBUTING.md)
4. Write tests for your changes
5. Ensure all tests pass (`./gradlew check`)
6. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
7. Push to the branch (`git push origin feature/AmazingFeature`)
8. Open a Pull Request

### Development Guidelines
- Follow Kotlin idiomatic best practices
- Add animations for new UI elements
- Include accessibility support
- Write comprehensive KDoc comments
- Test on multiple screen sizes
- Maintain or improve code coverage

For detailed guidelines, see [CONTRIBUTING.md](CONTRIBUTING.md).

## 📄 License

This project is for educational and personal use. Please ensure you comply with:
- Google's AI API terms of service
- Material Design usage guidelines
- Android development best practices

## 🙏 Acknowledgments

- Google for the Gemini AI API
- Material Design team for the design system
- Android team for Jetpack Compose
- Open source community

## 📞 Support

For issues and questions:
- Create an issue in the GitHub repository
- Check existing issues for solutions
- Review the [MODERNIZATION.md](MODERNIZATION.md) documentation

## 🎯 Future Enhancements

Potential features to add:
- [ ] More AI transformation styles
- [ ] Image filters and adjustments
- [ ] Batch processing
- [ ] History of generations
- [ ] Share functionality
- [ ] Custom model parameters
- [ ] Offline mode with caching
- [ ] Dark theme enhancements

---

**Made with ❤️ using Jetpack Compose and Material Design 3**
