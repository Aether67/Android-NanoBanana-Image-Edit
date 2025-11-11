package com.yunho.nanobanana.ai

import android.graphics.Bitmap
import android.graphics.Color
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for OutputValidator
 * 
 * Tests comprehensive validation logic for AI-generated content including:
 * - Image artifact detection
 * - Text coherence validation
 * - Cross-modal consistency checks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class OutputValidatorTest {
    
    private lateinit var validator: OutputValidator
    
    @Before
    fun setup() {
        validator = OutputValidator()
    }
    
    // ===== IMAGE VALIDATION TESTS =====
    
    @Test
    fun `validate image-only mode with valid image passes`() {
        val bitmap = createMockBitmap(500, 500)
        
        val result = validator.validate(
            image = bitmap,
            text = null,
            mode = AIOutputMode.IMAGE_ONLY
        )
        
        assertTrue("Should pass with valid image", result.passed)
        assertTrue("Confidence should be high", result.confidence > 0.7f)
        assertFalse("Should not require retry", result.shouldRetry)
    }
    
    @Test
    fun `validate image-only mode with no image fails critically`() {
        val result = validator.validate(
            image = null,
            text = null,
            mode = AIOutputMode.IMAGE_ONLY
        )
        
        assertFalse("Should fail without image", result.passed)
        assertEquals("Confidence should be zero", 0f, result.confidence, 0.01f)
        assertTrue("Should require retry", result.shouldRetry)
        assertTrue("Should have critical issue", 
            result.issues.any { it.severity == IssueSeverity.CRITICAL }
        )
    }
    
    @Test
    fun `validate image with too small dimensions fails`() {
        val bitmap = createMockBitmap(50, 50)
        
        val result = validator.validate(
            image = bitmap,
            text = null,
            mode = AIOutputMode.IMAGE_ONLY
        )
        
        assertFalse("Should fail with small image", result.passed)
        assertTrue("Should have critical issue about dimensions",
            result.issues.any { it.message.contains("too small") }
        )
    }
    
    // ===== TEXT VALIDATION TESTS =====
    
    @Test
    fun `validate text-only mode with valid text passes`() {
        val validText = """
            This is a comprehensive analysis of the image. 
            The composition demonstrates strong visual hierarchy.
            The color palette is well-balanced and harmonious.
            Overall, the image effectively communicates its intended message.
        """.trimIndent()
        
        val result = validator.validate(
            image = null,
            text = validText,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        assertTrue("Should pass with valid text", result.passed)
        assertTrue("Confidence should be high", result.confidence > 0.7f)
    }
    
    @Test
    fun `validate text-only mode with no text fails critically`() {
        val result = validator.validate(
            image = null,
            text = null,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        assertFalse("Should fail without text", result.passed)
        assertEquals("Confidence should be zero", 0f, result.confidence, 0.01f)
        assertTrue("Should require retry", result.shouldRetry)
    }
    
    @Test
    fun `validate detects error messages in text`() {
        val errorText = "Error: Unable to process the request"
        
        val result = validator.validate(
            image = null,
            text = errorText,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        assertFalse("Should fail with error message", result.passed)
        assertTrue("Should have critical text error",
            result.issues.any { 
                it.type == IssueType.TEXT_ERROR && 
                it.severity == IssueSeverity.CRITICAL 
            }
        )
    }
    
    @Test
    fun `validate detects text that is too short`() {
        val shortText = "Too short"
        
        val result = validator.validate(
            image = null,
            text = shortText,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        assertFalse("Should fail with short text", result.passed)
        assertTrue("Should have critical issue about length",
            result.issues.any { it.message.contains("too short") }
        )
    }
    
    @Test
    fun `validate detects incomplete sentences`() {
        val incompleteText = "This is a comprehensive analysis without proper ending"
        
        val result = validator.validate(
            image = null,
            text = incompleteText,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        // Should still pass but with minor issue
        assertTrue("Should pass despite incomplete sentence", result.passed)
        assertTrue("Should have minor issue about punctuation",
            result.issues.any { 
                it.severity == IssueSeverity.MINOR && 
                it.message.contains("incomplete") 
            }
        )
    }
    
    @Test
    fun `validate detects potential contradictions`() {
        val contradictoryText = """
            This approach always works perfectly.
            However, it never produces the desired results.
        """.trimIndent()
        
        val result = validator.validate(
            image = null,
            text = contradictoryText,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        // Should detect contradiction but still pass (minor issue)
        assertTrue("Should pass despite contradiction", result.passed)
        assertTrue("Should have contradiction issue",
            result.issues.any { it.type == IssueType.TEXT_CONTRADICTION }
        )
    }
    
    @Test
    fun `validate detects excessive repetition`() {
        val repetitiveText = """
            Image image image image image image.
            The image shows image details with image quality.
            Image processing creates image results.
        """.trimIndent()
        
        val result = validator.validate(
            image = null,
            text = repetitiveText,
            mode = AIOutputMode.TEXT_ONLY
        )
        
        // Should detect repetition
        assertTrue("Should have issue about repetition",
            result.issues.any { it.message.contains("repetition") }
        )
    }
    
    // ===== COMBINED MODE TESTS =====
    
    @Test
    fun `validate combined mode with both image and text passes`() {
        val bitmap = createMockBitmap(500, 500)
        val text = """
            The generated image demonstrates excellent composition.
            The color palette is harmonious and well-balanced.
            The lighting creates depth and visual interest.
        """.trimIndent()
        
        val result = validator.validate(
            image = bitmap,
            text = text,
            mode = AIOutputMode.COMBINED
        )
        
        assertTrue("Should pass with both outputs", result.passed)
        assertTrue("Confidence should be high", result.confidence > 0.7f)
    }
    
    @Test
    fun `validate combined mode with only image passes`() {
        val bitmap = createMockBitmap(500, 500)
        
        val result = validator.validate(
            image = bitmap,
            text = null,
            mode = AIOutputMode.COMBINED
        )
        
        assertTrue("Should pass with just image", result.passed)
    }
    
    @Test
    fun `validate combined mode with only text passes`() {
        val text = """
            Comprehensive analysis of the visual elements.
            The composition follows traditional design principles.
        """.trimIndent()
        
        val result = validator.validate(
            image = null,
            text = text,
            mode = AIOutputMode.COMBINED
        )
        
        assertTrue("Should pass with just text", result.passed)
    }
    
    @Test
    fun `validate combined mode with neither fails critically`() {
        val result = validator.validate(
            image = null,
            text = null,
            mode = AIOutputMode.COMBINED
        )
        
        assertFalse("Should fail without any content", result.passed)
        assertTrue("Should require retry", result.shouldRetry)
    }
    
    @Test
    fun `validate detects text without visual references in combined mode`() {
        val bitmap = createMockBitmap(500, 500)
        val text = """
            This is a long explanation about something completely unrelated 
            to any visual content or imagery whatsoever. It discusses various 
            topics but makes no mention of anything visual or graphical.
        """.trimIndent()
        
        val result = validator.validate(
            image = bitmap,
            text = text,
            mode = AIOutputMode.COMBINED
        )
        
        // Should pass but note consistency issue
        assertTrue("Should pass despite consistency issue", result.passed)
        assertTrue("Should have minor consistency issue",
            result.issues.any { it.type == IssueType.CONSISTENCY }
        )
    }
    
    // ===== VALIDATION RESULT TESTS =====
    
    @Test
    fun `validation result provides user-friendly messages`() {
        val bitmap = createMockBitmap(500, 500)
        val result = validator.validate(bitmap, null, AIOutputMode.IMAGE_ONLY)
        
        val message = result.getUserMessage()
        assertNotNull("Should have user message", message)
        assertTrue("Message should be non-empty", message.isNotEmpty())
    }
    
    @Test
    fun `validation result provides detailed feedback`() {
        val bitmap = createMockBitmap(500, 500)
        val result = validator.validate(bitmap, null, AIOutputMode.IMAGE_ONLY)
        
        val feedback = result.getDetailedFeedback()
        assertNotNull("Should have detailed feedback", feedback)
        assertTrue("Feedback should contain validation result", 
            feedback.contains("Validation Result")
        )
        assertTrue("Feedback should contain confidence", 
            feedback.contains("Confidence")
        )
    }
    
    @Test
    fun `confidence decreases with more issues`() {
        val shortText = "Short text"
        val result1 = validator.validate(null, shortText, AIOutputMode.TEXT_ONLY)
        
        val errorText = "Error: failed to process this cannot be done"
        val result2 = validator.validate(null, errorText, AIOutputMode.TEXT_ONLY)
        
        // result2 should have lower confidence due to more severe issues
        assertTrue("More issues should result in lower confidence",
            result2.confidence < result1.confidence
        )
    }
    
    @Test
    fun `shouldRetry is true for critical issues`() {
        val result = validator.validate(null, null, AIOutputMode.IMAGE_ONLY)
        
        assertTrue("Should retry for critical issues", result.shouldRetry)
        assertTrue("Should have critical issue",
            result.issues.any { it.severity == IssueSeverity.CRITICAL }
        )
    }
    
    @Test
    fun `shouldRetry is false when passed`() {
        val bitmap = createMockBitmap(500, 500)
        val result = validator.validate(bitmap, null, AIOutputMode.IMAGE_ONLY)
        
        if (result.passed) {
            assertFalse("Should not retry when passed", result.shouldRetry)
        }
    }
    
    // ===== HELPER METHODS =====
    
    private fun createMockBitmap(width: Int, height: Int): Bitmap {
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            // Fill with varied colors to avoid uniformity issues
            for (x in 0 until width step 10) {
                for (y in 0 until height step 10) {
                    val color = Color.rgb(
                        (x * 255 / width),
                        (y * 255 / height),
                        ((x + y) * 255 / (width + height))
                    )
                    if (x < width && y < height) {
                        setPixel(x, y, color)
                    }
                }
            }
        }
    }
}
