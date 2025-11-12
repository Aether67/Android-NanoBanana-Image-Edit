# NanoBanana - AI Image Editor

A modern Flutter app powered by Google's Gemini AI to transform and enhance your images with professional-grade UI and performance.

## ✨ Features

### Core Features
- **AI Image Generation**: Transform photos using Gemini 2.0 Flash AI
- **Image Enhancement**: AI-powered detail sharpening and quality improvement
- **Multi-image Support**: Select and process multiple images simultaneously
- **Creative Styles**: 5 preset styles (Photorealistic, Artistic, Anime, Sci-Fi, Custom)
- **Cross-platform**: Single codebase for iOS and Android

### UI & Design
- **Dark Minimalist Theme**: OLED-optimized pure black design with cyan accents
- **Smooth Animations**: 60+ FPS with staggered entrance effects
- **High Refresh Rate**: 90Hz/120Hz support on compatible devices
- **Material Design 3**: Modern, responsive, and accessible interface
- **Blur Effects**: Beautiful backdrop blur during image generation
- **Progress Indicators**: Real-time feedback with progress messages

### Advanced Features (Beta)
- **Advanced AI Parameters**: Fine-tune creativity, detail, and reasoning depth
- **Batch Processing**: Process multiple images at once (coming soon)
- **Image History**: Track generated images (coming soon)
- **Variant Comparison**: Compare variants side-by-side (coming soon)

## 📱 Requirements

- Flutter SDK 3.0.0+
- Android 5.0+ (API 21+) / iOS 12.0+
- Google AI API key ([Get one free](https://ai.google.dev/))
- Internet connection for AI processing

## 🚀 Quick Start

```bash
# Clone repository
git clone https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit

# Install dependencies
flutter pub get

# Run the app
flutter run
```

## 📖 Usage

1. **Launch** the app - Opens with dark minimalist interface
2. **Configure API Key** - Enter your Google AI API key (one-time setup)
3. **Select Images** - Choose one or more images from your gallery
4. **Pick Style** - Select a creative style or use custom prompt
5. **Generate** - Tap Generate to transform images with AI
6. **Enhance** - Use Enhance button for additional quality improvement
7. **Save** - Save generated variants to your collection

### Beta Features
Access experimental features in Settings → Beta Features:
- Toggle **Advanced AI Parameters** for fine-tuned control
- Adjust creativity, detail level, and reasoning depth in real-time

## 🏗️ Build

### Android
```bash
# Debug build
flutter build apk --debug

# Release build
flutter build apk --release

# App Bundle for Play Store
flutter build appbundle --release
```

### iOS
```bash
# Build for device
flutter build ios --release

# Create archive
flutter build ipa
```

## 🎨 Architecture

### State Management
- **Provider** pattern with ChangeNotifier
- Reactive UI updates with Consumer widgets
- Centralized state in AppProvider

### Layers
- **Presentation**: Screens and reusable widgets
- **Business Logic**: Providers and services
- **Data**: Models and API integration

### Key Technologies
- `provider ^6.1.1` - State management
- `google_generative_ai ^0.2.2` - Gemini AI integration
- `image_picker ^1.0.7` - Image selection
- `image ^4.1.7` - Image processing
- `shared_preferences ^2.2.2` - Local storage

## 🎯 Performance

- **High Refresh Rate**: 90Hz/120Hz support enabled
- **Optimized Animations**: Smooth 60+ FPS throughout
- **Efficient Rendering**: Minimalist design reduces GPU overhead
- **Smart Caching**: Reuses image data to reduce memory usage
- **Lazy Loading**: Widgets built on-demand

## 🔒 Security

- API keys stored securely in SharedPreferences
- No hardcoded credentials
- Input validation on all user inputs
- Error messages don't expose sensitive data
- Network requests with timeout protection

## 📁 Project Structure

```
lib/
├── main.dart                 # App entry point
├── models/                   # Data models
│   ├── ai_generation_result.dart
│   ├── ai_parameters.dart
│   ├── image_variant.dart
│   └── main_ui_state.dart
├── providers/                # State management
│   └── app_provider.dart
├── screens/                  # UI screens
│   ├── main_screen.dart
│   └── settings_screen.dart
├── services/                 # Business logic
│   ├── gemini_ai_service.dart
│   └── settings_service.dart
├── utils/                    # Utilities
│   ├── constants.dart
│   └── theme.dart
└── widgets/                  # Reusable components
    ├── action_buttons.dart
    ├── advanced_ai_parameters.dart
    ├── api_key_dialog.dart
    ├── generation_display.dart
    ├── image_picker_section.dart
    ├── prompt_input_section.dart
    └── style_picker_section.dart
```

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📄 License

For educational and personal use. Please comply with:
- Google's Gemini AI API [Terms of Service](https://ai.google.dev/terms)
- Flutter's [License](https://github.com/flutter/flutter/blob/master/LICENSE)

## 🐛 Issues & Support

- **Bug Reports**: [Create an issue](https://github.com/Aether67/Android-NanoBanana-Image-Edit/issues)
- **Feature Requests**: Open a discussion
- **Questions**: Check existing issues or create new one

## 🙏 Acknowledgments

- Google Gemini AI for powerful image generation
- Flutter team for excellent framework
- Material Design 3 for modern design system

---

**Built with ❤️ using Flutter & Gemini AI**

*Cross-platform • Modern • Fast • Accessible*
