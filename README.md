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
  - WCAG AA compliant contrast ratios
  - High-contrast display option

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

### Modern Architecture
- **MVVM Pattern**: Clean separation of UI, business logic, and data
- **Jetpack Compose**: Modern declarative UI toolkit
- **Kotlin Coroutines & Flow**: Smooth asynchronous operations with reactive state management
- **StateFlow Integration**: Real-time UI updates with minimal perceived latency
- **Material Motion**: Official Material Design 3 motion specifications

### Performance Optimizations
- **Async Processing**: Non-blocking image generation
- **Reactive Updates**: StateFlow for instant UI synchronization
- **Progressive Loading**: Multi-layered adaptive blur effects
- **Optimized Bitmap Handling**: Efficient image compression and encoding
- **Smart Caching**: Persistent API key storage

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

### Project Structure
```
app/src/main/java/com/yunho/nanobanana/
├── MainActivity.kt              # Main activity with enhanced UI composition
├── NanoBanana.kt               # Core business logic and state management
├── NanoBananaService.kt        # Enhanced AI service with retry logic
├── animations/                  # Animation utilities
│   └── MaterialMotion.kt       # Material Design 3 motion specifications
├── components/                  # Reusable UI components
│   ├── ApiKeySetting.kt       # API key configuration UI
│   ├── Generate.kt            # Animated generation button with haptics
│   ├── LoadingEffects.kt      # Shimmer, ripple, skeleton, and adaptive blur effects
│   ├── PickedImages.kt        # Selected images with staggered animations
│   ├── Prompt.kt              # Text prompt input
│   ├── ResultImage.kt         # Generated image with zoom gestures and reveal animation
│   ├── Save.kt                # Animated save button
│   ├── SelectImages.kt        # Image selection with animations
│   ├── StylePicker.kt         # Style preset carousel
│   ├── Reset.kt               # Reset with rotating icon
│   ├── PickerTitle.kt         # Title component
│   ├── TextOutput.kt          # Elegant text display with scrolling and selection
│   └── HapticGestures.kt      # Haptic feedback and gesture utilities
├── extension/                  # Utility extensions
│   └── Context.kt             # Context extension functions
└── ui/theme/                   # Material Design 3 theming
    ├── Color.kt               # Color palette
    ├── Theme.kt               # Dynamic theming
    └── Type.kt                # Typography
```

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

## 🔧 Build Information

- **Compile SDK**: 36
- **Min SDK**: 28
- **Target SDK**: 36
- **Version**: 1.0
- **Kotlin**: 2.0.21
- **Gradle**: 8.13
- **AGP**: 8.1.4

## 📚 Documentation

See these documentation files for detailed information:
- [MODERNIZATION.md](MODERNIZATION.md) - Complete modernization details
  - Animation specifications
  - API integration details
  - Best practices implemented
  - Testing recommendations
- [UI_ENHANCEMENTS.md](UI_ENHANCEMENTS.md) - Comprehensive UI/UX enhancements
  - Multi-layered adaptive blur effects
  - Shimmer and ripple animations
  - Elegant text output presentation
  - Kotlin Flow & StateFlow integration
  - Haptic feedback and gesture support
  - Testing scenarios and coverage guidelines

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Make your changes following the existing code style
4. Add comprehensive documentation
5. Test your changes thoroughly
6. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
7. Push to the branch (`git push origin feature/AmazingFeature`)
8. Open a Pull Request

### Development Guidelines
- Follow Material Design 3 guidelines
- Add animations for new UI elements
- Include accessibility support
- Write KDoc comments
- Test on multiple screen sizes

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
