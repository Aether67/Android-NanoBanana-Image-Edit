import 'dart:typed_data';
import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:image/image.dart' as img;

/// AI Data Source for Gemini API
/// Handles all HTTP communication with the AI service
class GeminiAIService {
  final String apiKey;
  final http.Client _client;

  static const String _baseUrl = 'https://generativelanguage.googleapis.com/v1beta';
  static const String _model = 'gemini-2.0-flash-exp';
  static const int _timeout = 60;
  static const int _maxRetries = 3;

  GeminiAIService({
    required this.apiKey,
    http.Client? client,
  }) : _client = client ?? http.Client();

  /// Generate image from prompt and input images
  Future<Uint8List?> generateImage({
    required String prompt,
    required List<Uint8List> images,
    double temperature = 0.7,
  }) async {
    final enhancedPrompt =
        '$prompt. Generate a high-quality, artifact-free image with proper resolution and clarity.';
    final result = await _performGenerationWithRetry(
      prompt: enhancedPrompt,
      images: images,
      temperature: temperature,
      extractImage: true,
    );
    return result.$1;
  }

  /// Generate text from prompt and input images
  Future<String?> generateText({
    required String prompt,
    required List<Uint8List> images,
    double temperature = 0.7,
  }) async {
    final enhancedPrompt = '$prompt. Provide a detailed explanation with reasoning.';
    final result = await _performGenerationWithRetry(
      prompt: enhancedPrompt,
      images: images,
      temperature: temperature,
      extractImage: false,
    );
    return result.$2;
  }

  /// Generate both image and text
  Future<(Uint8List?, String?)> generateCombined({
    required String prompt,
    required List<Uint8List> images,
    double temperature = 0.7,
  }) async {
    final enhancedPrompt = '''
Task: $prompt

Image Generation:
Generate a high-quality, artifact-free image with proper resolution and clarity.

Text Analysis:
Provide a clear explanation of the generated image, including:
- What changes were made and why
- Key visual elements and their significance
- Artistic or technical considerations
''';

    return await _performGenerationWithRetry(
      prompt: enhancedPrompt,
      images: images,
      temperature: temperature,
      extractImage: true,
    );
  }

  /// Enhance image quality
  Future<Uint8List?> enhanceImage({
    required Uint8List image,
    String? region,
  }) async {
    final prompt = region != null
        ? 'Enhance the quality and detail of this image in the region: $region. Focus on sharpening details, improving clarity, and reducing artifacts.'
        : 'Enhance the overall quality and detail of this image. Focus on sharpening details, improving clarity, and reducing artifacts while maintaining natural appearance.';

    final result = await _performGenerationWithRetry(
      prompt: prompt,
      images: [image],
      temperature: 0.3, // Lower temperature for enhancement
      extractImage: true,
    );
    return result.$1;
  }

  /// Perform generation with retry mechanism
  Future<(Uint8List?, String?)> _performGenerationWithRetry({
    required String prompt,
    required List<Uint8List> images,
    required double temperature,
    required bool extractImage,
  }) async {
    int attempt = 0;

    while (attempt < _maxRetries) {
      try {
        final result = await _performGeneration(
          prompt: prompt,
          images: images,
          temperature: temperature,
          extractImage: extractImage,
        );

        if (result.$1 != null || result.$2 != null) {
          return result;
        }
      } catch (e) {
        print('Attempt ${attempt + 1} failed: $e');
      }

      attempt++;
      if (attempt < _maxRetries) {
        await Future.delayed(Duration(seconds: attempt * 2));
      }
    }

    throw Exception('Failed to generate content after $_maxRetries attempts');
  }

  /// Perform the actual API call
  Future<(Uint8List?, String?)> _performGeneration({
    required String prompt,
    required List<Uint8List> images,
    required double temperature,
    required bool extractImage,
  }) async {
    // Build request body
    final parts = <Map<String, dynamic>>[];
    
    // Add text prompt
    parts.add({'text': prompt});
    
    // Add images as inline data
    for (var imageBytes in images) {
      final base64Image = base64Encode(imageBytes);
      parts.add({
        'inline_data': {
          'mime_type': 'image/jpeg',
          'data': base64Image,
        }
      });
    }

    final requestBody = {
      'contents': [
        {'parts': parts}
      ],
      'generationConfig': {
        'temperature': temperature,
        'topP': 0.95,
        'topK': 40,
        'maxOutputTokens': 8192,
      }
    };

    // Make API request
    final url = '$_baseUrl/models/$_model:generateContent?key=$apiKey';
    final response = await _client
        .post(
          Uri.parse(url),
          headers: {'Content-Type': 'application/json'},
          body: jsonEncode(requestBody),
        )
        .timeout(Duration(seconds: _timeout));

    if (response.statusCode != 200) {
      throw Exception('API request failed: ${response.statusCode} - ${response.body}');
    }

    // Parse response
    final jsonResponse = jsonDecode(response.body) as Map<String, dynamic>;
    
    Uint8List? resultImage;
    String? resultText;

    if (jsonResponse.containsKey('candidates')) {
      final candidates = jsonResponse['candidates'] as List;
      if (candidates.isNotEmpty) {
        final candidate = candidates[0] as Map<String, dynamic>;
        if (candidate.containsKey('content')) {
          final content = candidate['content'] as Map<String, dynamic>;
          if (content.containsKey('parts')) {
            final responseParts = content['parts'] as List;
            
            for (var part in responseParts) {
              final partMap = part as Map<String, dynamic>;
              
              // Extract text
              if (partMap.containsKey('text')) {
                resultText = partMap['text'] as String;
              }
              
              // Extract image if requested
              if (extractImage && partMap.containsKey('inline_data')) {
                final inlineData = partMap['inline_data'] as Map<String, dynamic>;
                if (inlineData.containsKey('data')) {
                  final imageData = inlineData['data'] as String;
                  resultImage = base64Decode(imageData);
                }
              }
            }
          }
        }
      }
    }

    return (resultImage, resultText);
  }

  void dispose() {
    _client.close();
  }
}
