package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.ColorMath
import com.example.model.ColorNode
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceOn

@Composable
fun ContrastChecker(
    bgColor: ColorNode,
    fgColor: ColorNode,
    paletteColors: List<ColorNode>,
    onBgColorUpdate: (com.example.math.Hsv) -> Unit,
    onFgColorUpdate: (com.example.math.Hsv) -> Unit,
    onSmartAdjust: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                "Accessibility Contrast Checker",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkSurfaceOn
            )
            Spacer(Modifier.height(16.dp))

            // Selection Dropdowns/Rows
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ColorSelector(
                    "Background",
                    bgColor,
                    paletteColors,
                    onBgColorUpdate
                )
                ColorSelector(
                    "Text / Element",
                    fgColor,
                    paletteColors,
                    onFgColorUpdate
                )
            }

            Spacer(Modifier.height(16.dp))

            val bg = bgColor
            val fg = fgColor
            val lBg = ColorMath.getRelativeLuminance(bg.rgb)
            val lFg = ColorMath.getRelativeLuminance(fg.rgb)
            val cr = ColorMath.getContrastRatio(lBg, lFg)
            
            val yBg = com.example.math.APCAMath.sRgbToY(bg.rgb.r, bg.rgb.g, bg.rgb.b)
            val yFg = com.example.math.APCAMath.sRgbToY(fg.rgb.r, fg.rgb.g, fg.rgb.b)
            val apcaLc = com.example.math.APCAMath.getApcaContrast(yFg, yBg)

            // WCAG Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("WCAG: %.2f : 1", cr),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkSurfaceOn
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ScoreBadge("AA", cr >= 4.5f)
                    ScoreBadge("AAA", cr >= 7.0f)
                }
            }
            
            Spacer(Modifier.height(16.dp))

            // APCA Preview Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(bg.rgb.r, bg.rgb.g, bg.rgb.b), RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "APCA Lc ${apcaLc.toInt()}",
                    color = Color(fg.rgb.r, fg.rgb.g, fg.rgb.b),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))

                val textColor = Color(fg.rgb.r, fg.rgb.g, fg.rgb.b)
                ApcaPreviewItem(apcaLc, 45f, "Headline (≥24px)", 24.sp, FontWeight.Bold, textColor)
                ApcaPreviewItem(apcaLc, 60f, "Title (≥18px)", 18.sp, FontWeight.SemiBold, textColor)
                ApcaPreviewItem(apcaLc, 75f, "Subtitle (≥16px)", 16.sp, FontWeight.Medium, textColor)
                ApcaPreviewItem(apcaLc, 90f, "Body text: The quick brown fox jumps over the lazy dog.", 14.sp, FontWeight.Normal, textColor)
            }

            Spacer(Modifier.height(16.dp))

            if (cr < 4.5f) {
                Button(
                    onClick = { onSmartAdjust(4.5f) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Smart Adjust (Target 4.5 AAA/AA)")
                }
            }
        }
    }
}

@Composable
private fun ApcaPreviewItem(
    actualLc: Float,
    targetLc: Float,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    color: Color
) {
    val passed = actualLc >= targetLc
    val alpha = if (passed) 1f else 0.3f
    val appliedColor = color.copy(alpha = alpha)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (passed) androidx.compose.material.icons.Icons.Default.CheckCircle else androidx.compose.material.icons.Icons.Default.Cancel,
                contentDescription = if (passed) "Pass" else "Fail",
                tint = appliedColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = appliedColor
            )
        }
        Text(
            text = "Lc $targetLc",
            fontSize = 12.sp,
            color = appliedColor
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSelector(
    label: String,
    colorNode: ColorNode,
    paletteColors: List<ColorNode>,
    onColorUpdate: (com.example.math.Hsv) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkSurfaceOn)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = colorNode.hex,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = DarkSurfaceOn,
                        unfocusedTextColor = DarkSurfaceOn
                    ),
                    leadingIcon = {
                        Box(modifier = Modifier.size(16.dp).background(Color(colorNode.rgb.r, colorNode.rgb.g, colorNode.rgb.b), RoundedCornerShape(4.dp)))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    paletteColors.forEach { pColor ->
                        DropdownMenuItem(
                            text = { Text("${pColor.id.uppercase()} (${pColor.hex})") },
                            onClick = {
                                onColorUpdate(pColor.hsv)
                                expanded = false
                            },
                            leadingIcon = {
                                Box(modifier = Modifier.size(16.dp).background(Color(pColor.rgb.r, pColor.rgb.g, pColor.rgb.b), RoundedCornerShape(4.dp)))
                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("H", style = MaterialTheme.typography.labelSmall, color = DarkSurfaceOn)
            Slider(
                value = colorNode.hsv.h,
                onValueChange = { onColorUpdate(colorNode.hsv.copy(h = it)) },
                valueRange = 0f..360f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = DarkSurfaceOn,
                    activeTrackColor = DarkSurfaceOn,
                    inactiveTrackColor = DarkSurfaceOn.copy(alpha = 0.3f)
                )
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("S", style = MaterialTheme.typography.labelSmall, color = DarkSurfaceOn)
            Slider(
                value = colorNode.hsv.s,
                onValueChange = { onColorUpdate(colorNode.hsv.copy(s = it)) },
                valueRange = 0f..100f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = DarkSurfaceOn,
                    activeTrackColor = DarkSurfaceOn,
                    inactiveTrackColor = DarkSurfaceOn.copy(alpha = 0.3f)
                )
            )
            Text("V", style = MaterialTheme.typography.labelSmall, color = DarkSurfaceOn)
            Slider(
                value = colorNode.hsv.v,
                onValueChange = { onColorUpdate(colorNode.hsv.copy(v = it)) },
                valueRange = 0f..100f,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = DarkSurfaceOn,
                    activeTrackColor = DarkSurfaceOn,
                    inactiveTrackColor = DarkSurfaceOn.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
private fun ScoreBadge(label: String, passed: Boolean) {
    Surface(
        color = if (passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = if (passed) "$label PASS" else "$label FAIL",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
