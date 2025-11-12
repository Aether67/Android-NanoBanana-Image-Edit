import 'dart:typed_data';

/// Domain model for AI generation results
/// Sealed classes for type-safe result handling
abstract class AIGenerationResult {
  const AIGenerationResult();
}

/// Successful generation with optional image and text
class AIGenerationSuccess extends AIGenerationResult {
  final Uint8List? image;
  final String? text;
  final String? reasoning;

  const AIGenerationSuccess({
    this.image,
    this.text,
    this.reasoning,
  }) : assert(
          image != null || text != null,
          'At least one of image or text must be non-null',
        );
}

/// Generation failed with error message
class AIGenerationError extends AIGenerationResult {
  final String message;
  final dynamic error;

  const AIGenerationError({
    required this.message,
    this.error,
  });
}

/// Generation is in progress
class AIGenerationLoading extends AIGenerationResult {
  final double progress;
  final String message;
  final String? reasoning;

  const AIGenerationLoading({
    this.progress = 0.0,
    this.message = 'Generating...',
    this.reasoning,
  });
}
