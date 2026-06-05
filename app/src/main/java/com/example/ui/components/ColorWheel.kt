package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.math.Hsv
import com.example.model.ColorNode
import kotlin.math.*

@Composable
fun ColorWheel(
    colors: List<ColorNode>,
    onColorChanged: (Int, Hsv) -> Unit,
    onBaseSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val maxDim = min(constraints.maxWidth, constraints.maxHeight).toFloat()
        val center = Offset(maxDim / 2f, maxDim / 2f)
        val density = androidx.compose.ui.platform.LocalDensity.current
        val paddingPx = with(density) { 24.dp.toPx() }
        val radius = (maxDim / 2f) - paddingPx

        var draggingIndex by remember { mutableStateOf<Int?>(null) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(colors) {
                    detectTapGestures(
                        onPress = { offset ->
                            // Find nearest pin
                            val hitNodeIndex = colors.indexOfFirst { node ->
                                val pos = calculatePinPosition(center.x, center.y, radius, node.hsv.h, node.hsv.s)
                                val dist = sqrt((pos.x - offset.x).pow(2) + (pos.y - offset.y).pow(2))
                                dist < 48.dp.toPx() // Touch target size
                            }
                            if (hitNodeIndex >= 0) {
                                draggingIndex = hitNodeIndex
                                onBaseSelected(hitNodeIndex)
                            }
                        }
                    )
                }
                .pointerInput(colors) {
                    detectDragGestures(
                        onDragStart = { offset ->
                             val hitNodeIndex = colors.indexOfFirst { node ->
                                val pos = calculatePinPosition(center.x, center.y, radius, node.hsv.h, node.hsv.s)
                                val dist = sqrt((pos.x - offset.x).pow(2) + (pos.y - offset.y).pow(2))
                                dist < 48.dp.toPx()
                            }
                            if (hitNodeIndex >= 0) {
                                draggingIndex = hitNodeIndex
                                onBaseSelected(hitNodeIndex)
                            }
                        },
                        onDragEnd = { draggingIndex = null },
                        onDragCancel = { draggingIndex = null },
                        onDrag = { change, _ ->
                            change.consume()
                            draggingIndex?.let { index ->
                                val pos = change.position
                                val (h, s) = calculateColorFromPosition(center.x, center.y, radius, pos.x, pos.y)
                                val oldV = colors[index].hsv.v
                                onColorChanged(index, Hsv(h, s, oldV))
                            }
                        }
                    )
                }
        ) {
            // Background wheel
            val sweepColors = List(360) { i -> androidx.compose.ui.graphics.Color.hsv(360f - i.toFloat(), 1f, 1f) }
            drawCircle(
                brush = Brush.sweepGradient(sweepColors, center = center),
                radius = radius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Draw links lines first
            colors.forEach { node ->
                val pos = calculatePinPosition(center.x, center.y, radius, node.hsv.h, node.hsv.s)
                drawLine(
                    color = Color.White,
                    start = center,
                    end = pos,
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Draw pins
            colors.forEachIndexed { index, node ->
                val pos = calculatePinPosition(center.x, center.y, radius, node.hsv.h, node.hsv.s)
                
                // Outer shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.2f),
                    radius = 18.dp.toPx(),
                    center = Offset(pos.x, pos.y + 2.dp.toPx())
                )
                // White border
                drawCircle(
                    color = Color.White,
                    radius = 16.dp.toPx(),
                    center = pos
                )
                // Fill
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(
                        android.graphics.Color.HSVToColor(floatArrayOf(node.hsv.h, node.hsv.s / 100f, 1f))
                    ),
                    radius = 13.dp.toPx(),
                    center = pos
                )
                
                // Inner ring if base
                if (node.isBase) {
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = pos,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}

private fun calculatePinPosition(xc: Float, yc: Float, rMax: Float, h: Float, s: Float): Offset {
    val hRad = h * Math.PI / 180f
    val r = rMax * (s / 100f)
    val xp = xc + r * cos(hRad)
    // Canvas Y is down, standard math Y is up, formula dictates: Yp = Yc - R * sin(H).
    val yp = yc - r * sin(hRad)
    return Offset(xp.toFloat(), yp.toFloat())
}

private fun calculateColorFromPosition(xc: Float, yc: Float, rMax: Float, xp: Float, yp: Float): Pair<Float, Float> {
    var s = (sqrt((xp - xc).pow(2) + (yp - yc).pow(2)) / rMax) * 100f
    s = s.coerceIn(0f, 100f)
    // formula: atan2(-(yp-yc), xp-xc)
    var h = atan2(-(xp - xc), xp - xc) 
    // wait, atan2 arguments: Y, X.
    h = atan2(-(yp - yc).toDouble(), (xp - xc).toDouble()).toFloat() * 180f / Math.PI.toFloat()
    if (h < 0) h += 360f
    return Pair(h, s)
}
