package com.yunho.nanobanana.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.yunho.nanobanana.NanoBanana
import com.yunho.nanobanana.animations.MotionTokens
import kotlin.math.absoluteValue

private const val RATIO = 300f / 420f
private const val SCALE = 0.8F
private val defaultMargin = 40.dp
private val staticPageSpacing = 16.dp

/**
 * Enhanced style picker carousel with smooth parallax animations
 * Displays style preset images with page indicators and smooth transitions
 */
@Composable
fun StylePicker(
    content: NanoBanana.Content.Picker,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = MotionTokens.DURATION_MEDIUM_3,
            easing = MotionTokens.EasingEmphasizedDecelerate
        ),
        label = "style_picker_alpha"
    )
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .semantics {
                contentDescription = "Style picker carousel with ${content.pagerState.pageCount} style options"
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            val pagerState = content.pagerState

            Text(
                text = "Choose Style",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
            )

            @SuppressLint("UnusedBoxWithConstraintsScope")
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
                    .padding(top = 10.dp)
                    .weight(weight = 1f, false),
                contentAlignment = Alignment.Center
            ) {
                val maxWidth = maxWidth
                val itemWidth = min(maxWidth.minus(defaultMargin * 2), maxHeight.times(RATIO))
                val pageSpacing = -itemWidth.times((1f - SCALE).div(2f)) + staticPageSpacing

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Swipeable style carousel, current page ${pagerState.currentPage + 1} of ${pagerState.pageCount}"
                        },
                    contentPadding = PaddingValues(horizontal = maxWidth.minus(itemWidth).div(2)),
                    pageSpacing = pageSpacing
                ) { page ->
                    val pageOffset = pagerState.getOffsetDistanceInPages(page)
                    
                    // Enhanced parallax effect with shadow
                    Image(
                        modifier = Modifier
                            .graphicsLayer {
                                // Scale animation
                                val scale = lerp(
                                    start = 1f,
                                    stop = SCALE,
                                    fraction = pageOffset.absoluteValue
                                )
                                scaleX = scale
                                scaleY = scale
                                
                                // Parallax rotation for depth
                                rotationY = lerp(
                                    start = 0f,
                                    stop = -10f,
                                    fraction = pageOffset.coerceIn(-1f, 1f)
                                )
                                
                                // Fade out non-selected pages slightly
                                alpha = lerp(
                                    start = 1f,
                                    stop = 0.7f,
                                    fraction = pageOffset.absoluteValue.coerceIn(0f, 1f)
                                )
                            }
                            .width(maxWidth)
                            .aspectRatio(RATIO, true)
                            .shadow(
                                elevation = lerp(
                                    start = 8.dp,
                                    stop = 2.dp,
                                    fraction = pageOffset.absoluteValue
                                ).value.dp,
                                shape = RoundedCornerShape(60.dp)
                            )
                            .clip(shape = RoundedCornerShape(60.dp))
                            .semantics {
                                contentDescription = "Style option ${page + 1}: ${content.prompt}"
                            },
                        painter = painterResource(content.getImage(page)),
                        contentDescription = "Style preset image ${page + 1}",
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Page indicator dots
            StylePageIndicator(
                pageCount = pagerState.pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )
        }
    }
}

/**
 * Page indicator dots for the style carousel
 */
@Composable
private fun StylePageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { page ->
            val isSelected = page == currentPage
            
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "indicator_width_$page"
            )
            
            val alpha by animateFloatAsState(
                targetValue = if (isSelected) 1f else 0.3f,
                animationSpec = tween(
                    durationMillis = MotionTokens.DURATION_SHORT_4,
                    easing = MotionTokens.EasingStandard
                ),
                label = "indicator_alpha_$page"
            )
            
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .graphicsLayer { this.alpha = alpha }
                    .semantics {
                        contentDescription = if (isSelected) {
                            "Current page ${page + 1}"
                        } else {
                            "Page ${page + 1}"
                        }
                    }
            ) {
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.matchParentSize()
                ) {
                    drawRect(
                        color = if (isSelected) {
                            androidx.compose.ui.graphics.Color(0xFF6750A4) // Primary color
                        } else {
                            androidx.compose.ui.graphics.Color(0xFF79747E) // Outline color
                        }
                    )
                }
            }
        }
    }
}
