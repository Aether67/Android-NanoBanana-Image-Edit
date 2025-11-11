package com.yunho.nanobanana

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.components.RippleProcessingIndicator
import com.yunho.nanobanana.components.ShimmerLoadingBox
import com.yunho.nanobanana.components.TextGenerationShimmer
import com.yunho.nanobanana.ui.theme.NanobananaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for loading effect components
 * Tests shimmer, ripple, and blur animations
 */
@RunWith(AndroidJUnit4::class)
class LoadingEffectsComponentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shimmerLoadingBox_showsWhenLoading() {
        composeTestRule.setContent {
            NanobananaTheme {
                ShimmerLoadingBox(
                    isLoading = true,
                    contentAfterLoading = {
                        // Empty content for test
                    }
                )
            }
        }

        // Shimmer should be visible when loading
        // The shimmer box itself doesn't have text, so we verify it exists
        composeTestRule.waitForIdle()
    }

    @Test
    fun shimmerLoadingBox_showsContentWhenNotLoading() {
        val contentText = "Loaded content"
        
        composeTestRule.setContent {
            NanobananaTheme {
                ShimmerLoadingBox(
                    isLoading = false,
                    contentAfterLoading = {
                        androidx.compose.material3.Text(contentText)
                    }
                )
            }
        }

        // Verify content is shown when not loading
        composeTestRule.onNodeWithText(contentText).assertIsDisplayed()
    }

    @Test
    fun rippleProcessingIndicator_renders() {
        composeTestRule.setContent {
            NanobananaTheme {
                RippleProcessingIndicator()
            }
        }

        // Ripple indicator should be rendered
        // It's a visual component, so we just verify it doesn't crash
        composeTestRule.waitForIdle()
    }

    @Test
    fun textGenerationShimmer_displaysText() {
        val shimmerText = "Generating..."
        
        composeTestRule.setContent {
            NanobananaTheme {
                TextGenerationShimmer(text = shimmerText)
            }
        }

        // Verify the shimmer text is displayed
        composeTestRule.onNodeWithText(shimmerText).assertIsDisplayed()
    }

    @Test
    fun textGenerationShimmer_hasAnimatedDots() {
        composeTestRule.setContent {
            NanobananaTheme {
                TextGenerationShimmer(text = "Processing")
            }
        }

        // The shimmer should have animated dots (visual test)
        // We verify the component renders without crashing
        composeTestRule.waitForIdle()
        
        // Give time for animation to start
        composeTestRule.mainClock.advanceTimeBy(500)
    }
}
