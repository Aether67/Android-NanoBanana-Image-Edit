import 'dart:typed_data';
import 'package:uuid/uuid.dart';
import 'ai_parameters.dart';

const _uuid = Uuid();

/// Represents a variant of a generated or enhanced image
/// Enables side-by-side comparison and selection of different AI-generated versions
class ImageVariant {
  final String id;
  final Uint8List image;
  final String prompt;
  final int timestamp;
  final bool isEnhanced;
  final VariantMetadata metadata;

  ImageVariant({
    String? id,
    required this.image,
    required this.prompt,
    int? timestamp,
    this.isEnhanced = false,
    VariantMetadata? metadata,
  })  : id = id ?? _uuid.v4(),
        timestamp = timestamp ?? DateTime.now().millisecondsSinceEpoch,
        metadata = metadata ?? VariantMetadata();

  /// Creates a copy with enhanced flag set
  ImageVariant asEnhanced({int enhancementTime = 0}) {
    return ImageVariant(
      id: id,
      image: image,
      prompt: prompt,
      timestamp: timestamp,
      isEnhanced: true,
      metadata: metadata.copyWith(enhancementTimeMs: enhancementTime),
    );
  }

  ImageVariant copyWith({
    String? id,
    Uint8List? image,
    String? prompt,
    int? timestamp,
    bool? isEnhanced,
    VariantMetadata? metadata,
  }) {
    return ImageVariant(
      id: id ?? this.id,
      image: image ?? this.image,
      prompt: prompt ?? this.prompt,
      timestamp: timestamp ?? this.timestamp,
      isEnhanced: isEnhanced ?? this.isEnhanced,
      metadata: metadata ?? this.metadata,
    );
  }
}

/// Metadata for a variant including generation and enhancement details
class VariantMetadata {
  final int generationTimeMs;
  final int enhancementTimeMs;
  final String style;
  final AIParameters parameters;

  VariantMetadata({
    this.generationTimeMs = 0,
    this.enhancementTimeMs = 0,
    this.style = '',
    AIParameters? parameters,
  }) : parameters = parameters ?? AIParameters();

  VariantMetadata copyWith({
    int? generationTimeMs,
    int? enhancementTimeMs,
    String? style,
    AIParameters? parameters,
  }) {
    return VariantMetadata(
      generationTimeMs: generationTimeMs ?? this.generationTimeMs,
      enhancementTimeMs: enhancementTimeMs ?? this.enhancementTimeMs,
      style: style ?? this.style,
      parameters: parameters ?? this.parameters,
    );
  }
}

/// Collection of variants for comparison
/// Provides immutable operations for managing variants
class VariantCollection {
  final List<ImageVariant> variants;
  final String? selectedVariantId;

  const VariantCollection({
    this.variants = const [],
    this.selectedVariantId,
  });

  /// Gets the currently selected variant
  ImageVariant? get selectedVariant {
    if (selectedVariantId == null) return null;
    try {
      return variants.firstWhere((v) => v.id == selectedVariantId);
    } catch (e) {
      return null;
    }
  }

  /// Selects a variant by ID
  VariantCollection selectVariant(String id) {
    if (variants.any((v) => v.id == id)) {
      return VariantCollection(
        variants: variants,
        selectedVariantId: id,
      );
    }
    return this;
  }

  /// Adds a new variant to the collection
  /// Automatically selects it if it's the first variant
  VariantCollection addVariant(ImageVariant variant) {
    final newVariants = [...variants, variant];
    final newSelectedId = selectedVariantId ?? variant.id;
    return VariantCollection(
      variants: newVariants,
      selectedVariantId: newSelectedId,
    );
  }

  /// Removes a variant by ID
  /// If the removed variant was selected, selects the first remaining variant
  VariantCollection removeVariant(String id) {
    final newVariants = variants.where((v) => v.id != id).toList();
    String? newSelectedId;
    
    if (newVariants.isEmpty) {
      newSelectedId = null;
    } else if (selectedVariantId == id) {
      newSelectedId = newVariants.isNotEmpty ? newVariants.first.id : null;
    } else {
      newSelectedId = selectedVariantId;
    }
    
    return VariantCollection(
      variants: newVariants,
      selectedVariantId: newSelectedId,
    );
  }

  /// Clears all variants
  VariantCollection clear() {
    return const VariantCollection();
  }

  /// Returns the number of variants
  int get size => variants.length;

  /// Returns whether the collection is empty
  bool get isEmpty => variants.isEmpty;
  
  bool get isNotEmpty => variants.isNotEmpty;
}
