package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.math.APCAMath
import com.example.model.ColorNode
import com.example.math.Hsv

enum class ApcaMode {
    ForegroundToBackground,
    BackgroundToForeground
}

@Composable
fun ApcaPicker(
    baseColor: ColorNode,
    fixedColor: ColorNode, // The color that is kept constant
    mode: ApcaMode,
    targetLc: Float = 75f,
    onColorSelected: (Hsv) -> Unit,
    modifier: Modifier = Modifier
) {
    val baseHsv = baseColor.hsv
    val fixedRgb = fixedColor.rgb
    val fixedY = APCAMath.sRgbToY(fixedRgb.r, fixedRgb.g, fixedRgb.b)
    
    val limits = remember(mode, fixedY, targetLc) {
        if (mode == ApcaMode.BackgroundToForeground) { // Fixed is Background, picking Foreground
            APCAMath.findForegroundLuminanceLimits(fixedY, targetLc)
        } else { // Fixed is Foreground, picking Background
            APCAMath.findBackgroundLuminanceLimits(fixedY, targetLc)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Top Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (mode == ApcaMode.BackgroundToForeground) "Pick Foreground Color" else "Pick Background Color", 
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Fixed Ref:", style = MaterialTheme.typography.labelSmall)
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(Color(fixedRgb.r, fixedRgb.g, fixedRgb.b), CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        }

        val yColor = APCAMath.sRgbToY(baseColor.rgb.r, baseColor.rgb.g, baseColor.rgb.b)
        val isValid = (limits.darkLimit != null && yColor <= limits.darkLimit) ||
                      (limits.lightLimit != null && yColor >= limits.lightLimit)

        // Color Preview (Text on Background)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (mode == ApcaMode.BackgroundToForeground) Color(fixedRgb.r, fixedRgb.g, fixedRgb.b)
                    else Color(baseColor.rgb.r, baseColor.rgb.g, baseColor.rgb.b)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isValid) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Invalid Contrast",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    "APCA Lc ${targetLc.toInt()} Preview",
                    color = if (mode == ApcaMode.BackgroundToForeground) Color(baseColor.rgb.r, baseColor.rgb.g, baseColor.rgb.b)
                            else Color(fixedRgb.r, fixedRgb.g, fixedRgb.b),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // SV Rectangle with Mask
        val hueColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHsv.h, 1f, 1f)))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(baseHsv.h, limits) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val s = (change.position.x / size.width).coerceIn(0f, 1f) * 100f
                        val v = (1f - (change.position.y / size.height).coerceIn(0f, 1f)) * 100f
                        onColorSelected(Hsv(baseHsv.h, s, v))
                    }
                }
                .pointerInput(baseHsv.h, limits) {
                    detectTapGestures(
                        onPress = { offset ->
                            val s = (offset.x / size.width).coerceIn(0f, 1f) * 100f
                            val v = (1f - (offset.y / size.height).coerceIn(0f, 1f)) * 100f
                            onColorSelected(Hsv(baseHsv.h, s, v))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background Gradient
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.White, hueColor)
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black)
                    )
                )

                // APCA Invalid Mask
                val path = Path()
                val steps = 40
                val pointsLight = mutableListOf<Offset>()
                val pointsDark = mutableListOf<Offset>()

                for (i in 0..steps) {
                    val s = (i.toFloat() / steps) * 100f
                    val x = (i.toFloat() / steps) * size.width
                    
                    val vDark = limits.darkLimit?.let { APCAMath.findVForLuminance(baseHsv.h, s, it) ?: 100f } ?: 0f
                    val vLight = limits.lightLimit?.let { APCAMath.findVForLuminance(baseHsv.h, s, it) ?: 100f } ?: 100f
                    
                    val yDark = size.height * (1f - vDark / 100f)
                    val yLight = size.height * (1f - vLight / 100f)
                    
                    pointsLight.add(Offset(x, yLight))
                    pointsDark.add(Offset(x, yDark))
                }

                if (pointsLight.isNotEmpty()) {
                    path.moveTo(pointsLight.first().x, pointsLight.first().y)
                    pointsLight.forEach { path.lineTo(it.x, it.y) }
                    pointsDark.reversed().forEach { path.lineTo(it.x, it.y) }
                    path.close()
                    
                    // Draw Crosshatch or transparent overlay
                    drawPath(path, color = Color.Black.copy(alpha = 0.5f))
                    // Or pattern, but simple black alpha is fine
                }

                // Cursor
                val x = (baseHsv.s / 100f) * size.width
                val y = (1f - (baseHsv.v / 100f)) * size.height
                              
                drawCircle(
                    color = if (isValid) Color.White else Color.Red,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
