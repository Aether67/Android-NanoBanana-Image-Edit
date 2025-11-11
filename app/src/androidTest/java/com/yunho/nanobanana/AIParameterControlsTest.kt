package com.yunho.nanobanana

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.ai.AIOutputMode
import com.yunho.nanobanana.ai.AIOutputStyle
import com.yunho.nanobanana.ai.PromptManager
import com.yunho.nanobanana.components.AIParameterControls
import com.yunho.nanobanana.ui.theme.NanobananaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for AI Parameter Controls component
 * Tests rendering, user interactions, and parameter updates
 */
@RunWith(AndroidJUnit4::class)
class AIParameterControlsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aiParameterControls_rendersCorrectly() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Verify main header is displayed
        composeTestRule.onNodeWithText("AI Parameters").assertIsDisplayed()
        composeTestRule.onNodeWithText("Customize AI behavior").assertIsDisplayed()
    }

    @Test
    fun aiParameterControls_expandsAndCollapses() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Initially collapsed - click to expand
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Verify expanded content is visible
        composeTestRule.onNodeWithText("Output Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Writing Style").assertIsDisplayed()
        
        // Click again to collapse
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Wait for collapse animation
        composeTestRule.waitForIdle()
    }

    @Test
    fun outputModeSelector_displaysAllModes() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Verify all output modes are displayed
        composeTestRule.onNodeWithText("Image").assertIsDisplayed()
        composeTestRule.onNodeWithText("Text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Both").assertIsDisplayed()
    }

    @Test
    fun outputModeSelector_changesMode() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        var parametersChanged = false
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager,
                    onParametersChanged = { parametersChanged = true }
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Click on "Text" mode
        composeTestRule.onNodeWithText("Text").performClick()
        
        // Verify mode changed
        assert(promptManager.outputMode == AIOutputMode.TEXT_ONLY)
        assert(parametersChanged)
    }

    @Test
    fun outputStyleSelector_displaysAllStyles() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Verify all output styles are displayed
        AIOutputStyle.values().forEach { style ->
            composeTestRule.onNodeWithText(style.displayName).assertIsDisplayed()
        }
    }

    @Test
    fun outputStyleSelector_changesStyle() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        var parametersChanged = false
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager,
                    onParametersChanged = { parametersChanged = true }
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Click on a different style
        composeTestRule.onNodeWithText(AIOutputStyle.TECHNICAL.displayName).performClick()
        
        // Verify style changed
        assert(promptManager.outputStyle == AIOutputStyle.TECHNICAL)
        assert(parametersChanged)
    }

    @Test
    fun creativitySlider_isDisplayed() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Verify creativity slider components
        composeTestRule.onNodeWithText("Creativity Level").assertIsDisplayed()
        composeTestRule.onNodeWithText("Controls AI's creative freedom").assertIsDisplayed()
    }

    @Test
    fun detailLevelSelector_displaysAllLevels() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Verify detail level label
        composeTestRule.onNodeWithText("Detail Level").assertIsDisplayed()
        
        // Verify all 5 levels are displayed
        for (level in 1..5) {
            composeTestRule.onNodeWithText(level.toString()).assertIsDisplayed()
        }
    }

    @Test
    fun detailLevelSelector_changesLevel() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        var parametersChanged = false
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager,
                    onParametersChanged = { parametersChanged = true }
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Click on level 5
        composeTestRule.onAllNodesWithText("5").onLast().performClick()
        
        // Verify level changed
        assert(promptManager.detailLevel == 5)
        assert(parametersChanged)
    }

    @Test
    fun reasoningDepthSelector_displaysAllDepths() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Verify reasoning depth components
        composeTestRule.onNodeWithText("Reasoning Depth").assertIsDisplayed()
        composeTestRule.onNodeWithText("Brief").assertIsDisplayed()
        composeTestRule.onNodeWithText("Detailed").assertIsDisplayed()
        composeTestRule.onNodeWithText("In-Depth").assertIsDisplayed()
    }

    @Test
    fun reasoningDepthSelector_changesDepth() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        var parametersChanged = false
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager,
                    onParametersChanged = { parametersChanged = true }
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Click on "In-Depth"
        composeTestRule.onNodeWithText("In-Depth").performClick()
        
        // Verify depth changed
        assert(promptManager.reasoningDepth == 3)
        assert(parametersChanged)
    }

    @Test
    fun aiParameterControls_hasAccessibilitySemantics() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager
                )
            }
        }

        // Verify semantic content description exists
        composeTestRule.onNode(
            hasContentDescription("AI parameter controls for customizing output")
        ).assertExists()
    }

    @Test
    fun parameterChanges_triggerCallback() {
        val promptManager = PromptManager(ApplicationProvider.getApplicationContext())
        var callbackCount = 0
        
        composeTestRule.setContent {
            NanobananaTheme {
                AIParameterControls(
                    promptManager = promptManager,
                    onParametersChanged = { callbackCount++ }
                )
            }
        }

        // Expand controls
        composeTestRule.onNodeWithText("AI Parameters").performClick()
        
        // Make multiple changes
        composeTestRule.onNodeWithText("Text").performClick()
        composeTestRule.onNodeWithText(AIOutputStyle.CREATIVE.displayName).performClick()
        composeTestRule.onNodeWithText("Brief").performClick()
        
        // Verify callback was triggered multiple times
        assert(callbackCount >= 3)
    }
}
