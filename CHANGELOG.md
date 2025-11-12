# Changelog

## [2.0.0] - 2025-11-12

### Complete Flutter Migration

**BREAKING CHANGE:** Complete rewrite from Android to Flutter

#### Added
- Flutter application with cross-platform support (iOS + Android)
- Provider state management
- Material Design 3 UI
- Comprehensive Dart implementation

#### Changed
- Platform: Android-only → iOS + Android
- Language: Kotlin → Dart
- UI: Jetpack Compose → Flutter Widgets
- State: ViewModel/StateFlow → Provider/ChangeNotifier

#### Features
- AI image generation using Gemini API
- Image enhancement
- Multi-image selection
- API key management
- 5 creative style presets
- Custom prompts
- Image variants
- Dark/Light mode

#### Technical
- Min Android SDK: 28 → 21 (broader compatibility)
- Min iOS: 12.0
- State Management: Provider ^6.1.1
- AI: google_generative_ai ^0.2.2

## [1.0.0] - 2024

### Initial Android Release

- Android app with Kotlin and Jetpack Compose
- AI image generation with Gemini
- MVVM architecture
- Material Design 3
