package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.math.HarmonyMath
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HarmonySelector(
    selectedRule: HarmonyMath.Rule,
    onRuleSelected: (HarmonyMath.Rule) -> Unit,
    modifier: Modifier = Modifier
) {
    val rules = HarmonyMath.Rule.values().filter { it != HarmonyMath.Rule.Custom }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Color harmonies: ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = selectedRule.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Left Arrow
            IconButton(
                onClick = { 
                    scope.launch { 
                        listState.animateScrollToItem(maxOf(0, listState.firstVisibleItemIndex - 2)) 
                    } 
                }, 
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous")
            }
            
            LazyRow(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules) { rule ->
                    HarmonyRuleButton(
                        rule = rule,
                        selected = rule == selectedRule,
                        onClick = { onRuleSelected(rule) }
                    )
                }
            }
            
            // Right Arrow
            IconButton(
                onClick = { 
                    scope.launch { 
                        listState.animateScrollToItem(minOf(rules.size - 1, listState.firstVisibleItemIndex + 2)) 
                    } 
                }, 
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next")
            }
        }
    }
}

@Composable
private fun HarmonyRuleButton(
    rule: HarmonyMath.Rule,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(28.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f
            
            // Draw outer circle
            drawCircle(color = Color.LightGray.copy(alpha = 0.5f), radius = radius, center = center, style = Stroke(2f))
            
            val angles = when (rule) {
                HarmonyMath.Rule.Analogous -> listOf(0f, -30f, 30f)
                HarmonyMath.Rule.Monochromatic -> listOf(0f)
                HarmonyMath.Rule.Triad -> listOf(0f, 120f, 240f)
                HarmonyMath.Rule.Complementary -> listOf(0f, 180f)
                HarmonyMath.Rule.SplitComplementary -> listOf(0f, 150f, 210f)
                HarmonyMath.Rule.DoubleSplitComplementary -> listOf(-30f, 30f, 150f, 210f)
                HarmonyMath.Rule.Square -> listOf(0f, 90f, 180f, 270f)
                HarmonyMath.Rule.Compound -> listOf(0f, 30f, 180f, 210f)
                HarmonyMath.Rule.Shades -> listOf(0f)
                else -> emptyList()
            }
            
            // Center dot
            drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 3f, center = center)
            
            // Lines and dots
            angles.forEachIndexed { index, angle ->
                val rad = angle * Math.PI / 180.0
                val px = center.x + radius * cos(rad).toFloat()
                val py = center.y + radius * sin(rad).toFloat()
                drawLine(color = Color.Black.copy(alpha = 0.7f), start = center, end = Offset(px, py), strokeWidth = 2f)
                
                // Base node is usually the 0 angle (or the first angle)
                if (index == 0) {
                    drawCircle(color = Color.White, radius = 4f, center = Offset(px, py))
                    drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 4f, center = Offset(px, py), style = Stroke(2f))
                } else {
                    drawCircle(color = Color.Black.copy(alpha = 0.7f), radius = 3f, center = Offset(px, py))
                }
            }
        }
    }
}
