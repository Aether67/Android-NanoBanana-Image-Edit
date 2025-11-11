package com.yunho.nanobanana.domain

import android.graphics.Bitmap
import com.yunho.nanobanana.domain.model.ImageVariant
import com.yunho.nanobanana.domain.model.VariantCollection
import com.yunho.nanobanana.domain.model.VariantMetadata
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

/**
 * Unit tests for ImageVariant and VariantCollection
 * Tests immutable operations and collection management
 */
class ImageVariantTest {

    private lateinit var testBitmap: Bitmap
    private lateinit var testMetadata: VariantMetadata

    @Before
    fun setup() {
        testBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        testMetadata = VariantMetadata(
            prompt = "Test prompt",
            style = "Photorealistic",
            wasEnhanced = false,
            enhancementTimeMs = null,
            aiParameters = mapOf("temperature" to "0.7"),
            timestamp = System.currentTimeMillis()
        )
    }

    @Test
    fun `ImageVariant creation with all fields`() {
        // When
        val variant = ImageVariant(
            id = UUID.randomUUID().toString(),
            image = testBitmap,
            metadata = testMetadata
        )

        // Then
        assertNotNull(variant.id)
        assertEquals(testBitmap, variant.image)
        assertEquals(testMetadata, variant.metadata)
    }

    @Test
    fun `VariantCollection starts empty`() {
        // When
        val collection = VariantCollection()

        // Then
        assertTrue(collection.variants.isEmpty())
        assertNull(collection.selectedVariantId)
    }

    @Test
    fun `VariantCollection add creates new variant`() {
        // Given
        val collection = VariantCollection()
        val variant = ImageVariant(
            id = "variant1",
            image = testBitmap,
            metadata = testMetadata
        )

        // When
        val newCollection = collection.add(variant)

        // Then
        assertEquals(1, newCollection.variants.size)
        assertEquals(variant.id, newCollection.variants[0].id)
        assertEquals(variant.id, newCollection.selectedVariantId) // First variant auto-selected
    }

    @Test
    fun `VariantCollection add multiple variants`() {
        // Given
        val collection = VariantCollection()
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val variant3 = ImageVariant("id3", testBitmap, testMetadata)

        // When
        val newCollection = collection
            .add(variant1)
            .add(variant2)
            .add(variant3)

        // Then
        assertEquals(3, newCollection.variants.size)
        assertEquals("id1", newCollection.selectedVariantId) // First remains selected
    }

    @Test
    fun `VariantCollection select changes selection`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val collection = VariantCollection()
            .add(variant1)
            .add(variant2)

        // When
        val newCollection = collection.select("id2")

        // Then
        assertEquals("id2", newCollection.selectedVariantId)
    }

    @Test
    fun `VariantCollection select non-existent ID does nothing`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val collection = VariantCollection().add(variant1)

        // When
        val newCollection = collection.select("non-existent")

        // Then
        assertEquals("id1", newCollection.selectedVariantId) // Selection unchanged
    }

    @Test
    fun `VariantCollection remove removes variant`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val collection = VariantCollection()
            .add(variant1)
            .add(variant2)

        // When
        val newCollection = collection.remove("id2")

        // Then
        assertEquals(1, newCollection.variants.size)
        assertEquals("id1", newCollection.variants[0].id)
    }

    @Test
    fun `VariantCollection remove selected variant auto-selects next`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val variant3 = ImageVariant("id3", testBitmap, testMetadata)
        val collection = VariantCollection()
            .add(variant1)
            .add(variant2)
            .add(variant3)

        // When
        val newCollection = collection.remove("id1") // Remove selected

        // Then
        assertEquals(2, newCollection.variants.size)
        assertEquals("id2", newCollection.selectedVariantId) // Next variant auto-selected
    }

    @Test
    fun `VariantCollection remove last variant clears selection`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val collection = VariantCollection().add(variant1)

        // When
        val newCollection = collection.remove("id1")

        // Then
        assertTrue(newCollection.variants.isEmpty())
        assertNull(newCollection.selectedVariantId)
    }

    @Test
    fun `VariantCollection clear removes all variants`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val collection = VariantCollection()
            .add(variant1)
            .add(variant2)

        // When
        val newCollection = collection.clear()

        // Then
        assertTrue(newCollection.variants.isEmpty())
        assertNull(newCollection.selectedVariantId)
    }

    @Test
    fun `VariantCollection getSelected returns selected variant`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val collection = VariantCollection()
            .add(variant1)
            .add(variant2)
            .select("id2")

        // When
        val selected = collection.getSelected()

        // Then
        assertNotNull(selected)
        assertEquals("id2", selected?.id)
    }

    @Test
    fun `VariantCollection getSelected returns null when no selection`() {
        // Given
        val collection = VariantCollection()

        // When
        val selected = collection.getSelected()

        // Then
        assertNull(selected)
    }

    @Test
    fun `VariantMetadata tracks enhancement status`() {
        // When
        val metadata = VariantMetadata(
            prompt = "Test",
            style = "Style",
            wasEnhanced = true,
            enhancementTimeMs = 1500L,
            aiParameters = emptyMap(),
            timestamp = System.currentTimeMillis()
        )

        // Then
        assertTrue(metadata.wasEnhanced)
        assertEquals(1500L, metadata.enhancementTimeMs)
    }

    @Test
    fun `VariantMetadata stores AI parameters`() {
        // When
        val metadata = VariantMetadata(
            prompt = "Test",
            style = "Style",
            wasEnhanced = false,
            enhancementTimeMs = null,
            aiParameters = mapOf(
                "temperature" to "0.7",
                "topP" to "0.9",
                "topK" to "40"
            ),
            timestamp = System.currentTimeMillis()
        )

        // Then
        assertEquals(3, metadata.aiParameters.size)
        assertEquals("0.7", metadata.aiParameters["temperature"])
        assertEquals("0.9", metadata.aiParameters["topP"])
        assertEquals("40", metadata.aiParameters["topK"])
    }

    @Test
    fun `VariantCollection immutability - original unchanged after add`() {
        // Given
        val original = VariantCollection()
        val variant = ImageVariant("id1", testBitmap, testMetadata)

        // When
        val modified = original.add(variant)

        // Then
        assertTrue(original.variants.isEmpty()) // Original unchanged
        assertEquals(1, modified.variants.size) // Modified has variant
    }

    @Test
    fun `VariantCollection immutability - original unchanged after remove`() {
        // Given
        val variant = ImageVariant("id1", testBitmap, testMetadata)
        val original = VariantCollection().add(variant)

        // When
        val modified = original.remove("id1")

        // Then
        assertEquals(1, original.variants.size) // Original unchanged
        assertTrue(modified.variants.isEmpty()) // Modified is empty
    }

    @Test
    fun `VariantCollection immutability - original unchanged after select`() {
        // Given
        val variant1 = ImageVariant("id1", testBitmap, testMetadata)
        val variant2 = ImageVariant("id2", testBitmap, testMetadata)
        val original = VariantCollection().add(variant1).add(variant2)

        // When
        val modified = original.select("id2")

        // Then
        assertEquals("id1", original.selectedVariantId) // Original unchanged
        assertEquals("id2", modified.selectedVariantId) // Modified has new selection
    }
}
