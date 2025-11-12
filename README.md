# NanoBanana - AI Image Editor

A Flutter app powered by Google's Gemini AI to transform and enhance your images.

## Features

- **AI Image Generation**: Transform photos using Gemini AI
- **Image Enhancement**: AI-powered detail sharpening
- **Multi-image Support**: Select and process multiple images
- **Creative Styles**: 5 preset styles or custom prompts
- **Material Design 3**: Modern, responsive UI
- **Cross-platform**: iOS and Android support

## Requirements

- Flutter SDK 3.0.0+
- Android 5.0+ / iOS 12.0+
- Google AI API key ([Get one here](https://ai.google.dev/))

## Quick Start

```bash
# Clone repository
git clone https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit

# Install dependencies
flutter pub get

# Run the app
flutter run
```

## Usage

1. Launch the app
2. Enter your Google AI API key in settings
3. Select images from your gallery
4. Choose a creative style or enter custom prompt
5. Tap "Generate" to create AI-transformed image
6. Use "Enhance" for additional detail improvement

## Build

```bash
# Android
flutter build apk

# iOS
flutter build ios
```

## Architecture

- **State Management**: Provider
- **API Integration**: Google Generative AI
- **Image Handling**: image_picker, image packages
- **Storage**: SharedPreferences

## License

For educational and personal use. Please comply with Google's AI API terms of service.

## Support

Create an issue on GitHub for bugs or questions.

---

**Made with Flutter & Gemini AI** ❤️
