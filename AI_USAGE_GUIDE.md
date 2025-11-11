# AI Capabilities Usage Guide

## Overview

This guide provides comprehensive instructions for using NanoBanana's advanced AI capabilities powered by Google's Gemini 2.5 model. The system offers intelligent image editing, text generation, and reasoning with automatic quality validation.

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [AI Output Modes](#ai-output-modes)
3. [Configuring AI Parameters](#configuring-ai-parameters)
4. [Using Prompt Templates](#using-prompt-templates)
5. [Understanding Validation Feedback](#understanding-validation-feedback)
6. [Real-time Reasoning](#real-time-reasoning)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Getting Started

### Quick Start

1. **Set API Key**: Configure your Google AI API key in settings
2. **Select Images**: Choose one or more images to edit
3. **Choose Mode**: Select IMAGE_ONLY, TEXT_ONLY, or COMBINED
4. **Customize Parameters**: Adjust creativity, detail, and reasoning depth
5. **Generate**: Tap the generate button and watch AI work
6. **Review Results**: Check validation feedback and output quality

### Basic Workflow

```
Select Images → Configure AI Parameters → Enter Prompt → Generate → Review Validation → Save Result
```

---

## AI Output Modes

### IMAGE_ONLY Mode

**Use When:**
- You want only the transformed image
- Text analysis is not needed
- Quick image generation is the priority

**Example Prompts:**
```
"Transform to Korean 1900s style"
"Create a hyper-realistic collectible figure"
"Apply watercolor painting effect"
"Enhance image quality and colors"
```

**Validation Focus:**
- Image dimensions and quality
- Artifact detection
- Color consistency
- Edge coherence

### TEXT_ONLY Mode

**Use When:**
- You need image analysis without generation
- Seeking creative suggestions
- Requesting technical feedback
- Learning about composition

**Example Prompts:**
```
"Analyze the composition and lighting"
"Suggest creative enhancements"
"Explain the artistic choices"
"Provide technical quality assessment"
```

**Validation Focus:**
- Text length and coherence
- Error message detection
- Logical consistency
- Completeness

### COMBINED Mode

**Use When:**
- You want both image and explanation
- Learning while creating
- Documenting transformations
- Understanding AI decisions

**Example Prompts:**
```
"Create a cyberpunk cityscape and explain the design"
"Transform to impressionist style with analysis"
"Enhance this photo and describe improvements"
"Apply vintage effect and explain technique"
```

**Validation Focus:**
- Both image and text quality
- Cross-modal consistency
- Alignment between visual and textual content

---

## Configuring AI Parameters

### Creativity Level (0.0 - 1.0)

**Low Creativity (0.0 - 0.3)**
- Focused, deterministic results
- Minimal variation between generations
- Best for: Technical accuracy, consistent style

**Medium Creativity (0.4 - 0.6)**
- Balanced approach
- Reliable with some variation
- Best for: General use, controlled creativity

**High Creativity (0.7 - 0.9)** ⭐ Default
- Creative, varied outputs
- More artistic interpretation
- Best for: Artistic work, unique results

**Maximum Creativity (0.9 - 1.0)**
- Unpredictable, highly creative
- Significant variation
- Best for: Experimental art, brainstorming

### Detail Level (1-5)

**Level 1 - Minimal**
- Simple, minimalist output
- 1-2 paragraphs of text
- Basic image quality
- Fast generation

**Level 2 - Moderate**
- Clean, well-composed output
- 2-3 paragraphs of text
- Good image quality

**Level 3 - Comprehensive** ⭐ Default
- Detailed, high-quality output
- 3-5 paragraphs of text
- Professional image quality

**Level 4 - Extensive**
- Highly detailed, professional-grade
- 5-7 paragraphs with examples
- Premium image quality

**Level 5 - Exhaustive**
- Hyper-realistic, extremely detailed
- 7+ paragraphs, multi-perspective
- Masterpiece-quality images

### Reasoning Depth (1-3)

**Level 1 - Brief**
- Simple explanations
- Key points only
- Quick to generate

**Level 2 - Detailed** ⭐ Default
- Step-by-step reasoning
- Justifications included
- Good balance

**Level 3 - In-Depth**
- Comprehensive analysis
- Multiple hypotheses
- Alternative perspectives
- Predictive insights

### Output Style

**Formal & Professional**
- Academic or business tone
- Precise terminology
- Structured arguments
- Example: "The composition demonstrates adherence to the rule of thirds..."

**Casual & Friendly**
- Conversational language
- Accessible explanations
- Relatable examples
- Example: "You'll notice how the colors really pop here..."

**Technical & Precise**
- Specific measurements
- Technical terminology
- Analytical approach
- Example: "The RGB values indicate a cool color palette with..."

**Creative & Vivid**
- Descriptive language
- Emotional resonance
- Artistic metaphors
- Example: "The image bursts with energy, as vibrant hues dance across..."

**Balanced & Clear** ⭐ Default
- Mix of precision and accessibility
- Clear structure
- Moderate detail

---

## Using Prompt Templates

### Available Templates

#### 1. IMAGE_EXPLANATION
**Purpose**: Comprehensive image analysis

**Best For**: Understanding visual elements, composition, artistic choices

**Example Usage**:
```kotlin
promptManager.applyTemplate(PromptTemplate.IMAGE_EXPLANATION)
```

#### 2. IMAGE_TRANSFORMATION_GUIDE
**Purpose**: Step-by-step transformation instructions

**Best For**: Learning techniques, replicating effects

**Example Usage**:
```kotlin
promptManager.applyTemplate(
    PromptTemplate.IMAGE_TRANSFORMATION_GUIDE,
    customInput = "vintage film look"
)
```

#### 3. CREATIVE_SUGGESTION
**Purpose**: Creative enhancement ideas

**Best For**: Brainstorming, exploring possibilities

#### 4. TECHNICAL_ANALYSIS
**Purpose**: Technical evaluation of image quality

**Best For**: Quality assessment, identifying issues

#### 5. ARTISTIC_COMMENTARY
**Purpose**: Artistic interpretation and discussion

**Best For**: Aesthetic analysis, style understanding

#### 6. QUALITY_IMPROVEMENT ⭐ NEW
**Purpose**: Detect and fix quality issues

**Best For**: Artifact removal, enhancement suggestions

**Example Output**:
```
Detected Issues:
- Compression artifacts in gradient areas
- Color banding in sky region
- Edge haloing from over-sharpening

Applied Enhancements:
- Removed JPEG block artifacts
- Smoothed color gradients
- Adjusted sharpening parameters
```

#### 7. STYLE_TRANSFER ⭐ NEW
**Purpose**: Transform to different artistic styles

**Best For**: Style exploration, artistic effects

**Example Usage**:
```kotlin
promptManager.applyTemplate(
    PromptTemplate.STYLE_TRANSFER,
    customInput = "impressionist painting"
)
```

#### 8. IMAGE_ENHANCEMENT ⭐ NEW
**Purpose**: General image improvement

**Best For**: Quick enhancements, overall quality boost

#### 9. COMPARATIVE_ANALYSIS ⭐ NEW
**Purpose**: Before/after comparison

**Best For**: Understanding changes, documenting improvements

---

## Understanding Validation Feedback

### Confidence Scores

**Excellent (90-100%)**
- 🟢 Green indicator
- High-quality output
- Ready to use immediately

**Good (70-89%)**
- 🟡 Light green indicator
- Minor imperfections acceptable
- Good for most use cases

**Fair (50-69%)**
- 🟠 Orange indicator
- Some quality concerns
- Review issues before using

**Low (0-49%)**
- 🔴 Red indicator
- Significant quality issues
- Consider regenerating

### Issue Severity Levels

**🔴 CRITICAL**
- Must address or regenerate
- Example: Missing content, error messages
- System automatically retries

**🟡 MAJOR**
- Significant quality concern
- Example: Low coherence, excessive repetition
- Review before accepting

**🔵 MINOR**
- Minor imperfection
- Example: Incomplete sentence, slight uniformity
- Usually acceptable

### Common Validation Issues

#### Image Issues

**Excessive Uniform Areas**
- Cause: Large blank or solid-color regions
- Impact: May indicate incomplete generation
- Solution: Regenerate or adjust prompt

**Compression Artifacts**
- Cause: JPEG block artifacts (8x8 blocks)
- Impact: Reduced visual quality
- Solution: Use QUALITY_IMPROVEMENT template

**Color Imbalance**
- Cause: Extreme dominance of one color channel
- Impact: Unnatural appearance
- Solution: Adjust creativity level, regenerate

**Low Edge Coherence**
- Cause: Inconsistent or chaotic edges
- Impact: Reduced visual clarity
- Solution: Increase detail level

#### Text Issues

**Too Short**
- Cause: Insufficient detail level
- Solution: Increase detail level to 3+

**Error Messages**
- Cause: AI encountered processing issue
- Solution: Automatic retry triggered

**Logical Contradictions**
- Cause: Inconsistent reasoning
- Impact: Reduced reliability
- Solution: Adjust reasoning depth

**Excessive Repetition**
- Cause: Over-emphasis on keywords
- Impact: Reduced readability
- Solution: Rephrase prompt, adjust creativity

#### Cross-Modal Issues

**No Visual References**
- Cause: Text doesn't describe image
- Impact: Reduced coherence in COMBINED mode
- Solution: Emphasize description in prompt

---

## Real-time Reasoning

### Understanding AI Reasoning Display

**Stage Indicator**
- Shows current processing phase
- Examples: "Analyzing Image Generation Request", "Generating Insightful Analysis"

**Reasoning Steps**
- Numbered list of AI's approach
- Reveals decision-making process
- Educational insight into AI workflow

**Current Thought**
- AI's immediate focus
- Real-time decision explanation
- Builds transparency

**Suggestions**
- Proactive guidance from AI
- Best practices recommendations
- Quality improvement tips

**Confidence Level**
- AI's self-assessment
- Reliability indicator
- Typically 80-95% for good outputs

### Interpreting Reasoning Feedback

**High Confidence (>85%)**
- AI is certain about approach
- Expect high-quality results
- Trust the output

**Medium Confidence (60-85%)**
- AI is proceeding cautiously
- Results likely good but may vary
- Review output carefully

**Low Confidence (<60%)**
- AI uncertain about best approach
- Consider adjusting parameters
- May need regeneration

---

## Best Practices

### Prompt Engineering

**Be Specific**
```
❌ Bad: "Make it better"
✅ Good: "Enhance colors and add dramatic lighting"
```

**Use Descriptive Language**
```
❌ Bad: "Change style"
✅ Good: "Transform to impressionist painting with visible brushstrokes and broken color"
```

**Provide Context**
```
❌ Bad: "Fix it"
✅ Good: "Remove compression artifacts and enhance sharpness while maintaining natural appearance"
```

**Leverage Templates**
```
✅ Use templates for common tasks
✅ Customize with specific inputs
✅ Combine templates for complex workflows
```

### Parameter Optimization

**For Image Transformation:**
- Creativity: 0.6 - 0.8
- Detail Level: 3 - 4
- Mode: IMAGE_ONLY or COMBINED

**For Text Analysis:**
- Creativity: 0.4 - 0.6
- Detail Level: 3 - 4
- Reasoning Depth: 2 - 3
- Mode: TEXT_ONLY

**For Creative Generation:**
- Creativity: 0.7 - 0.9
- Detail Level: 4 - 5
- Reasoning Depth: 2
- Mode: COMBINED

### Quality Optimization

1. **Start with defaults** - They work well for most cases
2. **Adjust gradually** - Small parameter changes have big effects
3. **Review validation** - Pay attention to confidence scores
4. **Use templates** - Pre-optimized for specific tasks
5. **Iterate** - Refine prompts based on results

---

## Troubleshooting

### Image Quality Issues

**Problem**: Blurry or low-resolution output

**Solutions**:
- Increase detail level to 4-5
- Add "high-resolution" to prompt
- Use IMAGE_ENHANCEMENT template
- Check input image quality

**Problem**: Unwanted artifacts

**Solutions**:
- Use QUALITY_IMPROVEMENT template
- Lower creativity level
- Add "artifact-free" to prompt
- Enable automatic validation retry

**Problem**: Colors look unnatural

**Solutions**:
- Add "natural colors" to prompt
- Adjust creativity level to 0.6-0.7
- Use TECHNICAL_ANALYSIS to identify issues
- Regenerate with color-specific instructions

### Text Quality Issues

**Problem**: Text too brief

**Solutions**:
- Increase detail level to 3+
- Set reasoning depth to 2-3
- Add "provide detailed explanation" to prompt
- Use appropriate template

**Problem**: Text doesn't match image

**Solutions**:
- Use COMBINED mode explicitly
- Add "describe the generated image" to prompt
- Increase reasoning depth
- Check cross-modal consistency in validation

**Problem**: Repetitive or incoherent text

**Solutions**:
- Adjust creativity level
- Rephrase prompt more clearly
- Increase reasoning depth
- Use more formal output style

### Generation Failures

**Problem**: Generation fails repeatedly

**Solutions**:
- Check API key validity
- Simplify prompt
- Reduce image count
- Lower detail level temporarily
- Check internet connection
- Review validation feedback for specific issues

**Problem**: Slow generation

**Solutions**:
- Reduce detail level
- Use IMAGE_ONLY mode if text not needed
- Lower resolution of input images
- Check device performance tier
- Enable graceful degradation

---

## Advanced Features

### Context-Aware Generation

The system maintains conversation context for coherent multi-turn interactions:

```kotlin
// First generation
"Create a sunset cityscape"

// Second generation (uses context)
"Add flying vehicles to the scene"

// Third generation (builds on previous)
"Enhance the lighting effects"
```

**Context is preserved** for the last 5 interactions and automatically integrated into prompts.

### Adaptive Processing

The system automatically adapts to device capabilities:

**High-End Devices:**
- Parallel image and text generation
- Full caching enabled
- Maximum concurrent operations
- Premium quality settings

**Mid-Range Devices:**
- Balanced processing
- Moderate caching
- Reasonable concurrency
- Good quality settings

**Low-End Devices:**
- Sequential processing
- Minimal caching
- Limited concurrency
- Optimized quality settings

### Telemetry & Monitoring

Track performance metrics:

```kotlin
val telemetryReport = repository.getTelemetryReport()
Log.d(TAG, "Average latency: ${telemetryReport.averageLatency}ms")
Log.d(TAG, "Success rate: ${telemetryReport.successRate}%")
Log.d(TAG, "Cache hit rate: ${telemetryReport.cacheHitRate}%")
```

---

## API Reference

### PromptManager

```kotlin
// Create instance
val promptManager = PromptManager(context)

// Configure parameters
promptManager.creativityLevel = 0.7f
promptManager.detailLevel = 3
promptManager.reasoningDepth = 2
promptManager.outputStyle = AIOutputStyle.BALANCED
promptManager.outputMode = AIOutputMode.COMBINED

// Generate prompts
val prompt = promptManager.generatePrompt(
    basePrompt = "Transform to vintage style",
    mode = AIOutputMode.COMBINED,
    includeReasoning = true
)

// Use templates
val templatePrompt = promptManager.applyTemplate(
    template = PromptTemplate.STYLE_TRANSFER,
    customInput = "impressionist painting"
)

// Save settings
promptManager.saveSettings()

// Clear context
promptManager.clearContext()
```

### OutputValidator

```kotlin
// Create instance
val validator = OutputValidator()

// Validate output
val result = validator.validate(
    image = generatedBitmap,
    text = generatedText,
    mode = AIOutputMode.COMBINED
)

// Check result
if (result.passed) {
    println("Validation passed!")
    println("Confidence: ${result.confidence}")
} else {
    println("Issues detected:")
    result.issues.forEach { issue ->
        println("- [${issue.severity}] ${issue.message}")
    }
}

// Get user message
val message = result.getUserMessage()
showToast(message)

// Get detailed feedback
val feedback = result.getDetailedFeedback()
Log.d(TAG, feedback)
```

---

## Conclusion

NanoBanana's AI capabilities provide a powerful, flexible system for image editing and analysis. By understanding the modes, parameters, templates, and validation system, you can achieve professional-quality results with intelligent assistance.

**Key Takeaways:**
- ✅ Use appropriate mode for your task
- ✅ Start with default parameters and adjust as needed
- ✅ Leverage templates for common workflows
- ✅ Pay attention to validation feedback
- ✅ Review reasoning to understand AI decisions
- ✅ Iterate and refine for best results

For detailed technical documentation, see [AI_PROMPT_ENGINEERING.md](AI_PROMPT_ENGINEERING.md).

For architecture details, see [ARCHITECTURE.md](ARCHITECTURE.md) and [ASYNC_ARCHITECTURE.md](ASYNC_ARCHITECTURE.md).

---

**Document Version**: 1.0  
**Last Updated**: 2025-11-11  
**Maintained By**: NanoBanana Development Team
