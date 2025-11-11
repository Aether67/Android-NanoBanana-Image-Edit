package com.yunho.nanobanana.components

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.animations.MotionTokens

/**
 * Enhanced picked images component with staggered animations
 * Displays selected images with smooth entry animations and accessibility
 */
@Composable
fun PickedImages(
    selectedBitmaps: List<Bitmap>,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.semantics {
            contentDescription = "Selected images card showing ${selectedBitmaps.size} images"
        },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📸 Selected Images (${selectedBitmaps.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            LazyRow(
                modifier = Modifier.height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = selectedBitmaps,
                    key = { index, _ -> "image_$index" }
                ) { index, bitmap ->
                    AnimatedImageCard(
                        bitmap = bitmap,
                        index = index,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual image card with staggered entrance animation
 */
@Composable
private fun AnimatedImageCard(
    bitmap: Bitmap,
    index: Int,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(bitmap) {
        kotlinx.coroutines.delay(index * 50L) // Stagger animation
        isVisible = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "image_card_scale_$index"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = MotionTokens.DURATION_MEDIUM_2,
            easing = MotionTokens.EasingEmphasizedDecelerate
        ),
        label = "image_card_alpha_$index"
    )
    
    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .semantics {
                contentDescription = "Selected image ${index + 1} of ${index + 1}"
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Selected image number ${index + 1}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
