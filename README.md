# NanoBanana - AI Image Editor (Flutter)

An advanced Flutter app powered by Google's Gemini AI to transform and enhance your images with AI.

> **Note:** This app has been completely migrated from Android (Kotlin/Jetpack Compose) to Flutter for cross-platform support.

<br>
<img src="https://github.com/user-attachments/assets/fa7e4138-fc0c-48aa-b8cf-02e9756f6455" width="250"/>
<img src="https://github.com/user-attachments/assets/97fa5bf1-c46d-4e6a-8da4-1ff7b98d07b3" width="250"/>
<img src="https://github.com/user-attachments/assets/3b38d3fa-ca3e-4bd2-a431-954347e6f22d" width="250"/>

## ✨ Key Features

### AI-Powered Capabilities
- **Image Generation**: Transform photos using Gemini 2.5 Flash Image Preview
- **Image Enhancement**: AI-powered detail sharpening and texture refinement
- **Interactive Zoom**: Pinch-to-zoom (1x-3x) with automatic localized enhancement at >2x
- **Smart Text Generation**: Contextual explanations and reasoning for AI outputs
- **Multi-Modal Output**: Simultaneous image and text generation

### Modern UI/UX
- **Material Design 3**: Beautiful, responsive interface with dynamic theming
- **Smooth Animations**: Spring physics, staggered entrances, interactive feedback
- **Advanced Loading States**: Blur effects, skeleton loaders, shimmer animations
- **Haptic Feedback**: Tactile responses throughout the app
- **Accessibility**: WCAG AA/AAA compliant, screen reader support, high-contrast mode
- **Gesture Controls**: Pinch-zoom, double-tap reset, swipe gestures

### Technical Excellence
- **Clean Architecture**: MVVM pattern with clear layer separation
- **Async Processing**: Kotlin Coroutines & Flow for non-blocking operations
- **Smart Caching**: LRU cache with 60-70% memory reduction
- **Error Resilience**: Comprehensive error handling with automatic retry
- **80%+ Test Coverage**: Unit, integration, and UI tests

## 📋 Requirements

- **Flutter SDK 3.0.0** or higher
- **Android 5.0** (API level 21) or higher / **iOS 12.0** or higher
- **Google AI API key** (Gemini API)
- **Internet connection** for AI processing
- **100MB+ free storage** for high-resolution images

## 🛠️ Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit
```

### 2. Get API Key
- Visit [Google AI Studio](https://ai.google.dev/)
- Create a new API key
- Copy for use in the app

### 3. Build & Run
```bash
# Get dependencies
flutter pub get

# Run on connected device
flutter run

# Build APK for Android
flutter build apk

# Build for iOS
flutter build ios
```

## 📖 Usage

1. **Setup**: Enter your Google AI API key in settings
2. **Select Images**: Choose photos from your gallery
3. **Choose Style**: Pick from presets or enter custom prompt
4. **Generate**: Tap Generate to create AI-transformed image
5. **Enhance**: Use the ✨ Enhance button for detail sharpening
6. **Zoom**: Pinch to zoom >2x for automatic localized enhancement
7. **Save**: Export to gallery or share

## 🏗️ Architecture

NanoBanana follows **Clean Architecture** principles with **Provider** state management:

```
Presentation (Widgets + Provider)
    ↓
Services (AI Service + Settings)
    ↓
Models (Domain Models)
    ↓
Gemini AI API
```

**Key Components:**
- **Models**: Pure Dart business logic and data classes
- **Services**: API integration and data persistence
- **Providers**: State management using Provider pattern
- **Widgets**: Reusable Flutter UI components

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed architecture documentation.

## 🎨 Features In Detail

### Image Enhancement
The AI enhancement feature provides:
- **Full-Image Enhancement**: Sharpen entire image with detail preservation
- **Localized Enhancement**: Zoom >2x to enhance visible regions
- **Seamless Blending**: Natural integration without artifacts
- **Smart Limitations**: 4MP max for full-image, 10MP for regional

See [IMAGE_ENHANCEMENT_GUIDE.md](IMAGE_ENHANCEMENT_GUIDE.md) for complete documentation.

### Creative Styles
- Korean Historical (Chosun Dynasty 1900s)
- Collectible Figure (Hyper-realistic with packaging)
- Rock-Paper-Scissors (Interactive game scenarios)
- Shopping Scene (3D Costco style)
- Custom Prompts (Your own creative ideas)

## 🧪 Testing

### Run Tests
```bash
# Unit tests
flutter test

# Widget tests (requires Flutter SDK)
flutter test test/

# Integration tests
flutter test integration_test/

# Test coverage
flutter test --coverage
```

### Coverage
- **Unit Tests**: 80%+
- **Integration Tests**: Complete flows
- **UI Tests**: Critical components

See [TESTING.md](TESTING.md) for testing strategy and guidelines.

## 📚 Documentation

- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Complete documentation index
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Architecture guide with AI integration
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Contribution guidelines and standards
- **[IMAGE_ENHANCEMENT_GUIDE.md](IMAGE_ENHANCEMENT_GUIDE.md)** - Enhancement feature guide
- **[BUILD_OPTIMIZATION.md](BUILD_OPTIMIZATION.md)** - Build performance guide
- **[TESTING.md](TESTING.md)** - Testing strategy
- **[SECURITY_SUMMARY.md](SECURITY_SUMMARY.md)** - Security analysis
- **[ROADMAP.md](ROADMAP.md)** - Product roadmap

## 📦 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for:
- Code of conduct
- Development setup
- Kotlin coding standards
- Architecture guidelines
- Testing requirements
- Pull request process

### Quick Guidelines
1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Follow Kotlin best practices (see CONTRIBUTING.md)
4. Write tests for your changes
5. Ensure all tests pass (`./gradlew check`)
6. Commit with clear messages
7. Push and open a Pull Request

## 📐 Build Information

- **Flutter SDK**: 3.0.0+
- **Dart**: 3.0.0+
- **Min Android SDK**: 21
- **Target Android SDK**: 34
- **Min iOS**: 12.0
- **Dependencies**: See pubspec.yaml

See pubspec.yaml for complete dependency list.

## 🎯 Roadmap

**Current Version**: 1.0

**In Progress:**
- Code quality tooling (ktlint, Detekt)
- Enhanced test coverage (target 95%)
- UI/UX refinements

**Planned:**
- Multi-provider AI support
- Batch image processing
- Generation history with search
- Before/after comparison slider
- Offline mode with caching

See [ROADMAP.md](ROADMAP.md) for complete roadmap.

## 📄 License

This project is for educational and personal use. Please ensure you comply with:
- Google's AI API terms of service
- Material Design usage guidelines
- Android development best practices

## 🙏 Acknowledgments

- Google for the Gemini AI API
- Flutter team for the amazing framework
- Material Design team for the design system
- Open source community

## 📞 Support

For issues and questions:
- Create an issue in the GitHub repository
- Check existing issues for solutions
- Review the [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)

---

**Made with ❤️ using Flutter and Material Design 3**

**Powered by Google Gemini AI**
