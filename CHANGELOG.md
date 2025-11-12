# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.0.0] - 2025-11-12

### Changed - Complete Flutter Migration

**BREAKING CHANGE:** Complete migration from Android (Kotlin/Jetpack Compose) to Flutter

This is a complete rewrite of the application using Flutter, enabling cross-platform support for both iOS and Android.

#### Added
- Flutter application structure with Provider state management
- Cross-platform support (iOS + Android)
- New Flutter-based UI components
- Dart implementation of all business logic
- Flutter testing infrastructure
- Platform-specific configurations for Android and iOS
- Migration documentation (FLUTTER_MIGRATION.md)
- Updated contribution guidelines (CONTRIBUTING_FLUTTER.md)

#### Changed
- **Architecture**: From Android MVVM to Flutter with Provider
- **Language**: From Kotlin to Dart
- **UI Framework**: From Jetpack Compose to Flutter Widgets
- **State Management**: From StateFlow/ViewModel to Provider/ChangeNotifier
- **Async Processing**: From Kotlin Coroutines to Dart Future/Stream
- **Platform Support**: From Android-only to iOS + Android

#### Migrated Features
- AI image generation using Gemini API
- Image enhancement capabilities
- Image selection from gallery
- API key management with secure storage
- Creative style templates
- Image variant management
- Material Design 3 theming
- Dark/Light mode support
- Error handling and loading states

#### Technical Details
- Minimum Flutter SDK: 3.0.0
- Minimum Dart SDK: 3.0.0
- Minimum Android SDK: 21 (Android 5.0)
- Minimum iOS: 12.0
- State Management: Provider ^6.1.1
- AI Integration: google_generative_ai ^0.2.2
- Image Handling: image_picker ^1.0.7

#### Files Created
- `lib/` - Main Flutter application code
  - `main.dart` - App entry point
  - `models/` - Domain models
  - `services/` - Business logic services
  - `providers/` - State management
  - `screens/` - UI screens
  - `widgets/` - Reusable widgets
  - `utils/` - Utilities and theme
- `android/` - Android platform configuration
- `ios/` - iOS platform configuration
- `test/` - Dart unit tests
- `pubspec.yaml` - Flutter dependencies
- `analysis_options.yaml` - Dart linter configuration
- `FLUTTER_MIGRATION.md` - Migration documentation

#### Documentation Updates
- Updated README.md for Flutter
- Updated .gitignore for Flutter project structure
- Added Flutter-specific contributing guidelines
- Created comprehensive migration guide

#### Known Limitations
- Old Android Kotlin code remains for reference (will be removed in future release)
- Some advanced Android-specific features may need reimplementation
- iOS testing requires macOS environment

## [1.0.0] - 2024 (Android Version)

### Initial Release - Android App

- Android app using Kotlin and Jetpack Compose
- AI image generation with Gemini 2.5 Flash
- Image enhancement features
- MVVM architecture with Clean Architecture principles
- Comprehensive test coverage (80%+)
- Material Design 3 UI
- Kotlin Coroutines for async operations
- LRU caching system
- Error resilience with retry mechanisms

---

For complete migration details, see [FLUTTER_MIGRATION.md](FLUTTER_MIGRATION.md)
