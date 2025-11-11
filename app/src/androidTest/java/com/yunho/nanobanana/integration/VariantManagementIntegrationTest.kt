package com.yunho.nanobanana.integration

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yunho.nanobanana.components.VariantComparison
import com.yunho.nanobanana.domain.model.ImageVariant
import com.yunho.nanobanana.domain.model.VariantCollection
import com.yunho.nanobanana.domain.model.VariantMetadata
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for variant management UI
 * Tests variant comparison, selection, and deletion flows
 */
@RunWith(AndroidJUnit4::class)
class VariantManagementIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun variantComparison_displaysAllVariants() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant1 = ImageVariant(
            id = "id1",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Prompt 1",
                style = "Style 1",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variant2 = ImageVariant(
            id = "id2",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Prompt 2",
                style = "Style 2",
                wasEnhanced = true,
                enhancementTimeMs = 1500L,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant1).add(variant2)

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onAllNodesWithTag("VariantCard")
            .assertCountEquals(2)
    }

    @Test
    fun variantCard_showsEnhancedBadge() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant = ImageVariant(
            id = "id1",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test",
                style = "Style",
                wasEnhanced = true,
                enhancementTimeMs = 1000L,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant)

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("✨ Enhanced")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun variantCard_showsOriginalBadge() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant = ImageVariant(
            id = "id1",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant)

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Original")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun variantCard_clickTriggersSelection() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant = ImageVariant(
            id = "test-id",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant)
        var selectedId: String? = null

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = { selectedId = it },
                onDeleteVariant = {}
            )
        }

        composeTestRule.onAllNodesWithTag("VariantCard").onFirst()
            .performClick()

        // Then
        assert(selectedId == "test-id")
    }

    @Test
    fun variantCard_deleteButtonTriggersConfirmation() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant = ImageVariant(
            id = "id1",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant)

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Delete variant")
            .performClick()

        // Then
        composeTestRule.onNodeWithText("Delete Variant")
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this variant?")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun variantCard_confirmDeletionTriggersCallback() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant = ImageVariant(
            id = "test-delete-id",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant)
        var deletedId: String? = null

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = { deletedId = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Delete variant")
            .performClick()
        composeTestRule.onNodeWithText("Delete")
            .performClick()

        // Then
        assert(deletedId == "test-delete-id")
    }

    @Test
    fun variantCard_cancelDeletionDoesNotTriggerCallback() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant = ImageVariant(
            id = "id1",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection().add(variant)
        var deletedId: String? = null

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = { deletedId = it }
            )
        }

        composeTestRule.onNodeWithContentDescription("Delete variant")
            .performClick()
        composeTestRule.onNodeWithText("Cancel")
            .performClick()

        // Then
        assert(deletedId == null)
    }

    @Test
    fun variantComparison_selectedVariantShowsIndicator() {
        // Given
        val testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val variant1 = ImageVariant(
            id = "id1",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test 1",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variant2 = ImageVariant(
            id = "id2",
            image = testBitmap,
            metadata = VariantMetadata(
                prompt = "Test 2",
                style = "Style",
                wasEnhanced = false,
                enhancementTimeMs = null,
                aiParameters = emptyMap(),
                timestamp = System.currentTimeMillis()
            )
        )
        val variants = VariantCollection()
            .add(variant1)
            .add(variant2)
            .select("id2")

        // When
        composeTestRule.setContent {
            VariantComparison(
                variants = variants,
                onSelectVariant = {},
                onDeleteVariant = {}
            )
        }

        // Then
        // Selected variant should have checkmark
        composeTestRule.onNodeWithContentDescription("Selected variant")
            .assertExists()
    }
}
