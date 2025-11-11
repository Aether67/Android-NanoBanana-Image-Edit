package com.yunho.nanobanana.ai

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Advanced Output Validator - Comprehensive quality validation for AI-generated content
 * 
 * Features:
 * - Advanced image artifact detection using edge analysis
 * - Color consistency validation across image regions
 * - Compression artifact detection
 * - Text coherence and consistency validation
 * - Logical contradiction detection in text
 * - Cross-modal consistency checks for combined outputs
 * 
 * Validation results include confidence scores and detailed feedback
 * for regeneration or user alerts.
 */
class OutputValidator {
    
    /**
     * Validates AI-generated content based on output mode
     * 
     * @param image Generated image (optional)
     * @param text Generated text (optional)
     * @param mode Output mode
     * @return ValidationResult with pass/fail and detailed feedback
     */
    fun validate(
        image: Bitmap? = null,
        text: String? = null,
        mode: AIOutputMode
    ): ValidationResult {
        val issues = mutableListOf<ValidationIssue>()
        
        when (mode) {
            AIOutputMode.IMAGE_ONLY -> {
                if (image == null) {
                    return ValidationResult(
                        passed = false,
                        confidence = 0f,
                        issues = listOf(ValidationIssue(
                            severity = IssueSeverity.CRITICAL,
                            type = IssueType.MISSING_CONTENT,
                            message = "No image generated for IMAGE_ONLY mode"
                        ))
                    )
                }
                issues.addAll(validateImage(image))
            }
            
            AIOutputMode.TEXT_ONLY -> {
                if (text == null) {
                    return ValidationResult(
                        passed = false,
                        confidence = 0f,
                        issues = listOf(ValidationIssue(
                            severity = IssueSeverity.CRITICAL,
                            type = IssueType.MISSING_CONTENT,
                            message = "No text generated for TEXT_ONLY mode"
                        ))
                    )
                }
                issues.addAll(validateText(text))
            }
            
            AIOutputMode.COMBINED -> {
                if (image == null && text == null) {
                    return ValidationResult(
                        passed = false,
                        confidence = 0f,
                        issues = listOf(ValidationIssue(
                            severity = IssueSeverity.CRITICAL,
                            type = IssueType.MISSING_CONTENT,
                            message = "No content generated for COMBINED mode"
                        ))
                    )
                }
                
                image?.let { issues.addAll(validateImage(it)) }
                text?.let { issues.addAll(validateText(it)) }
                
                // Cross-modal consistency check
                if (image != null && text != null) {
                    issues.addAll(validateConsistency(image, text))
                }
            }
        }
        
        // Calculate overall confidence
        val confidence = calculateConfidence(issues)
        val passed = issues.none { it.severity == IssueSeverity.CRITICAL }
        
        return ValidationResult(
            passed = passed,
            confidence = confidence,
            issues = issues
        )
    }
    
