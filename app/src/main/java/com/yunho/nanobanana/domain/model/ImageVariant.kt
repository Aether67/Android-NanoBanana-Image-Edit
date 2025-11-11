package com.yunho.nanobanana.domain.model

import android.graphics.Bitmap
import java.util.UUID

/**
 * Represents a variant of a generated or enhanced image
 * Enables side-by-side comparison and selection of different AI-generated versions
 */
data class ImageVariant(
    val id: String = UUID.randomUUID().toString(),
    val image: Bitmap,
    val prompt: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEnhanced: Boolean = false,
    val metadata: VariantMetadata = VariantMetadata()
) {
    /**
     * Creates a copy with enhanced flag set
     */
    fun asEnhanced(enhancementTime: Long = 0): ImageVariant {
        return copy(
            isEnhanced = true,
            metadata = metadata.copy(enhancementTimeMs = enhancementTime)
        )
    }
}

/**
 * Metadata for a variant including generation and enhancement details
 */
data class VariantMetadata(
    val generationTimeMs: Long = 0,
    val enhancementTimeMs: Long = 0,
    val style: String = "",
    val parameters: AIParameters = AIParameters()
)

/**
 * Collection of variants for comparison
 * Provides immutable operations for managing variants
 */
data class VariantCollection(
    val variants: List<ImageVariant> = emptyList(),
    val selectedVariantId: String? = null
) {
    /**
     * Gets the currently selected variant
     */
    val selectedVariant: ImageVariant?
        get() = variants.find { it.id == selectedVariantId }
    
    /**
     * Selects a variant by ID
     */
    fun selectVariant(id: String): VariantCollection {
        return if (variants.any { it.id == id }) {
            copy(selectedVariantId = id)
        } else {
            this
        }
    }
    
    /**
     * Adds a new variant to the collection
     * Automatically selects it if it's the first variant
     */
    fun addVariant(variant: ImageVariant): VariantCollection {
        val newVariants = variants + variant
        val newSelectedId = selectedVariantId ?: variant.id
        return copy(variants = newVariants, selectedVariantId = newSelectedId)
    }
    
    /**
     * Removes a variant by ID
     * If the removed variant was selected, selects the first remaining variant
     */
    fun removeVariant(id: String): VariantCollection {
        val newVariants = variants.filterNot { it.id == id }
        val newSelectedId = when {
            newVariants.isEmpty() -> null
            selectedVariantId == id -> newVariants.firstOrNull()?.id
            else -> selectedVariantId
        }
        return copy(variants = newVariants, selectedVariantId = newSelectedId)
    }
    
    /**
     * Clears all variants
     */
    fun clear(): VariantCollection {
        return VariantCollection()
    }
    
    /**
     * Returns the number of variants
     */
    val size: Int
        get() = variants.size
    
    /**
     * Returns whether the collection is empty
     */
    val isEmpty: Boolean
        get() = variants.isEmpty()
}
