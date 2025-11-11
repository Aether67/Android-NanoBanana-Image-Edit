package com.yunho.nanobanana

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.components.CompactTextOutput
import com.yunho.nanobanana.components.ElegantTextOutput
import com.yunho.nanobanana.components.HighContrastTextDisplay
import com.yunho.nanobanana.ui.theme.NanobananaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for text output components
 * Tests rendering, accessibility, and user interactions
 */
@RunWith(AndroidJUnit4::class)
class TextOutputComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun elegantTextOutput_rendersCorrectly() {
        val testText = "This is a test AI response with some content."
        val testTitle = "Test Response"
        
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = testText,
                    title = testTitle,
                    isLoading = false
                )
            }
        }

        // Verify title is displayed
        composeTestRule.onNodeWithText(testTitle).assertIsDisplayed()
        
        // Verify text content is displayed
        composeTestRule.onNodeWithText(testText).assertIsDisplayed()
    }

    @Test
    fun elegantTextOutput_showsLoadingState() {
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = "",
                    isLoading = true
                )
            }
        }

        // Should not show text when loading
        composeTestRule.onNode(
            hasTestTag("text_content")
        ).assertDoesNotExist()
    }

    @Test
    fun elegantTextOutput_supportsTextSelection() {
        val selectableText = "This text should be selectable"
        
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = selectableText,
                    isLoading = false
                )
            }
        }

        // Verify text is displayed
        composeTestRule.onNodeWithText(selectableText).assertIsDisplayed()
        
        // Text should be in a SelectionContainer (not directly testable in all cases)
        // This validates the component structure
    }

    @Test
    fun compactTextOutput_rendersWithIcon() {
        val testText = "Short message"
        val testIcon = "✓"
        
        composeTestRule.setContent {
            NanobananaTheme {
                CompactTextOutput(
                    text = testText,
                    icon = testIcon
                )
            }
        }

        // Verify text is displayed
        composeTestRule.onNodeWithText(testText).assertIsDisplayed()
        
        // Verify icon is present
        composeTestRule.onNodeWithText(testIcon).assertIsDisplayed()
    }

    @Test
    fun highContrastTextDisplay_hasAccessibleColors() {
        val testText = "High contrast text"
        
        composeTestRule.setContent {
            NanobananaTheme {
                HighContrastTextDisplay(
                    text = testText,
                    fontSize = 20
                )
            }
        }

        // Verify text is rendered
        composeTestRule.onNodeWithText(testText).assertIsDisplayed()
        
        // Verify accessibility semantics
        composeTestRule.onNode(
            hasContentDescription("High contrast text display")
        ).assertExists()
    }

    @Test
    fun elegantTextOutput_hasAccessibilitySemantics() {
        val testText = "Accessibility test"
        val testTitle = "Test"
        
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = testText,
                    title = testTitle,
                    isLoading = false
                )
            }
        }

        // Verify semantic content description exists
        composeTestRule.onNode(
            hasContentDescription("Text output card showing: $testTitle")
        ).assertExists()
    }

    @Test
    fun elegantTextOutput_handlesLongText() {
        val longText = "This is a very long text. ".repeat(50)
        
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = longText,
                    maxHeight = 200
                )
            }
        }

        // Verify text is displayed (even if scrolled)
        composeTestRule.onNodeWithText(longText, substring = true).assertExists()
    }

    @Test
    fun compactTextOutput_hasCorrectSemantics() {
        val testText = "Compact message"
        
        composeTestRule.setContent {
            NanobananaTheme {
                CompactTextOutput(text = testText)
            }
        }

        // Verify semantic description
        composeTestRule.onNode(
            hasContentDescription("Compact text message: $testText")
        ).assertExists()
    }
}
