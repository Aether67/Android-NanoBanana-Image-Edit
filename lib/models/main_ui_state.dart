import 'dart:typed_data';
import 'ai_parameters.dart';
import 'image_variant.dart';

/// UI state for the main screen
/// Immutable data class representing the complete UI state
class MainUiState {
  final String apiKey;
  final List<Uint8List> selectedImages;
  final String currentPrompt;
  final int selectedStyleIndex;
  final AIParameters aiParameters;
  final AIOutputMode outputMode;
  final GenerationState generationState;
  final EnhancementResult? enhancementState;
  final VariantCollection variants;

  const MainUiState({
    this.apiKey = '',
    this.selectedImages = const [],
    this.currentPrompt = '',
    this.selectedStyleIndex = 0,
    AIParameters? aiParameters,
    this.outputMode = AIOutputMode.combined,
    this.generationState = const GenerationStateIdle(),
    this.enhancementState,
    this.variants = const VariantCollection(),
  }) : aiParameters = aiParameters ?? const AIParameters();

  MainUiState copyWith({
    String? apiKey,
    List<Uint8List>? selectedImages,
    String? currentPrompt,
    int? selectedStyleIndex,
    AIParameters? aiParameters,
    AIOutputMode? outputMode,
    GenerationState? generationState,
    EnhancementResult? enhancementState,
    bool clearEnhancementState = false,
    VariantCollection? variants,
  }) {
    return MainUiState(
      apiKey: apiKey ?? this.apiKey,
      selectedImages: selectedImages ?? this.selectedImages,
      currentPrompt: currentPrompt ?? this.currentPrompt,
      selectedStyleIndex: selectedStyleIndex ?? this.selectedStyleIndex,
      aiParameters: aiParameters ?? this.aiParameters,
      outputMode: outputMode ?? this.outputMode,
      generationState: generationState ?? this.generationState,
      enhancementState:
          clearEnhancementState ? null : (enhancementState ?? this.enhancementState),
      variants: variants ?? this.variants,
    );
  }
}

/// State of AI generation process
abstract class GenerationState {
  const GenerationState();
}

/// No generation in progress
class GenerationStateIdle extends GenerationState {
  const GenerationStateIdle();
}

/// Generation in progress
class GenerationStateLoading extends GenerationState {
  final double progress;
  final String message;

  const GenerationStateLoading({
    this.progress = 0.0,
    this.message = 'Generating...',
  });
}

/// Generation completed successfully
class GenerationStateSuccess extends GenerationState {
  final Uint8List? image;
  final String? text;
  final String? reasoning;

  const GenerationStateSuccess({
    this.image,
    this.text,
    this.reasoning,
  });
}

/// Generation failed
class GenerationStateError extends GenerationState {
  final String message;

  const GenerationStateError(this.message);
}

/// Enhancement result
class EnhancementResult {
  final Uint8List enhancedImage;
  final String message;
  final int processingTimeMs;

  const EnhancementResult({
    required this.enhancedImage,
    this.message = '',
    this.processingTimeMs = 0,
  });
}

/// Predefined creative styles
class CreativeStyles {
  static const List<String> styles = [
    'Korean Historical (Chosun Dynasty 1900s)',
    'Collectible Figure (Hyper-realistic with packaging)',
    'Rock-Paper-Scissors (Interactive game scenarios)',
    'Shopping Scene (3D Costco style)',
    'Custom Prompt',
  ];

  static String getPromptTemplate(int index) {
    switch (index) {
      case 0:
        return 'Transform this into a Korean historical scene from the Chosun Dynasty (1900s era) with traditional clothing, architecture, and atmosphere.';
      case 1:
        return 'Create a hyper-realistic collectible figure version with detailed packaging design, product photography lighting, and premium display presentation.';
      case 2:
        return 'Transform into an interactive rock-paper-scissors game scene with playful, engaging composition and clear hand gesture representations.';
      case 3:
        return 'Create a 3D Costco-style shopping scene with warehouse atmosphere, product displays, bulk shopping carts, and retail environment.';
      default:
        return '';
    }
  }
}
