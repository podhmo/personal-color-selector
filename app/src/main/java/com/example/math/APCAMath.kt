package com.example.math

import kotlin.math.abs
import kotlin.math.pow

object APCAMath {
    fun sRgbToY(r: Int, g: Int, b: Int): Float {
        fun linearize(c: Float): Float {
            return if (c <= 0.04045f) {
                c / 12.92f
            } else {
                ((c + 0.055f) / 1.055f).pow(2.4f)
            }
        }
        val rl = linearize(r / 255f)
        val gl = linearize(g / 255f)
        val bl = linearize(b / 255f)
        return 0.2126f * rl + 0.7152f * gl + 0.0722f * bl
    }

    fun getApcaContrast(yFg: Float, yBg: Float): Float {
        val scale = 113.8f
        val blk = 0.022f

        if (yBg > yFg) { // Light mode (background is brighter)
            var fg = yFg
            if (fg < blk) fg += (blk - fg).pow(1.414f)
            val sub = 0.60f
            val sup = 0.62f
            return abs((yBg.pow(sub) - fg.pow(sup)) * scale)
        } else { // Dark mode (background is darker)
            var bg = yBg
            if (bg < blk) bg += (blk - bg).pow(1.414f)
            val sap = 0.58f
            val sab = 0.57f
            return abs((yFg.pow(sap) - bg.pow(sab)) * scale)
        }
    }

    data class LuminanceLimits(val darkLimit: Float?, val lightLimit: Float?)

    fun findForegroundLuminanceLimits(yBg: Float, targetLc: Float): LuminanceLimits {
        val EPSILON = 0.001f
        var darkLimit: Float? = null
        var lightLimit: Float? = null

        if (getApcaContrast(0.0f, yBg) >= targetLc) {
            var low = 0.0f
            var high = yBg
            while ((high - low) > EPSILON) {
                val mid = (low + high) / 2f
                val contrast = getApcaContrast(mid, yBg)
                if (contrast >= targetLc) {
                    low = mid
                } else {
                    high = mid
                }
            }
            darkLimit = low
        }

        if (getApcaContrast(1.0f, yBg) >= targetLc) {
            var low = yBg
            var high = 1.0f
            while ((high - low) > EPSILON) {
                val mid = (low + high) / 2f
                val contrast = getApcaContrast(mid, yBg)
                if (contrast >= targetLc) {
                    high = mid
                } else {
                    low = mid
                }
            }
            lightLimit = high
        }

        return LuminanceLimits(darkLimit, lightLimit)
    }

    fun findBackgroundLuminanceLimits(yFg: Float, targetLc: Float): LuminanceLimits {
        val EPSILON = 0.001f
        var darkLimit: Float? = null
        var lightLimit: Float? = null

        if (getApcaContrast(yFg, 0.0f) >= targetLc) {
            var low = 0.0f
            var high = yFg
            while ((high - low) > EPSILON) {
                val mid = (low + high) / 2f
                val contrast = getApcaContrast(yFg, mid)
                if (contrast >= targetLc) {
                    low = mid
                } else {
                    high = mid
                }
            }
            darkLimit = low
        }

        if (getApcaContrast(yFg, 1.0f) >= targetLc) {
            var low = yFg
            var high = 1.0f
            while ((high - low) > EPSILON) {
                val mid = (low + high) / 2f
                val contrast = getApcaContrast(yFg, mid)
                if (contrast >= targetLc) {
                    high = mid
                } else {
                    low = mid
                }
            }
            lightLimit = high
        }

        return LuminanceLimits(darkLimit, lightLimit)
    }

    fun findVForLuminance(h: Float, s: Float, targetY: Float): Float? {
        val rgbMax = ColorMath.hsvToRgb(h, s, 100f)
        val maxY = sRgbToY(rgbMax.r, rgbMax.g, rgbMax.b)
        if (targetY > maxY) return null

        var low = 0f
        var high = 100f
        for (i in 0..20) {
            val mid = (low + high) / 2f
            val rgb = ColorMath.hsvToRgb(h, s, mid)
            val y = sRgbToY(rgb.r, rgb.g, rgb.b)
            if (y < targetY) {
                low = mid
            } else {
                high = mid
            }
        }
        return (low + high) / 2f
    }
}
