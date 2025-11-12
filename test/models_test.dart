import 'package:flutter_test/flutter_test.dart';
import 'package:nanobanana/models/image_variant.dart';
import 'package:nanobanana/models/ai_parameters.dart';
import 'package:nanobanana/models/main_ui_state.dart';
import 'dart:typed_data';

void main() {
  group('ImageVariant Tests', () {
    test('ImageVariant creation with default values', () {
      final variant = ImageVariant(
        image: Uint8List(0),
        prompt: 'Test prompt',
      );

      expect(variant.id, isNotEmpty);
      expect(variant.prompt, equals('Test prompt'));
      expect(variant.isEnhanced, isFalse);
    });

    test('ImageVariant asEnhanced creates enhanced copy', () {
      final variant = ImageVariant(
        image: Uint8List(0),
        prompt: 'Test',
      );

      final enhanced = variant.asEnhanced(enhancementTime: 1000);

      expect(enhanced.isEnhanced, isTrue);
      expect(enhanced.metadata.enhancementTimeMs, equals(1000));
      expect(enhanced.id, equals(variant.id));
    });
  });

  group('VariantCollection Tests', () {
    test('Empty collection', () {
      const collection = VariantCollection();

      expect(collection.isEmpty, isTrue);
      expect(collection.size, equals(0));
      expect(collection.selectedVariant, isNull);
    });

    test('Add variant to collection', () {
      const collection = VariantCollection();
      final variant = ImageVariant(
        image: Uint8List(0),
        prompt: 'Test',
      );

      final newCollection = collection.addVariant(variant);

      expect(newCollection.size, equals(1));
      expect(newCollection.selectedVariantId, equals(variant.id));
      expect(newCollection.selectedVariant, isNotNull);
    });

    test('Select variant', () {
      const collection = VariantCollection();
      final variant1 = ImageVariant(image: Uint8List(0), prompt: 'Test1');
      final variant2 = ImageVariant(image: Uint8List(0), prompt: 'Test2');

      var newCollection = collection.addVariant(variant1).addVariant(variant2);
      newCollection = newCollection.selectVariant(variant2.id);

      expect(newCollection.selectedVariantId, equals(variant2.id));
    });

    test('Remove variant', () {
      const collection = VariantCollection();
      final variant = ImageVariant(image: Uint8List(0), prompt: 'Test');

      var newCollection = collection.addVariant(variant);
      newCollection = newCollection.removeVariant(variant.id);

      expect(newCollection.isEmpty, isTrue);
    });

    test('Clear collection', () {
      const collection = VariantCollection();
      final variant = ImageVariant(image: Uint8List(0), prompt: 'Test');

      final newCollection = collection.addVariant(variant).clear();

      expect(newCollection.isEmpty, isTrue);
      expect(newCollection.selectedVariant, isNull);
    });
  });

  group('AIParameters Tests', () {
    test('Default values', () {
      const params = AIParameters();

      expect(params.creativityLevel, equals(0.7));
      expect(params.detailLevel, equals(3));
      expect(params.reasoningDepth, equals(2));
      expect(params.outputStyle, equals(AIOutputStyle.balanced));
    });

    test('Copy with modifications', () {
      const params = AIParameters();
      final modified = params.copyWith(creativityLevel: 0.9);

      expect(modified.creativityLevel, equals(0.9));
      expect(modified.detailLevel, equals(3));
    });
  });

  group('GenerationState Tests', () {
    test('Idle state', () {
      const state = GenerationStateIdle();
      expect(state, isA<GenerationStateIdle>());
    });

    test('Loading state', () {
      const state = GenerationStateLoading(progress: 0.5, message: 'Processing');
      expect(state.progress, equals(0.5));
      expect(state.message, equals('Processing'));
    });

    test('Success state', () {
      final state = GenerationStateSuccess(
        image: Uint8List(10),
        text: 'Result text',
      );
      expect(state.image, isNotNull);
      expect(state.text, equals('Result text'));
    });

    test('Error state', () {
      const state = GenerationStateError('Error message');
      expect(state.message, equals('Error message'));
    });
  });

  group('MainUiState Tests', () {
    test('Default state', () {
      const state = MainUiState();

      expect(state.apiKey, isEmpty);
      expect(state.selectedImages, isEmpty);
      expect(state.currentPrompt, isEmpty);
      expect(state.generationState, isA<GenerationStateIdle>());
    });

    test('Copy with modifications', () {
      const state = MainUiState();
      final modified = state.copyWith(
        apiKey: 'test-key',
        currentPrompt: 'Test prompt',
      );

      expect(modified.apiKey, equals('test-key'));
      expect(modified.currentPrompt, equals('Test prompt'));
      expect(modified.selectedImages, isEmpty);
    });
  });

  group('CreativeStyles Tests', () {
    test('Has correct number of styles', () {
      expect(CreativeStyles.styles.length, equals(5));
    });

    test('Get prompt template', () {
      final template = CreativeStyles.getPromptTemplate(0);
      expect(template, contains('Korean Historical'));
    });

    test('Custom prompt returns empty', () {
      final template = CreativeStyles.getPromptTemplate(4);
      expect(template, isEmpty);
    });
  });
}
