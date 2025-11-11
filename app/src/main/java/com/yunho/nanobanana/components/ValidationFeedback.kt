package com.yunho.nanobanana.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yunho.nanobanana.ai.IssueSeverity
import com.yunho.nanobanana.ai.IssueType
import com.yunho.nanobanana.ai.ValidationResult
import com.yunho.nanobanana.animations.MotionTokens

/**
 * Validation Feedback Component - Displays AI output validation results to users
 * 
 * Features:
 * - Visual confidence indicator with color coding
 * - Detailed issue list with severity badges
 * - Expandable details view
 * - User-friendly messages
 * - Smooth animations
 */
@Composable
fun ValidationFeedback(
    validationResult: ValidationResult?,
    modifier: Modifier = Modifier,
    isVisible: Boolean = true
) {
    AnimatedVisibility(
        visible = isVisible && validationResult != null,
        enter = expandVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_MEDIUM_3,
                easing = MotionTokens.EasingEmphasizedDecelerate
            )
        ) + fadeIn(),
        exit = shrinkVertically(
            animationSpec = tween(
                durationMillis = MotionTokens.DURATION_MEDIUM_2,
                easing = MotionTokens.EasingEmphasizedAccelerate
            )
        ) + fadeOut()
    ) {
        validationResult?.let { result ->
            ValidationCard(
                validationResult = result,
                modifier = modifier
            )
        }
    }
}

/**
 * Main validation card component
 */
@Composable
private fun ValidationCard(
    validationResult: ValidationResult,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "AI output validation feedback"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = getCardColor(validationResult)
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 3.dp
        ),
        onClick = { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with confidence
            ValidationHeader(
                validationResult = validationResult,
                isExpanded = isExpanded
            )
            
            // User message
            Text(
                text = validationResult.getUserMessage(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ValidationDetails(validationResult = validationResult)
            }
        }
    }
}

/**
 * Validation header with icon and confidence
 */
@Composable
private fun ValidationHeader(
    validationResult: ValidationResult,
    isExpanded: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            StatusIcon(validationResult = validationResult)
            
            Column {
                Text(
                    text = if (validationResult.passed) "Quality Check Passed" else "Quality Issues Detected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Confidence indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Confidence:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ConfidenceBadge(confidence = validationResult.confidence)
                }
            }
        }
        
        // Expand/collapse indicator
        Text(
            text = if (isExpanded) "▲" else "▼",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Status icon based on validation result
 */
@Composable
private fun StatusIcon(validationResult: ValidationResult) {
    val (icon, bgColor) = when {
        validationResult.passed && validationResult.confidence > 0.9f -> 
            "✅" to Color(0xFF4CAF50).copy(alpha = 0.2f)
        validationResult.passed && validationResult.confidence > 0.7f -> 
            "✓" to Color(0xFF8BC34A).copy(alpha = 0.2f)
        validationResult.passed -> 
            "⚠️" to Color(0xFFFFA726).copy(alpha = 0.2f)
        else -> 
            "❌" to Color(0xFFEF5350).copy(alpha = 0.2f)
    }
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 20.sp)
    }
}

/**
 * Confidence badge with color coding
 */
@Composable
private fun ConfidenceBadge(confidence: Float) {
    val (color, label) = when {
        confidence > 0.9f -> Color(0xFF4CAF50) to "Excellent"
        confidence > 0.7f -> Color(0xFF8BC34A) to "Good"
        confidence > 0.5f -> Color(0xFFFFA726) to "Fair"
        else -> Color(0xFFEF5350) to "Low"
    }
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${"%.0f".format(confidence * 100)}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

/**
 * Detailed validation information
 */
@Composable
private fun ValidationDetails(validationResult: ValidationResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        
        if (validationResult.issues.isNotEmpty()) {
            Text(
                text = "Issues (${validationResult.issues.size}):",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            // Group issues by severity
            val groupedIssues = validationResult.issues.groupBy { it.severity }
            
            listOf(
                IssueSeverity.CRITICAL,
                IssueSeverity.MAJOR,
                IssueSeverity.MINOR
            ).forEach { severity ->
                groupedIssues[severity]?.let { issues ->
                    IssueGroup(severity = severity, issues = issues)
                }
            }
        } else {
            Text(
                text = "✨ No issues detected - excellent quality!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Medium
            )
        }
        
        // Retry indicator
        if (validationResult.shouldRetry) {
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🔄", fontSize = 16.sp)
                    Text(
                        text = "Critical issues detected - automatic retry recommended",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Issue group component
 */
@Composable
private fun IssueGroup(
    severity: IssueSeverity,
    issues: List<com.yunho.nanobanana.ai.ValidationIssue>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Severity header
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SeverityBadge(severity = severity)
            Text(
                text = "${severity.name} (${issues.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Issues
        issues.forEach { issue ->
            IssueItem(issue = issue)
        }
    }
}

/**
 * Severity badge
 */
@Composable
private fun SeverityBadge(severity: IssueSeverity) {
    val (icon, color) = when (severity) {
        IssueSeverity.CRITICAL -> "🔴" to Color(0xFFEF5350)
        IssueSeverity.MAJOR -> "🟡" to Color(0xFFFFA726)
        IssueSeverity.MINOR -> "🔵" to Color(0xFF42A5F5)
    }
    
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = 12.sp)
    }
}

/**
 * Individual issue item
 */
@Composable
private fun IssueItem(issue: com.yunho.nanobanana.ai.ValidationIssue) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = getIssueTypeIcon(issue.type),
                fontSize = 14.sp
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = issue.type.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = issue.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * Gets icon for issue type
 */
private fun getIssueTypeIcon(type: IssueType): String {
    return when (type) {
        IssueType.MISSING_CONTENT -> "❓"
        IssueType.IMAGE_QUALITY -> "🖼️"
        IssueType.IMAGE_ARTIFACT -> "⚠️"
        IssueType.TEXT_QUALITY -> "📝"
        IssueType.TEXT_ERROR -> "❌"
        IssueType.TEXT_CONTRADICTION -> "⚡"
        IssueType.CONSISTENCY -> "🔗"
    }
}

/**
 * Gets card color based on validation result
 */
private fun getCardColor(result: ValidationResult): Color {
    return when {
        result.passed && result.confidence > 0.9f -> 
            Color(0xFF4CAF50).copy(alpha = 0.1f)
        result.passed && result.confidence > 0.7f -> 
            Color(0xFF8BC34A).copy(alpha = 0.1f)
        result.passed -> 
            Color(0xFFFFA726).copy(alpha = 0.1f)
        else -> 
            Color(0xFFEF5350).copy(alpha = 0.1f)
    }
}
