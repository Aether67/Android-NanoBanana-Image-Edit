# AI Prompt Engineering & Reasoning Workflows

## Overview

This document provides comprehensive documentation of the AI prompt engineering logic, reasoning workflows, and usage guidelines for the NanoBanana AI Image Editor's enhanced capabilities.

## Table of Contents

1. [Prompt Management System](#prompt-management-system)
2. [AI Output Modes](#ai-output-modes)
3. [User-Configurable Parameters](#user-configurable-parameters)
4. [Reasoning Workflows](#reasoning-workflows)
5. [Quality Validation](#quality-validation)
6. [Example Prompts & Expected Outputs](#example-prompts--expected-outputs)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Prompt Management System

### Architecture

The `PromptManager` class provides a flexible, context-aware system for generating optimized prompts based on user intent and configuration.

#### Key Components:

- **Context Tracking**: Maintains conversation history for coherent multi-turn interactions
- **Template System**: Predefined templates for common use cases
- **Dynamic Enhancement**: Automatically adds quality directives and contextual information
- **Parameter Integration**: Incorporates user preferences into prompt generation

### Prompt Construction Process

```
User Base Prompt
    ↓
Mode-Specific Instructions (Image/Text/Combined)
    ↓
Quality Directives
    ↓
Contextual Information
    ↓
Enhanced Prompt → Gemini API
```

---

## AI Output Modes

### 1. Image-Only Mode

**Purpose**: Generate high-quality images without text analysis

**Prompt Enhancement**:
```kotlin
"[Base Prompt] Generate a [quality-level] image with proper resolution and clarity. 
Ensure artifact-free output with natural colors and lighting."
```

**Quality Levels** (based on Detail Level parameter):
- Level 1: "simple, minimalist"
- Level 2: "clean, well-composed"
- Level 3: "detailed, high-quality" (default)
- Level 4: "highly detailed, professional-grade"
- Level 5: "hyper-realistic, extremely detailed, masterpiece-quality"

**Example**:
```
Input: "Transform to Korean 1900s style"
Enhanced: "Transform to Korean 1900s style Generate a detailed, high-quality 
image with proper resolution and clarity. Ensure artifact-free output with 
natural colors and lighting."
```

### 2. Text-Only Mode

**Purpose**: Generate insightful analysis, reasoning, or commentary without images

**Prompt Enhancement**:
```kotlin
"[Style Prefix] [Base Prompt]

Provide [reasoning-depth] of your response.
[Detail-level directive]"
```

**Style Prefixes**:
- **Formal**: "In a formal, professional tone:"
- **Casual**: "In a friendly, conversational tone:"
- **Technical**: "With technical precision and detail:"
- **Creative**: "With creative flair and vivid language:"
- **Balanced**: No prefix (default)

**Reasoning Depth Levels**:
- Level 1: "a brief explanation"
- Level 2: "a detailed explanation with reasoning" (default)
- Level 3: "an in-depth analysis with step-by-step reasoning and hypotheses"

**Example**:
```
Input: "Explain the artistic choices in this portrait"
Style: Technical
Detail Level: 4
Reasoning Depth: 3

Enhanced: "With technical precision and detail: Explain the artistic choices 
in this portrait

Provide an in-depth analysis with step-by-step reasoning and hypotheses of 
your response. Provide extensive detail and examples."
```

### 3. Combined Mode

**Purpose**: Generate both image and comprehensive text analysis

**Prompt Structure**:
```
Task: [Base Prompt]

Image Generation:
[Image prompt with quality directives]

Text Analysis:
Provide [style-specific] analysis of the generated image, including:
- What changes were made and why
- Key visual elements and their significance
- Artistic or technical considerations
- Reasoning behind creative decisions (if reasoning enabled)
- Hypotheses about viewer perception (if reasoning enabled)
```

**Example**:
```
Input: "Create a cyberpunk city scene"
Style: Creative
Detail Level: 4
Reasoning Depth: 2

Enhanced: "Task: Create a cyberpunk city scene

Image Generation:
Create a cyberpunk city scene Generate a highly detailed, professional-grade 
image with proper resolution and clarity. Ensure artifact-free output with 
natural colors and lighting.

Text Analysis:
Provide an insightful commentary of the generated image, including:
- What changes were made and why
- Key visual elements and their significance
- Artistic or technical considerations
- Reasoning behind creative decisions
- Hypotheses about viewer perception"
```

---

## User-Configurable Parameters

### 1. Creativity Level (0.0 - 1.0)

**Technical Impact**: Maps directly to Gemini API's `temperature` parameter

**User Impact**:
- **0.0 - 0.3**: Very focused, deterministic outputs
- **0.4 - 0.6**: Balanced creativity and consistency
- **0.7 - 0.9**: High creativity, more variation (default: 0.7)
- **0.9 - 1.0**: Maximum creativity, unpredictable results

**Recommendation**: 
- Image transformation: 0.6 - 0.8
- Text analysis: 0.4 - 0.6
- Creative generation: 0.7 - 0.9

### 2. Detail Level (1-5)

**Impact on Image Generation**:
- Determines quality modifiers in prompts
- Affects expected output complexity

**Impact on Text Generation**:
- Level 1: Concise summaries (1-2 paragraphs)
- Level 2: Moderate detail (2-3 paragraphs)
- Level 3: Comprehensive (3-5 paragraphs) - default
- Level 4: Extensive with examples (5-7 paragraphs)
- Level 5: Exhaustive multi-perspective analysis (7+ paragraphs)

### 3. Reasoning Depth (1-3)

**Level 1 - Brief**:
- Simple explanations
- Key points only
- Minimal justification

**Level 2 - Detailed** (default):
- Step-by-step reasoning
- Justifications for decisions
- Connections between concepts

**Level 3 - In-Depth**:
- Comprehensive analysis
- Multiple hypotheses
- Alternative perspectives
- Predictive insights

### 4. Output Style

**Formal & Professional**:
- Academic or business tone
- Precise terminology
- Structured arguments
- Example: "The composition demonstrates adherence to the rule of thirds..."

**Casual & Friendly**:
- Conversational language
- Accessible explanations
- Relatable examples
- Example: "You'll notice how the colors really pop here..."

**Technical & Precise**:
- Specific measurements
- Technical terminology
- Analytical approach
- Example: "The RGB values indicate a cool color palette with..."

**Creative & Vivid**:
- Descriptive language
- Emotional resonance
- Artistic metaphors
- Example: "The image bursts with energy, as vibrant hues dance across..."

**Balanced & Clear** (default):
- Mix of precision and accessibility
- Clear structure
- Moderate detail
- Example: "This image effectively uses lighting to highlight the subject..."

---

## Reasoning Workflows

### Image Generation Reasoning

**Stages**:
1. **Analysis**: "Analyzing prompt requirements and creative intent"
2. **Planning**: "Determining optimal composition and visual elements"
3. **Strategy**: "Planning color palette and lighting strategy"
4. **Execution**: "Generating high-quality image with artifact prevention"

**Real-time Feedback Components**:
- Current processing stage
- Step-by-step reasoning
- Hypotheses about outcomes
- Suggestions for optimization
- Confidence level (0-100%)

**Example Reasoning Output**:
```
Stage: Analyzing Image Generation Request
Steps:
1. Analyzing prompt requirements and creative intent
2. Determining optimal composition and visual elements
3. Planning color palette and lighting strategy
4. Generating high-quality image with artifact prevention

Current Thought: "Creating a balanced composition that aligns with your 
vision while maintaining visual coherence and quality."

Suggestions:
- Consider the emotional impact of chosen colors
- Ensure visual hierarchy guides viewer attention
- Maintain consistency with the requested style

Confidence: 85%
```

### Text Generation Reasoning

**Stages**:
1. **Understanding**: "Understanding the core concepts and context"
2. **Analysis**: "Identifying key points and relationships"
3. **Structure**: "Structuring coherent and logical explanation"
4. **Refinement**: "Refining language for clarity and engagement"

**Example Reasoning Output**:
```
Stage: Generating Insightful Analysis
Steps:
1. Understanding the core concepts and context
2. Identifying key points and relationships
3. Structuring coherent and logical explanation
4. Refining language for clarity and engagement

Current Thought: "Crafting a response that balances technical accuracy 
with accessibility and engagement."

Confidence: 90%
```

### Combined Mode Reasoning

**Stages**:
1. **Multi-modal Analysis**: "Analyzing prompt for image and text requirements"
2. **Alignment**: "Ensuring coherent alignment between visual and textual elements"
3. **Synthesis**: "Generating synchronized outputs with consistent messaging"
4. **Validation**: "Validating quality and contextual coherence"

**Example Reasoning Output**:
```
Stage: Coordinating Image and Text Generation
Steps:
1. Analyzing prompt for image and text requirements
2. Ensuring coherent alignment between visual and textual elements
3. Generating synchronized outputs with consistent messaging
4. Validating quality and contextual coherence

Current Thought: "Ensuring that both image and text outputs tell a 
unified, compelling story."

Suggestions:
- Visual elements should reinforce textual explanations
- Text should provide context that enhances image understanding
- Maintain consistent tone and style across both outputs

Confidence: 80%
```

---

## Quality Validation

### Automatic Validation System

The system performs automatic validation on all AI-generated outputs to ensure quality and trigger re-generation when needed.

#### Image Validation

**Basic Checks**:
- Minimum dimensions: 100x100 pixels
- Valid bitmap data
- Non-zero byte count

**Future Enhancements** (planned):
- Artifact detection using edge analysis
- Color consistency validation
- Composition quality scoring
- Compression artifact detection

#### Text Validation

**Current Checks**:
1. **Minimum Length**: At least 10 characters
2. **Error Detection**: Filters responses starting with error keywords
3. **Coherence**: Ensures response is not just error messages

**Validation Logic**:
```kotlin
fun isTextValid(text: String): Boolean {
    val trimmedText = text.trim()
    
    // Must have reasonable length
    if (trimmedText.length < 10) return false
    
    // Should not be just error messages
    val errorKeywords = listOf("error", "failed", "unable", "cannot")
    val lowerText = trimmedText.lowercase()
    val hasErrors = errorKeywords.any { keyword ->
        lowerText.startsWith(keyword) || lowerText.contains("$keyword:")
    }
    
    return !hasErrors
}
```

#### Retry Mechanism

**Configuration**:
- Maximum attempts: 3
- Exponential backoff: 2s, 4s, 6s delays
- Validation between attempts

**Process**:
```
Attempt 1
    ↓
Validation Failed?
    ↓
Wait 2 seconds → Attempt 2
    ↓
Validation Failed?
    ↓
Wait 4 seconds → Attempt 3
    ↓
Success or Final Error
```

---

## Example Prompts & Expected Outputs

### Example 1: Image Transformation

**User Input**:
```
"Transform this photo into a watercolor painting"
```

**Configuration**:
- Mode: IMAGE_ONLY
- Creativity: 0.7
- Detail Level: 4

**Enhanced Prompt**:
```
Transform this photo into a watercolor painting Generate a highly detailed, 
professional-grade image with proper resolution and clarity. Ensure artifact-free 
output with natural colors and lighting.

Ensure high-resolution output without artifacts, compression issues, or visual anomalies.
```

**Expected Output**:
- High-quality watercolor-style image
- Preserved subject composition
- Artistic brush stroke effects
- Natural color transitions

---

### Example 2: Image Analysis

**User Input**:
```
"Analyze the composition and lighting in this photograph"
```

**Configuration**:
- Mode: TEXT_ONLY
- Style: TECHNICAL
- Detail Level: 4
- Reasoning Depth: 3

**Enhanced Prompt**:
```
With technical precision and detail: Analyze the composition and lighting in this photograph

Provide an in-depth analysis with step-by-step reasoning and hypotheses of your response. 
Provide extensive detail and examples.

Ensure coherent, well-structured text without logical inconsistencies or contradictions.
```

**Expected Output**:
```
Technical Analysis of Composition and Lighting

Composition Analysis:
The photograph employs a classic rule of thirds composition, with the main subject 
positioned at the intersection of the upper-right grid lines. This placement creates 
visual tension and guides the viewer's eye naturally through the frame.

Leading Lines:
The architectural elements create strong diagonal lines that converge toward the 
subject, establishing depth and directing attention. These lines follow a perspective 
that suggests approximately 28mm focal length equivalent.

Lighting Breakdown:
The scene is illuminated by a key light source positioned at approximately 45 degrees 
from camera-right. The color temperature appears to be around 5500K (daylight), 
creating natural skin tones and neutral shadows.

Shadow Analysis:
Shadows exhibit soft edges with a falloff gradient suggesting diffused lighting, 
likely from overcast conditions or a large light modifier. The shadow-to-highlight 
ratio measures approximately 1:3, indicating well-controlled contrast.

Reasoning:
The photographer likely chose this composition to balance the subject with negative 
space, creating breathing room that prevents claustrophobia while maintaining focus. 
The lighting approach suggests a desire for natural, documentary-style authenticity 
rather than dramatic artistic effect.

Hypotheses:
Based on the lighting quality and composition choices, this appears to be shot during 
golden hour (approximately one hour before sunset), which would explain the warm 
undertones and soft shadow quality. The viewer's perception would likely register 
this as a calm, contemplative moment due to the balanced composition and gentle lighting.
```

---

### Example 3: Combined Generation

**User Input**:
```
"Create a futuristic city skyline at sunset"
```

**Configuration**:
- Mode: COMBINED
- Style: CREATIVE
- Creativity: 0.8
- Detail Level: 3
- Reasoning Depth: 2

**Enhanced Prompt**:
```
Task: Create a futuristic city skyline at sunset

Image Generation:
Create a futuristic city skyline at sunset Generate a detailed, high-quality image 
with proper resolution and clarity. Ensure artifact-free output with natural colors 
and lighting.

Text Analysis:
Provide an insightful commentary of the generated image, including:
- What changes were made and why
- Key visual elements and their significance
- Artistic or technical considerations
- Reasoning behind creative decisions
- Hypotheses about viewer perception

Ensure both image and text outputs are of high quality, with coherent alignment 
between visual and textual content.
```

**Expected Output**:

*Image*: A stunning futuristic cityscape with sleek skyscrapers, flying vehicles, 
holographic billboards, bathed in warm sunset colors transitioning from orange to purple.

*Text*:
```
Visual Narrative: A Sunset Over Tomorrow's Metropolis

The generated image captures a breathtaking vision of urban evolution, where 
architectural ambition meets natural beauty. The skyline bristles with crystalline 
towers that seem to pierce the heavens, their surfaces reflecting the dying light 
in a symphony of amber and rose.

Key Visual Elements:
The composition centers on a cluster of ultra-modern skyscrapers whose organic, 
flowing forms suggest bio-inspired design principles. These structures incorporate 
vertical gardens and transparent surfaces, hinting at sustainable technology 
integrated into urban planning.

Creative Decisions:
The sunset palette was chosen to create emotional warmth and nostalgia, contrasting 
with the cold precision of futuristic technology. This juxtaposition speaks to 
humanity's enduring connection to natural rhythms even in highly technological 
environments. The flying vehicles trace elegant arcs through the sky, their paths 
illuminated to guide the eye and suggest constant motion and vitality.

Artistic Considerations:
The color grading emphasizes the golden hour's magic while maintaining the crisp, 
clean aesthetic expected of future architecture. Volumetric lighting creates god 
rays piercing through the urban canyon, adding depth and atmospheric perspective.

Viewer Experience:
This scene likely evokes wonder mixed with contemplation—viewers may feel inspired 
by technological progress while simultaneously appreciating timeless natural beauty. 
The image suggests optimism about humanity's future while respecting the enduring 
power of nature's daily spectacles.
```

---

### Example 4: Using Prompt Templates

**Template**: IMAGE_TRANSFORMATION_GUIDE

**User Input**: Apply template to "vintage film look"

**Generated Prompt**:
```
Explain how to transform this image to achieve a vintage film look, including 
step-by-step reasoning and expected outcomes.
```

**Configuration**:
- Mode: TEXT_ONLY
- Style: BALANCED
- Detail Level: 3
- Reasoning Depth: 2

**Expected Output**:
```
Step-by-Step Guide: Achieving a Vintage Film Look

Step 1: Color Grading Foundation
Begin by reducing overall saturation by approximately 20-30%. Vintage film stocks 
had limited color reproduction compared to digital sensors, resulting in more 
muted, organic color palettes.

Step 2: Grain Introduction
Add film grain texture with a particle size of 2-3 pixels. This simulates the 
silver halide crystals in analog film. The grain should be more prominent in 
shadow areas, as this is where film grain was most visible.

Step 3: Color Shift Application
Apply a subtle warm shift to highlights (add 5-10% yellow/orange) and cool shift 
to shadows (add 5-10% cyan/blue). This mimics the cross-processing effects common 
in vintage film processing.

Step 4: Contrast Adjustment
Reduce overall contrast by lifting blacks (increase shadow values by 10-15%) and 
slightly lowering highlights. Vintage film had a narrower dynamic range than modern 
digital sensors.

Step 5: Vignetting
Add subtle corner darkening (10-20% reduction in luminance at edges) to simulate 
natural lens vignetting common in older camera systems.

Expected Outcome:
The resulting image will exhibit the nostalgic character of film photography—
softer contrasts, organic grain structure, and gentle color shifts that evoke 
memories and emotion. The reduced saturation and adjusted tonal range will create 
a timeless quality that feels both authentic and artistically intentional.

Reasoning:
Each step builds upon film photography's inherent characteristics and limitations, 
which paradoxically became its aesthetic strengths. By understanding why these 
artifacts occurred in analog processes, we can recreate them intentionally with 
greater authenticity and artistic control.
```

---

## Best Practices

### For Users

1. **Be Specific**: Detailed prompts yield better results
   - Bad: "Make it better"
   - Good: "Enhance colors and add dramatic lighting"

2. **Use Appropriate Modes**:
   - Image-only: When you only need visual output
   - Text-only: For analysis without generation
   - Combined: When you want both explained output

3. **Adjust Parameters Thoughtfully**:
   - Start with defaults
   - Increase creativity for artistic work
   - Increase detail level for complex requests
   - Adjust reasoning depth based on need for explanation

4. **Leverage Templates**:
   - Use built-in templates for common tasks
   - Templates provide optimized prompts

5. **Review Reasoning Feedback**:
   - Check AI's reasoning to understand its approach
   - Use suggestions to refine your prompts

### For Developers

1. **Context Management**:
   - Clear context when switching topics
   - Keep conversation context relevant
   - Limit context history to 5 recent interactions

2. **Quality Validation**:
   - Always validate outputs before accepting
   - Implement retry logic for failed validations
   - Log validation failures for improvement

3. **Parameter Tuning**:
   - Test different creativity levels for use cases
   - Monitor quality across detail levels
   - Validate reasoning depth effectiveness

4. **Error Handling**:
   - Provide clear error messages
   - Implement graceful degradation
   - Log errors for debugging

---

## Troubleshooting

### Common Issues

#### Issue: Generated images have artifacts

**Possible Causes**:
- Creativity level too high
- Insufficient detail level
- Complex prompt conflicting instructions

**Solutions**:
- Reduce creativity to 0.6-0.7
- Increase detail level to 4-5
- Simplify prompt or break into steps
- Check validation is enabled

#### Issue: Text output is too brief

**Possible Causes**:
- Detail level too low
- Reasoning depth set to brief
- Prompt doesn't encourage elaboration

**Solutions**:
- Increase detail level to 3+
- Set reasoning depth to detailed or in-depth
- Add specific questions to prompt
- Use "explain in detail" phrasing

#### Issue: Output doesn't match style

**Possible Causes**:
- Wrong output style selected
- Style not emphasized in base prompt
- Creativity level too low for creative styles

**Solutions**:
- Verify selected output style
- Increase creativity for creative/casual styles
- Add style keywords to base prompt
- Use style-specific templates

#### Issue: Validation keeps failing

**Possible Causes**:
- Impossible or contradictory prompt
- API issues or rate limiting
- Validation criteria too strict

**Solutions**:
- Simplify prompt
- Check API key and connectivity
- Review error logs for patterns
- Adjust validation thresholds if needed

#### Issue: No reasoning feedback shown

**Possible Causes**:
- Reasoning component not enabled
- Processing too fast to display
- Component visibility settings

**Solutions**:
- Enable reasoning feedback in UI
- Check component rendering logic
- Verify isActive parameter is true

---

## API Integration Notes

### Gemini 2.0 Flash API

**Endpoint**: 
```
https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent
```

**Key Parameters**:
- `temperature`: Controls creativity (0.0-1.0)
- `topK`: Limits vocabulary consideration (default: 32)
- `topP`: Nucleus sampling threshold (default: 1.0)
- `candidateCount`: Number of responses (use 1 for consistency)

**Request Structure**:
```json
{
  "contents": [{
    "parts": [
      {"text": "enhanced_prompt"},
      {"inline_data": {"mime_type": "image/jpeg", "data": "base64_image"}}
    ]
  }],
  "generationConfig": {
    "temperature": 0.7,
    "topK": 32,
    "topP": 1.0,
    "candidateCount": 1
  }
}
```

**Response Structure**:
```json
{
  "candidates": [{
    "content": {
      "parts": [
        {"text": "generated_text"},
        {"inlineData": {"mimeType": "image/jpeg", "data": "base64_image"}}
      ]
    }
  }]
}
```

---

## Future Enhancements

### Planned Features

1. **Advanced Artifact Detection**:
   - Edge analysis for image quality
   - Compression artifact detection
   - Color consistency validation

2. **Learning System**:
   - Track successful prompts
   - Suggest improvements based on history
   - Personalized prompt recommendations

3. **Batch Processing**:
   - Apply same prompt to multiple images
   - Consistent style across batch
   - Parallel processing optimization

4. **Custom Templates**:
   - User-defined template creation
   - Template sharing and import
   - Template version control

5. **Advanced Reasoning**:
   - Multi-step reasoning chains
   - Alternative hypothesis generation
   - Confidence calibration

---

## Conclusion

The NanoBanana AI Image Editor's enhanced prompt management and reasoning system provides a powerful, flexible foundation for creative image manipulation and analysis. By understanding the prompt engineering principles, reasoning workflows, and quality validation mechanisms documented here, users and developers can maximize the effectiveness of AI-generated outputs while maintaining high quality standards.

For additional support or questions, please refer to the main README.md or open an issue on the project repository.

---

**Document Version**: 1.0  
**Last Updated**: 2025-11-11  
**Maintained By**: NanoBanana Development Team
