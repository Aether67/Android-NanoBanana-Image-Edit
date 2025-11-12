import 'dart:typed_data';
import 'package:flutter/foundation.dart';
import 'package:image_picker/image_picker.dart';
import '../models/main_ui_state.dart';
import '../models/ai_parameters.dart';
import '../models/image_variant.dart';
import '../services/gemini_ai_service.dart';
import '../services/settings_service.dart';
import 'package:image/image.dart' as img;

/// Main app provider for state management
class AppProvider extends ChangeNotifier {
  MainUiState _state = const MainUiState();
  late SettingsService _settingsService;
  GeminiAIService? _aiService;
  final ImagePicker _imagePicker = ImagePicker();

  MainUiState get state => _state;

  AppProvider() {
    _initializeSettings();
  }

  Future<void> _initializeSettings() async {
    _settingsService = await SettingsService.init();
    final apiKey = _settingsService.getApiKey();
    _state = _state.copyWith(apiKey: apiKey);
    if (apiKey.isNotEmpty) {
      _aiService = GeminiAIService(apiKey: apiKey);
    }
    notifyListeners();
  }

  /// Save API key
  Future<void> saveApiKey(String apiKey) async {
    await _settingsService.saveApiKey(apiKey);
    _state = _state.copyWith(apiKey: apiKey);
    _aiService = GeminiAIService(apiKey: apiKey);
    notifyListeners();
  }

  /// Update prompt
  void updatePrompt(String prompt) {
    _state = _state.copyWith(currentPrompt: prompt);
    notifyListeners();
  }

  /// Update style index
  void updateStyleIndex(int index) {
    _state = _state.copyWith(selectedStyleIndex: index);
    if (index < 4) {
      final template = CreativeStyles.getPromptTemplate(index);
      _state = _state.copyWith(currentPrompt: template);
    }
    notifyListeners();
  }

  /// Pick images from gallery
  Future<void> pickImages() async {
    try {
      final List<XFile> images = await _imagePicker.pickMultiImage();
      if (images.isEmpty) return;

      final List<Uint8List> imageBytes = [];
      for (var image in images) {
        final bytes = await image.readAsBytes();
        imageBytes.add(bytes);
      }

      _state = _state.copyWith(selectedImages: imageBytes);
      notifyListeners();
    } catch (e) {
      print('Error picking images: $e');
    }
  }

  /// Add selected images
  void addSelectedImages(List<Uint8List> images) {
    final currentImages = List<Uint8List>.from(_state.selectedImages);
    currentImages.addAll(images);
    _state = _state.copyWith(selectedImages: currentImages);
    notifyListeners();
  }

  /// Remove selected image
  void removeSelectedImage(int index) {
    final images = List<Uint8List>.from(_state.selectedImages);
    images.removeAt(index);
    _state = _state.copyWith(selectedImages: images);
    notifyListeners();
  }

  /// Clear selected images
  void clearSelectedImages() {
    _state = _state.copyWith(selectedImages: []);
    notifyListeners();
  }

  /// Generate AI content
  Future<void> generateContent(String? customPrompt) async {
    if (_aiService == null) {
      _state = _state.copyWith(
        generationState: const GenerationStateError('Please set API key first'),
      );
      notifyListeners();
      return;
    }

    if (_state.selectedImages.isEmpty) {
      _state = _state.copyWith(
        generationState: const GenerationStateError('Please select at least one image'),
      );
      notifyListeners();
      return;
    }

    final prompt = customPrompt ?? _state.currentPrompt;
    if (prompt.isEmpty) {
      _state = _state.copyWith(
        generationState: const GenerationStateError('Please enter a prompt'),
      );
      notifyListeners();
      return;
    }

    try {
      _state = _state.copyWith(
        generationState: const GenerationStateLoading(
          progress: 0.0,
          message: 'Generating...',
        ),
      );
      notifyListeners();

      final result = await _aiService!.generateCombined(
        prompt: prompt,
        images: _state.selectedImages,
        temperature: _state.aiParameters.creativityLevel,
      );

      if (result.$1 != null || result.$2 != null) {
        _state = _state.copyWith(
          generationState: GenerationStateSuccess(
            image: result.$1,
            text: result.$2,
          ),
        );
        
        // Add to variants if we have an image
        if (result.$1 != null) {
          final variant = ImageVariant(
            image: result.$1!,
            prompt: prompt,
          );
          _state = _state.copyWith(
            variants: _state.variants.addVariant(variant),
          );
        }
      } else {
        _state = _state.copyWith(
          generationState: const GenerationStateError('No content generated'),
        );
      }
    } catch (e) {
      _state = _state.copyWith(
        generationState: GenerationStateError('Error: ${e.toString()}'),
      );
    }

    notifyListeners();
  }

  /// Enhance image
  Future<void> enhanceImage({String? region}) async {
    if (_aiService == null) {
      return;
    }

    final currentState = _state.generationState;
    Uint8List? imageToEnhance;

    if (currentState is GenerationStateSuccess && currentState.image != null) {
      imageToEnhance = currentState.image;
    } else if (_state.variants.selectedVariant != null) {
      imageToEnhance = _state.variants.selectedVariant!.image;
    }

    if (imageToEnhance == null) {
      return;
    }

    try {
      final startTime = DateTime.now();
      final enhanced = await _aiService!.enhanceImage(
        image: imageToEnhance,
        region: region,
      );

      if (enhanced != null) {
        final processingTime = DateTime.now().difference(startTime).inMilliseconds;
        _state = _state.copyWith(
          enhancementState: EnhancementResult(
            enhancedImage: enhanced,
            message: 'Enhancement complete',
            processingTimeMs: processingTime,
          ),
          generationState: GenerationStateSuccess(
            image: enhanced,
            text: (currentState is GenerationStateSuccess) ? currentState.text : null,
          ),
        );
        notifyListeners();
      }
    } catch (e) {
      print('Error enhancing image: $e');
    }
  }

  /// Save as variant
  void saveAsVariant() {
    final currentState = _state.generationState;
    if (currentState is GenerationStateSuccess && currentState.image != null) {
      final variant = ImageVariant(
        image: currentState.image!,
        prompt: _state.currentPrompt,
      );
      _state = _state.copyWith(
        variants: _state.variants.addVariant(variant),
      );
      notifyListeners();
    }
  }

  /// Select variant
  void selectVariant(String id) {
    _state = _state.copyWith(
      variants: _state.variants.selectVariant(id),
    );
    notifyListeners();
  }

  /// Delete variant
  void deleteVariant(String id) {
    _state = _state.copyWith(
      variants: _state.variants.removeVariant(id),
    );
    notifyListeners();
  }

  /// Reset to idle state
  void resetToIdle() {
    _state = _state.copyWith(
      generationState: const GenerationStateIdle(),
      clearEnhancementState: true,
    );
    notifyListeners();
  }

  @override
  void dispose() {
    _aiService?.dispose();
    super.dispose();
  }
}
