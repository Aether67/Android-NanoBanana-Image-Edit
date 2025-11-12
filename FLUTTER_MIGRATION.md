# Flutter Migration Guide

## Overview

This document outlines the complete migration of NanoBanana from Android (Kotlin/Jetpack Compose) to Flutter.

## Migration Summary

### Architecture Changes

**From: Android MVVM with Clean Architecture**
- Presentation: Jetpack Compose + ViewModel + StateFlow
- Domain: Use Cases + Models
- Data: Repositories + Data Sources

**To: Flutter with Provider Pattern**
- Presentation: Flutter Widgets + Provider
- Services: AI Service + Settings Service
- Models: Domain Models

### Key Differences

| Aspect | Android (Before) | Flutter (After) |
|--------|-----------------|-----------------|
| **Language** | Kotlin | Dart |
| **UI Framework** | Jetpack Compose | Flutter Widgets |
| **State Management** | StateFlow + ViewModel | Provider |
| **Async** | Coroutines + Flow | Future + Stream |
| **DI** | Manual DI Container | Provider injection |
| **Platform** | Android only | iOS + Android |

## File Structure

### Flutter Project Structure
```
lib/
├── main.dart                 # App entry point
├── models/                   # Domain models
│   ├── ai_generation_result.dart
│   ├── ai_parameters.dart
│   ├── image_variant.dart
│   └── main_ui_state.dart
├── services/                 # Business logic services
│   ├── gemini_ai_service.dart
│   └── settings_service.dart
├── providers/                # State management
│   └── app_provider.dart
├── screens/                  # Full-screen views
│   └── main_screen.dart
├── widgets/                  # Reusable components
│   ├── action_buttons.dart
│   ├── api_key_dialog.dart
│   ├── generation_display.dart
│   ├── image_picker_section.dart
│   ├── prompt_input_section.dart
│   └── style_picker_section.dart
└── utils/                    # Utilities
    └── theme.dart

test/                         # Unit tests
android/                      # Android-specific config
ios/                         # iOS-specific config
```

## Component Migration Map

### Models
- `MainUiState.kt` → `main_ui_state.dart`
- `AIGenerationResult.kt` → `ai_generation_result.dart`
- `ImageVariant.kt` → `image_variant.dart`
- `AIParameters.kt` → `ai_parameters.dart`

### Services
- `GeminiAIDataSource.kt` → `gemini_ai_service.dart`
- `SettingsDataSource.kt` → `settings_service.dart`

### State Management
- `MainViewModel.kt` → `app_provider.dart`
- StateFlow → ChangeNotifier

### UI Components
- `MainActivity.kt` → `main.dart`
- Compose Screens → Flutter Screens
- Composable functions → Widget classes

## Key Features Migrated

✅ **Core Features:**
- AI image generation using Gemini API
- Image enhancement
- Image selection from gallery
- API key management
- Creative style templates
- Image variant management

✅ **UI/UX:**
- Material Design 3
- Dark/Light theme
- Responsive layouts
- Loading states
- Error handling

✅ **Data Persistence:**
- SharedPreferences for settings
- API key storage

## Dependencies

### Flutter Packages Used
```yaml
dependencies:
  flutter:
    sdk: flutter
  
  # State Management
  provider: ^6.1.1
  
  # Google AI
  google_generative_ai: ^0.2.2
  http: ^1.1.2
  
  # Image Handling
  image_picker: ^1.0.7
  image: ^4.1.7
  
  # Storage
  shared_preferences: ^2.2.2
  path_provider: ^2.1.2
  
  # Utilities
  uuid: ^4.3.3
  rxdart: ^0.27.7
  permission_handler: ^11.2.0
```

## Platform Support

### Android
- Minimum SDK: 21 (Android 5.0)
- Target SDK: 34 (Android 14)
- Permissions: Internet, Storage, Camera

### iOS
- Minimum iOS: 12.0
- Permissions: Photo Library, Camera

## Testing

### Test Coverage
- Unit tests for models
- Widget tests for UI components
- Integration tests for full flows

### Running Tests
```bash
flutter test                    # All tests
flutter test --coverage         # With coverage
```

## Build & Run

### Development
```bash
flutter pub get                 # Get dependencies
flutter run                     # Run on device/emulator
flutter run --release          # Release mode
```

### Production Builds
```bash
flutter build apk              # Android APK
flutter build appbundle        # Android App Bundle
flutter build ios              # iOS build
```

## Migration Benefits

### Advantages
✅ **Cross-platform**: Single codebase for iOS and Android
✅ **Hot Reload**: Faster development iteration
✅ **Rich UI**: Flutter's extensive widget library
✅ **Performance**: Compiled to native code
✅ **Community**: Large and active Flutter community
✅ **Maintainability**: Easier to maintain single codebase

### Considerations
⚠️ **Learning Curve**: Team needs to learn Dart/Flutter
⚠️ **Platform Features**: Some native features may need platform channels
⚠️ **App Size**: Flutter apps can be larger initially

## Future Enhancements

### Planned Features
- [ ] Advanced image editing controls
- [ ] Batch processing
- [ ] Cloud storage integration
- [ ] Social sharing
- [ ] Image history with search
- [ ] Custom AI model fine-tuning

### Technical Debt
- [ ] Add comprehensive widget tests
- [ ] Implement integration tests
- [ ] Add performance monitoring
- [ ] Optimize build size
- [ ] Add CI/CD pipeline for Flutter

## Resources

### Documentation
- [Flutter Docs](https://flutter.dev/docs)
- [Dart Language Tour](https://dart.dev/guides/language/language-tour)
- [Provider Package](https://pub.dev/packages/provider)
- [Google Generative AI](https://pub.dev/packages/google_generative_ai)

### Tools
- [DartPad](https://dartpad.dev) - Online Dart/Flutter playground
- [FlutterFire](https://firebase.flutter.dev) - Firebase for Flutter
- [VS Code Flutter Extension](https://marketplace.visualstudio.com/items?itemName=Dart-Code.flutter)

## Conclusion

The migration to Flutter opens up the app to a wider audience by supporting both iOS and Android platforms while maintaining feature parity with the original Android app. The new architecture is cleaner, more maintainable, and leverages Flutter's powerful UI framework for a better user experience.
