/// AI generation parameters
class AIParameters {
  final double creativityLevel;
  final int detailLevel;
  final int reasoningDepth;
  final AIOutputStyle outputStyle;

  const AIParameters({
    this.creativityLevel = 0.7,
    this.detailLevel = 3,
    this.reasoningDepth = 2,
    this.outputStyle = AIOutputStyle.balanced,
  });

  AIParameters copyWith({
    double? creativityLevel,
    int? detailLevel,
    int? reasoningDepth,
    AIOutputStyle? outputStyle,
  }) {
    return AIParameters(
      creativityLevel: creativityLevel ?? this.creativityLevel,
      detailLevel: detailLevel ?? this.detailLevel,
      reasoningDepth: reasoningDepth ?? this.reasoningDepth,
      outputStyle: outputStyle ?? this.outputStyle,
    );
  }
}

/// AI output modes
enum AIOutputMode {
  imageOnly,
  textOnly,
  combined,
}

/// AI output styles for text generation
enum AIOutputStyle {
  formal('Formal & Professional'),
  casual('Casual & Friendly'),
  technical('Technical & Precise'),
  creative('Creative & Vivid'),
  balanced('Balanced & Clear');

  final String displayName;
  const AIOutputStyle(this.displayName);
}