    /**
     * Validates image quality and detects artifacts
     */
    private fun validateImage(bitmap: Bitmap): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        
        // Basic dimension check
        if (bitmap.width < MIN_IMAGE_WIDTH || bitmap.height < MIN_IMAGE_HEIGHT) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.CRITICAL,
                type = IssueType.IMAGE_QUALITY,
                message = "Image dimensions too small (${bitmap.width}x${bitmap.height})"
            ))
            return issues // Skip further checks if dimensions are invalid
        }
        
        // Check for excessive blank/uniform areas
        val uniformityScore = checkImageUniformity(bitmap)
        if (uniformityScore > MAX_UNIFORMITY_THRESHOLD) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MAJOR,
                type = IssueType.IMAGE_ARTIFACT,
                message = "Image contains excessive uniform areas (score: ${"%.2f".format(uniformityScore)})"
            ))
        }
        
        // Detect compression artifacts
        val compressionArtifacts = detectCompressionArtifacts(bitmap)
        if (compressionArtifacts) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MINOR,
                type = IssueType.IMAGE_ARTIFACT,
                message = "Potential compression artifacts detected"
            ))
        }
        
        // Check color distribution
        val colorIssues = validateColorDistribution(bitmap)
        issues.addAll(colorIssues)
        
        // Edge coherence check
        val edgeScore = analyzeEdgeCoherence(bitmap)
        if (edgeScore < MIN_EDGE_COHERENCE) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MINOR,
                type = IssueType.IMAGE_QUALITY,
                message = "Low edge coherence detected (score: ${"%.2f".format(edgeScore)})"
            ))
        }
        
        Log.d(TAG, "Image validation complete: ${issues.size} issues found")
        return issues
    }
    
    /**
     * Validates text quality and coherence
     */
    private fun validateText(text: String): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val trimmedText = text.trim()
        
        // Length check
        if (trimmedText.length < MIN_TEXT_LENGTH) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.CRITICAL,
                type = IssueType.TEXT_QUALITY,
                message = "Text too short (${trimmedText.length} characters)"
            ))
            return issues
        }
        
        // Check for error messages
        val errorKeywords = listOf("error", "failed", "unable", "cannot", "sorry", "apologize")
        val lowerText = trimmedText.lowercase()
        errorKeywords.forEach { keyword ->
            if (lowerText.startsWith(keyword) || lowerText.contains("$keyword:")) {
                issues.add(ValidationIssue(
                    severity = IssueSeverity.CRITICAL,
                    type = IssueType.TEXT_ERROR,
                    message = "Text appears to be an error message (contains '$keyword')"
                ))
            }
        }
        
        // Check for incomplete sentences
        if (!trimmedText.endsWith(".") && !trimmedText.endsWith("!") && !trimmedText.endsWith("?")) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MINOR,
                type = IssueType.TEXT_QUALITY,
                message = "Text appears incomplete (no ending punctuation)"
            ))
        }
        
        // Detect logical contradictions
        val contradictions = detectContradictions(trimmedText)
        issues.addAll(contradictions)
        
        // Check for repetition
        val repetitionScore = detectRepetition(trimmedText)
        if (repetitionScore > MAX_REPETITION_THRESHOLD) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MAJOR,
                type = IssueType.TEXT_QUALITY,
                message = "Excessive repetition detected (score: ${"%.2f".format(repetitionScore)})"
            ))
        }
        
        // Coherence check
        val coherenceScore = analyzeTextCoherence(trimmedText)
        if (coherenceScore < MIN_TEXT_COHERENCE) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MAJOR,
                type = IssueType.TEXT_QUALITY,
                message = "Low text coherence (score: ${"%.2f".format(coherenceScore)})"
            ))
        }
        
        Log.d(TAG, "Text validation complete: ${issues.size} issues found")
        return issues
    }
    
    /**
     * Validates consistency between image and text in combined mode
     */
    private fun validateConsistency(bitmap: Bitmap, text: String): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        
        // Basic heuristic: check if text mentions visual elements
        val visualKeywords = listOf("image", "picture", "visual", "color", "composition", 
                                     "lighting", "scene", "subject", "background")
        val lowerText = text.lowercase()
        val hasVisualReferences = visualKeywords.any { lowerText.contains(it) }
        
        if (!hasVisualReferences && text.length > 100) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MINOR,
                type = IssueType.CONSISTENCY,
                message = "Text may not reference the generated image"
            ))
        }
        
        return issues
    }
    
    /**
     * Checks for excessive uniform areas in the image
     */
    private fun checkImageUniformity(bitmap: Bitmap): Float {
        val sampleSize = 100 // Sample 100 pixels
        val samplePoints = mutableListOf<Int>()
        
        val stepX = maxOf(bitmap.width / 10, 1)
        val stepY = maxOf(bitmap.height / 10, 1)
        
        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                if (x < bitmap.width && y < bitmap.height) {
                    samplePoints.add(bitmap.getPixel(x, y))
                }
            }
        }
        
        if (samplePoints.isEmpty()) return 0f
        
        // Calculate color variance
        val avgColor = samplePoints.average()
        val variance = samplePoints.map { (it - avgColor) * (it - avgColor) }.average()
        
        // Normalize to 0-1 scale (lower variance = higher uniformity)
        val uniformity = 1.0f - (variance / 1000000f).coerceIn(0f, 1f)
        return uniformity
    }
    
    /**
     * Detects compression artifacts using block detection
     */
    private fun detectCompressionArtifacts(bitmap: Bitmap): Boolean {
        // Simple block artifact detection
        val blockSize = 8
        if (bitmap.width < blockSize * 2 || bitmap.height < blockSize * 2) {
            return false
        }
        
        var blockBoundaries = 0
        val threshold = 30
        
        // Check for 8x8 block boundaries (common JPEG artifact)
        for (x in blockSize until bitmap.width step blockSize) {
            for (y in 0 until bitmap.height - 1) {
                val diff = colorDifference(
                    bitmap.getPixel(x - 1, y),
                    bitmap.getPixel(x, y)
                )
                if (diff > threshold) {
                    blockBoundaries++
                }
            }
        }
        
        // If too many sharp boundaries at block edges, likely compression artifacts
        val artifactThreshold = (bitmap.height / blockSize) * 0.3
        return blockBoundaries > artifactThreshold
    }
    
    /**
     * Validates color distribution in the image
     */
    private fun validateColorDistribution(bitmap: Bitmap): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        
        val colorSamples = mutableListOf<Triple<Int, Int, Int>>()
        val stepX = maxOf(bitmap.width / 20, 1)
        val stepY = maxOf(bitmap.height / 20, 1)
        
        for (x in 0 until bitmap.width step stepX) {
            for (y in 0 until bitmap.height step stepY) {
                if (x < bitmap.width && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    colorSamples.add(Triple(
                        Color.red(pixel),
                        Color.green(pixel),
                        Color.blue(pixel)
                    ))
                }
            }
        }
        
        if (colorSamples.isEmpty()) return issues
        
        // Check for extreme color dominance
        val avgRed = colorSamples.map { it.first }.average()
        val avgGreen = colorSamples.map { it.second }.average()
        val avgBlue = colorSamples.map { it.third }.average()
        
        val maxChannel = maxOf(avgRed, avgGreen, avgBlue)
        val minChannel = minOf(avgRed, avgGreen, avgBlue)
        
        if (maxChannel > 200 && minChannel < 50) {
            issues.add(ValidationIssue(
                severity = IssueSeverity.MINOR,
                type = IssueType.IMAGE_QUALITY,
                message = "Extreme color imbalance detected"
            ))
        }
        
        return issues
    }
    
    /**
     * Analyzes edge coherence in the image
     */
    private fun analyzeEdgeCoherence(bitmap: Bitmap): Float {
        if (bitmap.width < 3 || bitmap.height < 3) return 1f
        
        var edgePixels = 0
        var totalEdges = 0
        val threshold = 40
        
        // Simple Sobel-like edge detection
        for (x in 1 until minOf(bitmap.width - 1, 50)) {
            for (y in 1 until minOf(bitmap.height - 1, 50)) {
                val center = bitmap.getPixel(x, y)
                val right = bitmap.getPixel(x + 1, y)
                val down = bitmap.getPixel(x, y + 1)
                
                val diffX = colorDifference(center, right)
                val diffY = colorDifference(center, down)
                
                totalEdges++
                if (diffX > threshold || diffY > threshold) {
                    edgePixels++
                }
            }
        }
        
        if (totalEdges == 0) return 1f
        
        // Normalize edge density (ideal is around 0.1-0.3)
        val edgeDensity = edgePixels.toFloat() / totalEdges
        return if (edgeDensity < 0.05 || edgeDensity > 0.5) 0.5f else 1f
    }
    
    /**
     * Detects logical contradictions in text
     */
    private fun detectContradictions(text: String): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()
        val lowerText = text.lowercase()
        
        // Simple contradiction patterns
        val contradictionPatterns = listOf(
            Pair("always", "never"),
            Pair("all", "none"),
            Pair("always", "sometimes"),
            Pair("never", "sometimes"),
            Pair("impossible", "possible"),
            Pair("certain", "uncertain")
        )
        
        contradictionPatterns.forEach { (word1, word2) ->
            if (lowerText.contains(word1) && lowerText.contains(word2)) {
                issues.add(ValidationIssue(
                    severity = IssueSeverity.MINOR,
                    type = IssueType.TEXT_CONTRADICTION,
                    message = "Potential contradiction detected: '$word1' and '$word2' both present"
                ))
            }
        }
        
        return issues
    }
    
    /**
     * Detects excessive repetition in text
     */
    private fun detectRepetition(text: String): Float {
        val words = text.lowercase().split("\\s+".toRegex())
        if (words.size < 10) return 0f
        
        val wordCounts = words.groupingBy { it }.eachCount()
        val maxRepetition = wordCounts.values.maxOrNull() ?: 0
        
        // Normalize repetition score
        return (maxRepetition.toFloat() / words.size).coerceIn(0f, 1f)
    }
    
    /**
     * Analyzes text coherence using simple heuristics
     */
    private fun analyzeTextCoherence(text: String): Float {
        val sentences = text.split(Regex("[.!?]")).filter { it.trim().isNotEmpty() }
        if (sentences.isEmpty()) return 0f
        
        // Check average sentence length (too short or too long = lower coherence)
        val avgSentenceLength = sentences.map { it.split("\\s+".toRegex()).size }.average()
        val lengthScore = when {
            avgSentenceLength < 5 -> 0.5f
            avgSentenceLength > 40 -> 0.6f
            else -> 1f
        }
        
        // Check for transition words (indicates better coherence)
        val transitionWords = listOf("however", "therefore", "moreover", "additionally", 
                                      "furthermore", "consequently", "thus", "also")
        val lowerText = text.lowercase()
        val hasTransitions = transitionWords.any { lowerText.contains(it) }
        val transitionScore = if (hasTransitions) 1f else 0.8f
        
        return (lengthScore + transitionScore) / 2f
    }
    
    /**
     * Calculates overall confidence score
     */
    private fun calculateConfidence(issues: List<ValidationIssue>): Float {
        if (issues.isEmpty()) return 1.0f
        
        val totalDeduction = issues.sumOf { issue ->
            when (issue.severity) {
                IssueSeverity.CRITICAL -> 0.5
                IssueSeverity.MAJOR -> 0.2
                IssueSeverity.MINOR -> 0.05
            }
        }
        
        return (1.0f - totalDeduction.toFloat()).coerceIn(0f, 1f)
    }
    
    /**
     * Calculates color difference between two pixels
     */
    private fun colorDifference(color1: Int, color2: Int): Int {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        
        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        
        val rDiff = abs(r1 - r2)
        val gDiff = abs(g1 - g2)
        val bDiff = abs(b1 - b2)
        
        return sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble()).toInt()
    }
    
    companion object {
        private const val TAG = "OutputValidator"
        
        // Image validation thresholds
        private const val MIN_IMAGE_WIDTH = 100
        private const val MIN_IMAGE_HEIGHT = 100
        private const val MAX_UNIFORMITY_THRESHOLD = 0.95f
        private const val MIN_EDGE_COHERENCE = 0.4f
        
        // Text validation thresholds
        private const val MIN_TEXT_LENGTH = 10
        private const val MAX_REPETITION_THRESHOLD = 0.3f
        private const val MIN_TEXT_COHERENCE = 0.5f
    }
}

