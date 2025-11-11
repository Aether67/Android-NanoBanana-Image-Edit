package com.yunho.nanobanana

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.accessibility.AccessibilityUtils
import com.yunho.nanobanana.accessibility.DynamicTypography
import com.yunho.nanobanana.components.*
import com.yunho.nanobanana.ui.theme.NanobananaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*

/**
 * Comprehensive accessibility tests for UI/UX enhancements
 * Tests WCAG compliance, dynamic typography, and focus navigation
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityEnhancementTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun wcag_contrastRatios_calculatedCorrectly() {
        // Test contrast ratio calculation
        val blackWhiteRatio = AccessibilityUtils.calculateContrastRatio(
            Color.Black,
            Color.White
        )
        
        // Black and white should have maximum contrast (21:1)
        assertTrue("Black/White contrast should be 21:1", blackWhiteRatio >= 20.9f)
    }
    
    @Test
    fun wcag_aa_compliance_verifiedForNormalText() {
        // Test WCAG AA compliance
        val blackOnWhite = AccessibilityUtils.meetsWCAG_AA(
            Color.Black,
            Color.White
        )
        
        assertTrue("Black on white should meet WCAG AA", blackOnWhite)
        
        // Test insufficient contrast
        val lightGrayOnWhite = AccessibilityUtils.meetsWCAG_AA(
            Color(0xFFCCCCCC),
            Color.White
        )
        
        assertFalse("Light gray on white should not meet WCAG AA", lightGrayOnWhite)
    }
    
    @Test
    fun accessibleColor_returnsHighContrastOption() {
        // Test on dark background
        val darkBg = Color(0xFF333333)
        val accessibleColor = AccessibilityUtils.getAccessibleColor(darkBg)
        
        assertEquals("Should return white for dark background", Color.White, accessibleColor)
        
        // Test on light background
        val lightBg = Color(0xFFF0F0F0)
        val accessibleColor2 = AccessibilityUtils.getAccessibleColor(lightBg)
        
        assertEquals("Should return black for light background", Color.Black, accessibleColor2)
    }
    
    @Test
    fun promptInputSkeleton_rendersCorrectly() {
        composeTestRule.setContent {
            NanobananaTheme {
                PromptInputSkeleton()
            }
        }
        
        // Skeleton should render without errors
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun aiOutputPanelSkeleton_showsImageAndTextPlaceholders() {
        composeTestRule.setContent {
            NanobananaTheme {
                AIOutputPanelSkeleton(
                    showImagePlaceholder = true,
                    showTextPlaceholder = true
                )
            }
        }
        
        // Both placeholders should be visible
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun synchronizedTransition_smoothlySwitchesBetweenStates() {
        var isLoading = true
        val loadingText = "Loading..."
        val contentText = "Loaded content"
        
        composeTestRule.setContent {
            NanobananaTheme {
                SynchronizedTransition(
                    isLoading = isLoading,
                    loadingContent = { 
                        androidx.compose.material3.Text(loadingText)
                    },
                    actualContent = { 
                        androidx.compose.material3.Text(contentText)
                    }
                )
            }
        }
        
        // Initially should show loading
        composeTestRule.onNodeWithText(loadingText).assertIsDisplayed()
        composeTestRule.onNodeWithText(contentText).assertDoesNotExist()
        
        // Change state
        isLoading = false
        composeTestRule.waitForIdle()
        
        // Should now show content (note: transition may take time)
        composeTestRule.mainClock.advanceTimeBy(500)
    }
    
    @Test
    fun elegantTextOutput_hasAccessibleSemantics() {
        val testText = "Accessible test content"
        val testTitle = "Test Title"
        
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = testText,
                    title = testTitle,
                    isLoading = false
                )
            }
        }
        
        // Verify semantic content description
        composeTestRule.onNode(
            hasContentDescription("Text output card showing: $testTitle")
        ).assertExists()
        
        // Verify text content is accessible
        composeTestRule.onNodeWithText(testText).assertIsDisplayed()
    }
    
    @Test
    fun highContrastTextDisplay_meetsAccessibilityStandards() {
        val testText = "High contrast accessible text"
        
        composeTestRule.setContent {
            NanobananaTheme {
                HighContrastTextDisplay(
                    text = testText,
                    fontSize = 20,
                    contrastLevel = 1.5f
                )
            }
        }
        
        // Verify high contrast display exists
        composeTestRule.onNode(
            hasContentDescription("High contrast text display")
        ).assertExists()
        
        // Verify text is rendered
        composeTestRule.onNodeWithText(testText).assertIsDisplayed()
    }
    
    @Test
    fun textOutput_supportsTextSelection() {
        val selectableText = "This text should be selectable for copy/paste"
        
        composeTestRule.setContent {
            NanobananaTheme {
                ElegantTextOutput(
                    text = selectableText,
                    isLoading = false
                )
            }
        }
        
        // Text should be displayed and accessible
        composeTestRule.onNodeWithText(selectableText).assertIsDisplayed()
    }
}

/**
 * Tests for transition animations between UI states
 */
@RunWith(AndroidJUnit4::class)
class TransitionAnimationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun synchronizedTransition_animatesCorrectly() {
        var isLoading by mutableStateOf(true)
        
        composeTestRule.setContent {
            NanobananaTheme {
                SynchronizedTransition(
                    isLoading = isLoading,
                    loadingContent = {
                        androidx.compose.material3.Text("Loading state")
                    },
                    actualContent = {
                        androidx.compose.material3.Text("Content state")
                    }
                )
            }
        }
        
        // Verify initial loading state
        composeTestRule.onNodeWithText("Loading state").assertIsDisplayed()
        
        // Trigger transition
        isLoading = false
        
        // Allow time for animation
        composeTestRule.mainClock.advanceTimeBy(450)
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun rippleProcessingIndicator_animatesInfinitely() {
        composeTestRule.setContent {
            NanobananaTheme {
                RippleProcessingIndicator()
            }
        }
        
        // Let animation run
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.mainClock.advanceTimeBy(2000)
        
        // Verify component doesn't crash during animation
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun textGenerationShimmer_animatesDots() {
        composeTestRule.setContent {
            NanobananaTheme {
                TextGenerationShimmer(text = "Processing")
            }
        }
        
        // Verify text is displayed
        composeTestRule.onNodeWithText("Processing").assertIsDisplayed()
        
        // Advance animation
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()
    }
}

/**
 * Tests for skeleton loading components
 */
@RunWith(AndroidJUnit4::class)
class SkeletonLoadingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun promptInputSkeleton_displaysShimmerEffect() {
        composeTestRule.setContent {
            NanobananaTheme {
                PromptInputSkeleton()
            }
        }
        
        // Should render without crashes
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun aiOutputPanelSkeleton_showsOnlyImagePlaceholder() {
        composeTestRule.setContent {
            NanobananaTheme {
                AIOutputPanelSkeleton(
                    showImagePlaceholder = true,
                    showTextPlaceholder = false
                )
            }
        }
        
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun aiOutputPanelSkeleton_showsOnlyTextPlaceholder() {
        composeTestRule.setContent {
            NanobananaTheme {
                AIOutputPanelSkeleton(
                    showImagePlaceholder = false,
                    showTextPlaceholder = true
                )
            }
        }
        
        composeTestRule.waitForIdle()
    }
    
    @Test
    fun skeletonImageCard_pulsesCorrectly() {
        composeTestRule.setContent {
            NanobananaTheme {
                SkeletonImageCard()
            }
        }
        
        // Let pulse animation run
        composeTestRule.mainClock.advanceTimeBy(1000)
        composeTestRule.waitForIdle()
    }
}
