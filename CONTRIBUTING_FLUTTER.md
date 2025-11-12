# Contributing to NanoBanana (Flutter)

Thank you for your interest in contributing to NanoBanana! This guide will help you understand our Flutter development process, coding standards, and best practices.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Coding Standards](#coding-standards)
- [Architecture Guidelines](#architecture-guidelines)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Documentation](#documentation)

## Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Help others learn and grow

## Getting Started

### Prerequisites

- **Flutter SDK 3.0.0+** ([Install Flutter](https://flutter.dev/docs/get-started/install))
- **Dart SDK 3.0.0+** (included with Flutter)
- **Android Studio** or **VS Code** with Flutter extensions
- **Xcode** (for iOS development on macOS)
- **Git**

### Fork and Clone

```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/Android-NanoBanana-Image-Edit.git
cd Android-NanoBanana-Image-Edit

# Add upstream remote
git remote add upstream https://github.com/Aether67/Android-NanoBanana-Image-Edit.git
```

## Development Setup

### Install Dependencies

```bash
flutter pub get
```

### Run the App

```bash
# List available devices
flutter devices

# Run on specific device
flutter run -d <device_id>

# Run in release mode
flutter run --release
```

### Hot Reload

While the app is running:
- Press `r` to hot reload
- Press `R` to hot restart
- Press `q` to quit

## Coding Standards

### Dart Style Guide

Follow the official [Dart Style Guide](https://dart.dev/guides/language/effective-dart/style):

#### Naming Conventions

```dart
// Classes: UpperCamelCase
class ImageVariant { }

// Variables, functions: lowerCamelCase
String apiKey = '';
void generateImage() { }

// Constants: lowerCamelCase
const double defaultTemperature = 0.7;

// Private members: prefix with underscore
String _privateField;
void _privateMethod() { }
```

#### Code Organization

```dart
// 1. Imports (sorted alphabetically)
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

// 2. Class declaration
class MyWidget extends StatelessWidget {
  // 3. Fields
  final String title;
  
  // 4. Constructor
  const MyWidget({
    super.key,
    required this.title,
  });
  
  // 5. Overrides
  @override
  Widget build(BuildContext context) {
    return Container();
  }
  
  // 6. Public methods
  void publicMethod() { }
  
  // 7. Private methods
  void _privateMethod() { }
}
```

#### Formatting

```bash
# Format all Dart files
flutter format .

# Format specific file
flutter format lib/main.dart
```

### Widget Best Practices

#### Use const constructors when possible

```dart
// Good
const Text('Hello');
const SizedBox(height: 16);

// Bad (if widget is immutable)
Text('Hello');
SizedBox(height: 16);
```

#### Extract complex widgets

```dart
// Good - extracted widget
class UserProfile extends StatelessWidget {
  const UserProfile({super.key});
  
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Complex UI
      ],
    );
  }
}

// Bad - everything in one widget
class MyScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        // Hundreds of lines of UI
      ],
    );
  }
}
```

#### Prefer composition over inheritance

```dart
// Good
class MyButton extends StatelessWidget {
  const MyButton({super.key});
  
  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: () {},
      child: const Text('Click me'),
    );
  }
}

// Avoid
class MyButton extends ElevatedButton {
  // Don't extend Flutter widgets
}
```

## Architecture Guidelines

### Project Structure

```
lib/
├── main.dart                    # App entry point
├── models/                      # Domain models
│   └── *.dart
├── services/                    # Business logic
│   └── *.dart
├── providers/                   # State management
│   └── *.dart
├── screens/                     # Full-screen views
│   └── *.dart
├── widgets/                     # Reusable components
│   └── *.dart
└── utils/                       # Utilities
    └── *.dart
```

### State Management

We use **Provider** for state management:

```dart
// 1. Define provider
class AppProvider extends ChangeNotifier {
  String _value = '';
  
  String get value => _value;
  
  void updateValue(String newValue) {
    _value = newValue;
    notifyListeners();
  }
}

// 2. Provide at app level
ChangeNotifierProvider(
  create: (_) => AppProvider(),
  child: MyApp(),
)

// 3. Consume in widgets
Consumer<AppProvider>(
  builder: (context, provider, child) {
    return Text(provider.value);
  },
)

// Or use context.watch/read
final value = context.watch<AppProvider>().value;
context.read<AppProvider>().updateValue('new');
```

### Error Handling

```dart
// Use try-catch for async operations
Future<void> loadData() async {
  try {
    final data = await apiService.fetchData();
    // Process data
  } catch (e) {
    // Handle error
    print('Error loading data: $e');
    // Show error to user
  }
}

// Validate inputs
void processInput(String input) {
  if (input.isEmpty) {
    throw ArgumentError('Input cannot be empty');
  }
  // Process input
}
```

## Testing Requirements

### Writing Tests

```bash
# Run all tests
flutter test

# Run specific test file
flutter test test/models_test.dart

# Run with coverage
flutter test --coverage
```

### Test Structure

```dart
import 'package:flutter_test/flutter_test.dart';

void main() {
  group('MyClass', () {
    test('should do something', () {
      // Arrange
      final myClass = MyClass();
      
      // Act
      final result = myClass.doSomething();
      
      // Assert
      expect(result, equals(expectedValue));
    });
    
    test('should handle error', () {
      expect(
        () => myClass.throwError(),
        throwsException,
      );
    });
  });
}
```

### Widget Tests

```dart
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('MyWidget displays text', (tester) async {
    // Build widget
    await tester.pumpWidget(
      const MaterialApp(
        home: MyWidget(),
      ),
    );
    
    // Verify
    expect(find.text('Hello'), findsOneWidget);
    
    // Interact
    await tester.tap(find.byType(ElevatedButton));
    await tester.pump();
    
    // Verify after interaction
    expect(find.text('Clicked'), findsOneWidget);
  });
}
```

### Coverage Requirements

- **Minimum coverage**: 70%
- **Target coverage**: 80%+
- All new features must include tests

## Pull Request Process

### Before Submitting

1. **Run tests**
   ```bash
   flutter test
   ```

2. **Format code**
   ```bash
   flutter format .
   ```

3. **Analyze code**
   ```bash
   flutter analyze
   ```

4. **Update documentation** if needed

### PR Guidelines

1. **Create feature branch**
   ```bash
   git checkout -b feature/my-new-feature
   ```

2. **Make focused commits**
   ```bash
   git commit -m "feat: add image export feature"
   git commit -m "fix: resolve memory leak in image processing"
   ```

3. **Keep PRs small** (< 500 lines changed)

4. **Write clear descriptions**
   - What changes were made
   - Why they were made
   - How to test them

5. **Request review** from maintainers

### Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
type(scope): subject

body

footer
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Formatting
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance

Example:
```
feat(ai): add batch image processing

Implement batch processing for multiple images:
- Process up to 10 images at once
- Show progress for each image
- Handle errors gracefully

Closes #123
```

## Documentation

### Code Documentation

```dart
/// Generates AI content from images and prompt.
///
/// Takes a list of [images] and a [prompt] to generate
/// AI content using the Gemini API.
///
/// Returns a [Future] that completes with the generated
/// content or throws an exception on error.
///
/// Example:
/// ```dart
/// final result = await generateContent(
///   images: [image1, image2],
///   prompt: 'Describe this image',
/// );
/// ```
Future<AIResult> generateContent({
  required List<Uint8List> images,
  required String prompt,
}) async {
  // Implementation
}
```

### README Updates

Update relevant documentation:
- README.md for user-facing changes
- ARCHITECTURE.md for architectural changes
- This file for process changes

## Questions?

- Open an issue for bugs
- Start a discussion for feature ideas
- Check existing issues/PRs first

Thank you for contributing to NanoBanana! 🎨✨
