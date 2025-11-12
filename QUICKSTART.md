# Quick Start Guide

Get NanoBanana running in 5 minutes!

## Prerequisites

```bash
flutter --version  # Check Flutter is installed
flutter devices    # Check connected devices
```

## Setup

### 1. Clone & Install
```bash
git clone https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit
flutter pub get
```

### 2. Get API Key
1. Visit [Google AI Studio](https://ai.google.dev/)
2. Create a project and generate an API key
3. Copy the key

### 3. Run
```bash
flutter run
```

### 4. Configure
1. App will prompt for API key
2. Paste your key and save
3. Start using the app! 🎉

## Common Commands

```bash
# Run with specific device
flutter run -d <device_id>

# Build for production
flutter build apk        # Android
flutter build ios        # iOS

# Run tests
flutter test

# Format code
flutter format .
```

## Troubleshooting

**Flutter not found?**
```bash
export PATH="$PATH:/path/to/flutter/bin"
flutter doctor
```

**No devices?**
- Android: Enable USB debugging or start emulator
- iOS: Start iOS simulator (macOS only)

**Build failed?**
```bash
flutter clean
flutter pub get
flutter run
```

## Next Steps

- Read [README.md](README.md) for features
- Check [CHANGELOG.md](CHANGELOG.md) for version history

Happy coding! 🚀
