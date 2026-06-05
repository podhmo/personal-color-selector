package com.example.model

import com.example.math.Hsv
import com.example.math.Rgb

data class ColorNode(
    val id: String,
    val hex: String,
    val rgb: Rgb,
    val hsv: Hsv,
    val isBase: Boolean = false
)