/**
 * Validation result with confidence and detailed issues
 */
data class ValidationResult(
    val passed: Boolean,
    val confidence: Float,
    val issues: List<ValidationIssue>,
    val shouldRetry: Boolean = !passed && issues.any { it.severity == IssueSeverity.CRITICAL }
) {
    /**
     * Gets user-friendly message summarizing validation result
     */
    fun getUserMessage(): String {
        return when {
            passed && confidence > 0.9f -> "Excellent quality output generated"
            passed && confidence > 0.7f -> "Good quality output with minor imperfections"
            passed -> "Acceptable output with some quality concerns"
            else -> {
                val criticalIssues = issues.filter { it.severity == IssueSeverity.CRITICAL }
                "Quality issues detected: ${criticalIssues.firstOrNull()?.message ?: "Unknown error"}"
            }
        }
    }
    
    /**
     * Gets detailed feedback for logging or debugging
     */
    fun getDetailedFeedback(): String {
        return buildString {
            appendLine("Validation Result: ${if (passed) "PASSED" else "FAILED"}")
            appendLine("Confidence: ${"%.1f".format(confidence * 100)}%")
            if (issues.isNotEmpty()) {
                appendLine("\nIssues (${issues.size}):")
                issues.forEach { issue ->
                    appendLine("  - [${issue.severity}] ${issue.type}: ${issue.message}")
                }
            }
        }
    }
}

/**
 * Individual validation issue
 */
data class ValidationIssue(
    val severity: IssueSeverity,
    val type: IssueType,
    val message: String
)

/**
 * Severity levels for validation issues
 */
enum class IssueSeverity {
    CRITICAL,  // Must retry or alert user
    MAJOR,     // Significant quality concern
    MINOR      // Minor imperfection
}

/**
 * Types of validation issues
 */
enum class IssueType {
    MISSING_CONTENT,
    IMAGE_QUALITY,
    IMAGE_ARTIFACT,
    TEXT_QUALITY,
    TEXT_ERROR,
    TEXT_CONTRADICTION,
    CONSISTENCY
}
