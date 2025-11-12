# Quick Start Guide - NanoBanana Flutter App

Get started with NanoBanana development in 5 minutes!

## Prerequisites Check

```bash
# Check Flutter installation
flutter --version

# Check Dart installation
dart --version

# Check connected devices
flutter devices
```

## Setup Steps

### 1. Clone and Navigate
```bash
git clone https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit
```

### 2. Install Dependencies
```bash
flutter pub get
```

### 3. Get Your API Key
1. Visit [Google AI Studio](https://ai.google.dev/)
2. Create a new project
3. Generate an API key
4. Copy the API key (you'll enter it in the app)

### 4. Run the App
```bash
# List available devices
flutter devices

# Run on Android
flutter run -d android

# Run on iOS (macOS only)
flutter run -d ios

# Run on Chrome (for testing)
flutter run -d chrome
```

### 5. First Run
1. App will prompt for API key
2. Paste your Google AI API key
3. Click "Save"
4. You're ready to go! 🎉

## Common Commands

### Development
```bash
# Hot reload (press 'r' while app is running)
# Hot restart (press 'R' while app is running)

# Run with specific device
flutter run -d <device_id>

# Run in release mode
flutter run --release

# Enable verbose logging
flutter run -v
```

### Testing
```bash
# Run all tests
flutter test

# Run specific test file
flutter test test/models_test.dart

# Run with coverage
flutter test --coverage

# View coverage report
genhtml coverage/lcov.info -o coverage/html
open coverage/html/index.html  # macOS
```

### Code Quality
```bash
# Format all Dart code
flutter format .

# Analyze code for issues
flutter analyze

# Fix some analysis issues automatically
dart fix --apply
```

### Building
```bash
# Build APK (Android)
flutter build apk

# Build App Bundle (Android - for Play Store)
flutter build appbundle

# Build iOS (macOS only)
flutter build ios

# Build for web
flutter build web
```

## Project Structure

```
Android-NanoBanana-Image-Edit/
├── lib/                          # Main application code
│   ├── main.dart                # App entry point
│   ├── models/                  # Data models
│   ├── services/                # Business logic
│   ├── providers/               # State management
│   ├── screens/                 # UI screens
│   ├── widgets/                 # Reusable components
│   └── utils/                   # Utilities
├── test/                        # Unit & widget tests
├── android/                     # Android-specific code
├── ios/                         # iOS-specific code
├── pubspec.yaml                 # Dependencies
└── README.md                    # Documentation
```

## Key Features to Try

1. **Image Selection**
   - Tap "Pick Images" button
   - Select one or more photos from gallery

2. **AI Generation**
   - Choose a creative style (Korean Historical, Collectible Figure, etc.)
   - Or select "Custom Prompt" and write your own
   - Tap "Generate" to create AI-transformed image

3. **Image Enhancement**
   - After generation, tap "Enhance" for detail improvement
   - Uses AI to sharpen and refine the image

4. **Save Variants**
   - Save different versions as variants
   - Compare and choose your favorite

## Troubleshooting

### "Flutter not found"
```bash
# Add Flutter to PATH
export PATH="$PATH:/path/to/flutter/bin"

# Verify
flutter doctor
```

### "No connected devices"
```bash
# For Android
# - Enable USB debugging on Android device
# - Or start Android emulator

# For iOS (macOS only)
# - Open Xcode
# - Start iOS simulator

# For Web
# - Chrome should be detected automatically
```

### "Build failed"
```bash
# Clean and rebuild
flutter clean
flutter pub get
flutter run
```

### "API key error"
```bash
# Make sure you:
# 1. Created API key at https://ai.google.dev/
# 2. Enabled Gemini API
# 3. Entered key correctly in app settings
```

### Missing dependencies
```bash
# Reinstall dependencies
flutter pub get

# If still issues, try:
flutter pub cache repair
```

## Development Tips

### Hot Reload
- Press `r` while app is running to hot reload
- Changes appear instantly without restart
- Works for most UI changes

### Debug Prints
```dart
// Add debug prints
print('Debug: $variableName');
debugPrint('Debug message');
```

### VS Code Setup
Install extensions:
- Flutter
- Dart
- Flutter Widget Snippets

### Android Studio Setup
- Install Flutter plugin
- Install Dart plugin
- Use Android emulator for testing

## Next Steps

1. Read [README.md](README.md) for full documentation
2. Check [ARCHITECTURE.md](ARCHITECTURE.md) for app structure
3. See [CONTRIBUTING_FLUTTER.md](CONTRIBUTING_FLUTTER.md) for contribution guidelines
4. Review [FLUTTER_MIGRATION.md](FLUTTER_MIGRATION.md) for migration details

## Need Help?

- Check existing [GitHub Issues](https://github.com/Aether67/Android-NanoBanana-Image-Edit/issues)
- Create a new issue for bugs
- Start a discussion for questions

## Resources

- [Flutter Documentation](https://flutter.dev/docs)
- [Dart Language Tour](https://dart.dev/guides/language/language-tour)
- [Provider Package](https://pub.dev/packages/provider)
- [Google AI Documentation](https://ai.google.dev/docs)

Happy coding! 🚀
