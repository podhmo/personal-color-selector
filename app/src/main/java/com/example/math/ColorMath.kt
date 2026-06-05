package com.example.math

import kotlin.math.*

data class Rgb(val r: Int, val g: Int, val b: Int)
data class Hsv(val h: Float, val s: Float, val v: Float)

object ColorMath {
    fun normalizeHue(h: Float): Float {
        var res = h % 360f
        if (res < 0) res += 360f
        return res
    }

    fun hsvToRgb(h: Float, s: Float, v: Float): Rgb {
        val hNorm = normalizeHue(h)
        val sNorm = s.coerceIn(0f, 100f) / 100f
        val vNorm = v.coerceIn(0f, 100f) / 100f

        val c = vNorm * sNorm
        val x = c * (1 - abs((hNorm / 60) % 2 - 1))
        val m = vNorm - c

        var rPrime = 0f
        var gPrime = 0f
        var bPrime = 0f

        when {
            hNorm < 60 -> { rPrime = c; gPrime = x; bPrime = 0f }
            hNorm < 120 -> { rPrime = x; gPrime = c; bPrime = 0f }
            hNorm < 180 -> { rPrime = 0f; gPrime = c; bPrime = x }
            hNorm < 240 -> { rPrime = 0f; gPrime = x; bPrime = c }
            hNorm < 300 -> { rPrime = x; gPrime = 0f; bPrime = c }
            else -> { rPrime = c; gPrime = 0f; bPrime = x }
        }

        return Rgb(
            r = ((rPrime + m) * 255f).roundToInt().coerceIn(0, 255),
            g = ((gPrime + m) * 255f).roundToInt().coerceIn(0, 255),
            b = ((bPrime + m) * 255f).roundToInt().coerceIn(0, 255)
        )
    }

    fun rgbToHsv(r: Int, g: Int, b: Int): Hsv {
        val rNorm = r / 255f
        val gNorm = g / 255f
        val bNorm = b / 255f

        val cMax = max(rNorm, max(gNorm, bNorm))
        val cMin = min(rNorm, min(gNorm, bNorm))
        val delta = cMax - cMin

        var h = 0f
        if (delta == 0f) {
            h = 0f
        } else if (cMax == rNorm) {
            h = 60f * (((gNorm - bNorm) / delta) % 6f)
        } else if (cMax == gNorm) {
            h = 60f * (((bNorm - rNorm) / delta) + 2f)
        } else if (cMax == bNorm) {
            h = 60f * (((rNorm - gNorm) / delta) + 4f)
        }

        val s = if (cMax == 0f) 0f else delta / cMax
        val v = cMax

        return Hsv(normalizeHue(h), s * 100f, v * 100f)
    }

    fun rgbToHex(rgb: Rgb): String {
        return String.format("#%02X%02X%02X", rgb.r, rgb.g, rgb.b)
    }

    // Relative Luminance
    fun getRelativeLuminance(rgb: Rgb): Float {
        fun linearize(c: Float): Float {
            return if (c <= 0.04045f) {
                c / 12.92f
            } else {
                ((c + 0.055f) / 1.055f).pow(2.4f)
            }
        }
        val rl = linearize(rgb.r / 255f)
        val gl = linearize(rgb.g / 255f)
        val bl = linearize(rgb.b / 255f)
        return 0.2126f * rl + 0.7152f * gl + 0.0722f * bl
    }

    fun getContrastRatio(l1: Float, l2: Float): Float {
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    fun smartAdjustContrast(bgRgb: Rgb, fgHsv: Hsv, targetRatio: Float): Hsv {
        val lbg = getRelativeLuminance(bgRgb)
        val ltarget = if (lbg < 0.5f) {
            targetRatio * (lbg + 0.05f) - 0.05f
        } else {
            (lbg + 0.05f) / targetRatio - 0.05f
        }
        
        var low = 0f
        var high = 100f
        var bestV = fgHsv.v
        var minDiff = Float.MAX_VALUE

        for (i in 0 until 50) { // binary search
            val mid = (low + high) / 2f
            val midRgb = hsvToRgb(fgHsv.h, fgHsv.s, mid)
            val lmid = getRelativeLuminance(midRgb)
            val diff = abs(lmid - ltarget)
            
            if (diff < minDiff) {
                minDiff = diff
                bestV = mid
            }
            if (diff < 0.001f) {
                break
            }
            
            if (lmid < ltarget) { // mid is too dark, needs more brightness
                low = mid
            } else {
                high = mid
            }
        }
        // Double check contrast
        return Hsv(fgHsv.h, fgHsv.s, bestV)
    }
}
