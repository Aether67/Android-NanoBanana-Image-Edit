package com.yunho.nanobanana

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.ai.AIOutputMode
import com.yunho.nanobanana.ai.AIOutputStyle
import com.yunho.nanobanana.ai.PromptManager
import com.yunho.nanobanana.ai.PromptTemplate
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for AI Prompt Manager
 * Validates prompt generation, context tracking, and parameter management
 */
@RunWith(AndroidJUnit4::class)
class PromptManagerTest {

    private lateinit var context: Context
    private lateinit var promptManager: PromptManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        promptManager = PromptManager(context)
    }

    @Test
    fun promptManager_initialization_loadsDefaultValues() {
        // Verify default values are set correctly
        assertEquals(AIOutputMode.COMBINED, promptManager.outputMode)
        assertEquals(AIOutputStyle.BALANCED, promptManager.outputStyle)
        assertTrue(promptManager.creativityLevel in 0.0f..1.0f)
        assertTrue(promptManager.detailLevel in 1..5)
        assertTrue(promptManager.reasoningDepth in 1..3)
    }

    @Test
    fun generatePrompt_imageOnlyMode_includesQualityDirectives() {
        val basePrompt = "Transform to watercolor painting"
        
        val enhancedPrompt = promptManager.generatePrompt(
            basePrompt = basePrompt,
            mode = AIOutputMode.IMAGE_ONLY,
            includeReasoning = false
        )
        
        // Verify base prompt is included
        assertTrue(enhancedPrompt.contains(basePrompt))
        
        // Verify quality directives are present
        assertTrue(enhancedPrompt.contains("resolution"))
        assertTrue(enhancedPrompt.contains("artifact-free"))
    }

    @Test
    fun generatePrompt_textOnlyMode_includesStylePrefix() {
        promptManager.outputStyle = AIOutputStyle.TECHNICAL
        val basePrompt = "Analyze this image"
        
        val enhancedPrompt = promptManager.generatePrompt(
            basePrompt = basePrompt,
            mode = AIOutputMode.TEXT_ONLY,
            includeReasoning = true
        )
        
        // Verify style prefix is applied
        assertTrue(enhancedPrompt.contains("technical"))
        
        // Verify reasoning directive is present
        assertTrue(enhancedPrompt.contains("explanation") || 
                   enhancedPrompt.contains("reasoning") ||
                   enhancedPrompt.contains("analysis"))
    }

    @Test
    fun generatePrompt_combinedMode_includesBothImageAndText() {
        val basePrompt = "Create futuristic cityscape"
        
        val enhancedPrompt = promptManager.generatePrompt(
            basePrompt = basePrompt,
            mode = AIOutputMode.COMBINED,
            includeReasoning = true
        )
        
        // Verify both image and text components are present
        assertTrue(enhancedPrompt.contains("Image"))
        assertTrue(enhancedPrompt.contains("Text") || enhancedPrompt.contains("Analysis"))
        assertTrue(enhancedPrompt.contains(basePrompt))
    }

    @Test
    fun generatePrompt_respectsDetailLevel() {
        val basePrompt = "Test prompt"
        
        // Test detail level 1
        promptManager.detailLevel = 1
        val prompt1 = promptManager.generatePrompt(basePrompt, AIOutputMode.IMAGE_ONLY, false)
        assertTrue(prompt1.contains("simple") || prompt1.contains("minimalist"))
        
        // Test detail level 5
        promptManager.detailLevel = 5
        val prompt5 = promptManager.generatePrompt(basePrompt, AIOutputMode.IMAGE_ONLY, false)
        assertTrue(prompt5.contains("hyper-realistic") || 
                   prompt5.contains("extremely detailed") ||
                   prompt5.contains("masterpiece"))
    }

    @Test
    fun generatePrompt_respectsReasoningDepth() {
        val basePrompt = "Explain this"
        
        // Test reasoning depth 1 (brief)
        promptManager.reasoningDepth = 1
        val prompt1 = promptManager.generatePrompt(basePrompt, AIOutputMode.TEXT_ONLY, true)
        assertTrue(prompt1.contains("brief"))
        
        // Test reasoning depth 3 (in-depth)
        promptManager.reasoningDepth = 3
        val prompt3 = promptManager.generatePrompt(basePrompt, AIOutputMode.TEXT_ONLY, true)
        assertTrue(prompt3.contains("in-depth") || 
                   prompt3.contains("step-by-step") ||
                   prompt3.contains("hypotheses"))
    }

    @Test
    fun promptManager_contextTracking_maintainsHistory() {
        // Clear any existing context
        promptManager.clearContext()
        
        // Generate multiple prompts
        promptManager.generatePrompt("First prompt", AIOutputMode.IMAGE_ONLY, false)
        promptManager.generatePrompt("Second prompt", AIOutputMode.TEXT_ONLY, false)
        val thirdPrompt = promptManager.generatePrompt("Third prompt", AIOutputMode.COMBINED, false)
        
        // Verify context is included (check for "Previous" or similar context indicator)
        assertTrue(thirdPrompt.contains("Previous") || thirdPrompt.contains("Context"))
    }

    @Test
    fun promptManager_clearContext_removesHistory() {
        // Add some context
        promptManager.generatePrompt("First", AIOutputMode.IMAGE_ONLY, false)
        promptManager.generatePrompt("Second", AIOutputMode.TEXT_ONLY, false)
        
        // Clear context
        promptManager.clearContext()
        
        // Generate new prompt - should not include previous context
        val newPrompt = promptManager.generatePrompt("New prompt", AIOutputMode.IMAGE_ONLY, false)
        
        // Context section should not appear in new prompt
        val contextLines = newPrompt.split("\n").filter { 
            it.contains("Previous") && it.contains("Context") 
        }
        // Should have at most the section header, not actual previous prompts
        assertTrue(contextLines.size <= 1)
    }

    @Test
    fun getTemplatePrompt_returnsCorrectTemplate() {
        val explanationTemplate = promptManager.getTemplatePrompt(PromptTemplate.IMAGE_EXPLANATION)
        assertTrue(explanationTemplate.contains("Analyze"))
        assertTrue(explanationTemplate.contains("image"))
        
        val technicalTemplate = promptManager.getTemplatePrompt(PromptTemplate.TECHNICAL_ANALYSIS)
        assertTrue(technicalTemplate.contains("technical"))
        assertTrue(technicalTemplate.contains("analysis"))
    }

    @Test
    fun saveSettings_persistsParameters() {
        // Set custom values
        promptManager.creativityLevel = 0.9f
        promptManager.detailLevel = 5
        promptManager.reasoningDepth = 3
        promptManager.outputStyle = AIOutputStyle.CREATIVE
        
        // Save settings
        promptManager.saveSettings()
        
        // Create new instance and verify values are loaded
        val newPromptManager = PromptManager(context)
        assertEquals(0.9f, newPromptManager.creativityLevel, 0.01f)
        assertEquals(5, newPromptManager.detailLevel)
        assertEquals(3, newPromptManager.reasoningDepth)
        assertEquals(AIOutputStyle.CREATIVE, newPromptManager.outputStyle)
    }

    @Test
    fun promptManager_allOutputStyles_haveDisplayNames() {
        AIOutputStyle.values().forEach { style ->
            assertNotNull(style.displayName)
            assertTrue(style.displayName.isNotEmpty())
        }
    }

    @Test
    fun promptManager_allTemplates_haveDisplayNames() {
        PromptTemplate.values().forEach { template ->
            assertNotNull(template.displayName)
            assertTrue(template.displayName.isNotEmpty())
        }
    }

    @Test
    fun generatePrompt_withDifferentStyles_producesVariedOutput() {
        val basePrompt = "Describe this scene"
        
        promptManager.outputStyle = AIOutputStyle.FORMAL
        val formalPrompt = promptManager.generatePrompt(basePrompt, AIOutputMode.TEXT_ONLY, false)
        
        promptManager.outputStyle = AIOutputStyle.CASUAL
        val casualPrompt = promptManager.generatePrompt(basePrompt, AIOutputMode.TEXT_ONLY, false)
        
        // Prompts should differ based on style
        assertNotEquals(formalPrompt, casualPrompt)
    }

    @Test
    fun creativityLevel_staysWithinValidRange() {
        // Test boundary values
        promptManager.creativityLevel = 0.0f
        assertTrue(promptManager.creativityLevel >= 0.0f)
        
        promptManager.creativityLevel = 1.0f
        assertTrue(promptManager.creativityLevel <= 1.0f)
        
        promptManager.creativityLevel = 0.5f
        assertEquals(0.5f, promptManager.creativityLevel, 0.01f)
    }
}
