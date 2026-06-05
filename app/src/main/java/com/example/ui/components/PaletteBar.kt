package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.math.CVDMath
import com.example.model.ColorNode

@Composable
fun PaletteBar(
    colors: List<ColorNode>,
    conflicts: List<Pair<Int, Int>>,
    cvdType: CVDMath.CVDType,
    onColorBaseSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.forEachIndexed { index, node ->
            val displayRgb = CVDMath.simulate(node.rgb, cvdType)
            val displayColor = Color(displayRgb.r, displayRgb.g, displayRgb.b)
            
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(displayColor)
                        .clickable { onColorBaseSelect(index) }
                        .let { 
                            if (node.isBase) {
                                it.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            } else it 
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val hasConflict = conflicts.any { it.first == index || it.second == index }
                    if (hasConflict) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Conflict Warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (node.isBase) "${node.id.uppercase()}:BASE" else node.hex,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (node.isBase) FontWeight.Bold else FontWeight.Normal,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    maxLines = 1
                )
            }
        }
    }
}
