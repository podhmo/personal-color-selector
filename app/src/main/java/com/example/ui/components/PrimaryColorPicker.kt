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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.math.Hsv
import com.example.model.ColorNode

@Composable
fun PrimaryColorPicker(
    currentColor: ColorNode,
    onColorChanged: (Hsv) -> Unit,
    modifier: Modifier = Modifier
) {
    val hsv = currentColor.hsv
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Top section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Base color", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("HEX", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }

        // Hex Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Color(currentColor.rgb.r, currentColor.rgb.g, currentColor.rgb.b), CircleShape)
            )
            Spacer(Modifier.width(16.dp))
            Text(currentColor.hex, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }

        // SV Rectangle
        val hueColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hsv.h, 1f, 1f)))
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.8f)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(hsv.h) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val s = (change.position.x / size.width).coerceIn(0f, 1f) * 100f
                        val v = (1f - (change.position.y / size.height).coerceIn(0f, 1f)) * 100f
                        onColorChanged(Hsv(hsv.h, s, v))
                    }
                }
                .pointerInput(hsv.h) {
                    detectTapGestures(
                        onPress = { offset ->
                            val s = (offset.x / size.width).coerceIn(0f, 1f) * 100f
                            val v = (1f - (offset.y / size.height).coerceIn(0f, 1f)) * 100f
                            onColorChanged(Hsv(hsv.h, s, v))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
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

                // Cursor
                val x = (hsv.s / 100f) * size.width
                val y = (1f - (hsv.v / 100f)) * size.height
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }

        // Hue Slider
        val hueGradient = Brush.horizontalGradient(
            colors = listOf(
                Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
        )
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .pointerInput(hsv.s, hsv.v) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val h = (change.position.x / size.width).coerceIn(0f, 1f) * 360f
                        onColorChanged(Hsv(h, hsv.s, hsv.v))
                    }
                }
                .pointerInput(hsv.s, hsv.v) {
                    detectTapGestures(
                        onPress = { offset ->
                            val h = (offset.x / size.width).coerceIn(0f, 1f) * 360f
                            onColorChanged(Hsv(h, hsv.s, hsv.v))
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerRadius = CornerRadius(8.dp.toPx())
                drawRoundRect(
                    brush = hueGradient,
                    cornerRadius = cornerRadius
                )

                val x = (hsv.h / 360f) * size.width
                val handleRadius = 10.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = handleRadius,
                    center = Offset(x.coerceIn(handleRadius, size.width - handleRadius), size.height / 2f),
                )
                drawCircle(
                    color = Color(0xFF1976D2),
                    radius = handleRadius,
                    center = Offset(x.coerceIn(handleRadius, size.width - handleRadius), size.height / 2f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
