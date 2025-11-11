package com.yunho.nanobanana.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yunho.nanobanana.domain.model.ImageVariant
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comparison view for image variants
 * Allows users to view, select, and manage multiple AI-generated image versions
 */
@Composable
fun VariantComparison(
    variants: List<ImageVariant>,
    selectedVariantId: String?,
    onVariantSelect: (String) -> Unit,
    onVariantDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = variants.isNotEmpty(),
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Variants (${variants.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Compare & Select",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Variant cards row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    modifier = Modifier.semantics {
                        contentDescription = "Image variants list"
                    }
                ) {
                    items(variants, key = { it.id }) { variant ->
                        VariantCard(
                            variant = variant,
                            isSelected = variant.id == selectedVariantId,
                            onSelect = { onVariantSelect(variant.id) },
                            onDelete = { onVariantDelete(variant.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariantCard(
    variant: ImageVariant,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    Card(
        onClick = onSelect,
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            )
            .semantics {
                contentDescription = if (isSelected) "Selected variant" else "Variant"
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Image preview
                Image(
                    bitmap = variant.image.asImageBitmap(),
                    contentDescription = "Variant preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Status badge
                Surface(
                    color = if (variant.isEnhanced) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (variant.isEnhanced) "✨ Enhanced" else "Original",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (variant.isEnhanced) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onTertiary
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                
                // Timestamp
                Text(
                    text = formatTimestamp(variant.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Selected indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                )
            }
            
            // Delete button
            IconButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete variant",
                    tint = MaterialTheme.colorScheme.error
                )
            }
            
            // Delete confirmation dialog
            if (showDeleteConfirmation) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmation = false },
                    title = { Text("Delete Variant?") },
                    text = { Text("This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onDelete()
                                showDeleteConfirmation = false
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmation = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

/**
 * Formats timestamp to readable format
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val format = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            format.format(Date(timestamp))
        }
    }
}
