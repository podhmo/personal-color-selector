package com.example.math

import kotlin.math.*

object CVDMath {
    enum class CVDType {
        None, Protanopia, Deuteranopia, Tritanopia
    }

    private fun linearize(c: Float): Float {
        return if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    }

    private fun delinearize(c: Float): Float {
        return if (c <= 0.0031308f) 12.92f * c else 1.055f * c.pow(1f / 2.4f) - 0.055f
    }

    fun simulate(rgb: Rgb, type: CVDType): Rgb {
        if (type == CVDType.None) return rgb

        val rL = linearize(rgb.r / 255f)
        val gL = linearize(rgb.g / 255f)
        val bL = linearize(rgb.b / 255f)

        // RGB to LMS
        val l = 0.319708f * rL + 1.622340f * gL + 0.078428f * bL
        val m = 0.129945f * rL + 1.416870f * gL + 0.083041f * bL
        val s = 0.000000f * rL + 0.013399f * gL + 0.941648f * bL

        var lSim = l
        var mSim = m
        var sSim = s

        when (type) {
            CVDType.Protanopia -> {
                lSim = 1.049611f * m - 0.049611f * s
            }
            CVDType.Deuteranopia -> {
                mSim = 0.952733f * l + 0.047267f * s
            }
            CVDType.Tritanopia -> {
                sSim = -0.314850f * l + 1.314850f * m
            }
            else -> {}
        }

        // LMS to RGB
        val rS = 5.472212f * lSim - 6.128330f * mSim + 0.076326f * sSim
        val gS = -0.502016f * lSim + 1.137812f * mSim - 0.054326f * sSim
        val bS = 0.007142f * lSim - 0.114781f * mSim + 1.071731f * sSim

        val finalR = (delinearize(rS.coerceIn(0f, 1f)) * 255f).roundToInt().coerceIn(0, 255)
        val finalG = (delinearize(gS.coerceIn(0f, 1f)) * 255f).roundToInt().coerceIn(0, 255)
        val finalB = (delinearize(bS.coerceIn(0f, 1f)) * 255f).roundToInt().coerceIn(0, 255)

        return Rgb(finalR, finalG, finalB)
    }

    // Reference from typical RGB->XYZ->LAB formula (D65 standard)
    fun rgbToLab(rgb: Rgb): FloatArray {
        var r = linearize(rgb.r / 255f) * 100f
        var g = linearize(rgb.g / 255f) * 100f
        var b = linearize(rgb.b / 255f) * 100f

        val x = r * 0.4124564f + g * 0.3575761f + b * 0.1804375f
        val y = r * 0.2126729f + g * 0.7151522f + b * 0.0721750f
        val z = r * 0.0193339f + g * 0.1191920f + b * 0.9503041f

        // XYZ to Lab setup
        val refX = 95.047f
        val refY = 100.000f
        val refZ = 108.883f

        fun f(t: Float): Float {
            return if (t > 0.008856f) t.pow(1f / 3f) else (7.787f * t) + (16f / 116f)
        }

        val fx = f(x / refX)
        val fy = f(y / refY)
        val fz = f(z / refZ)

        val L = (116f * fy) - 16f
        val a = 500f * (fx - fy)
        val labB = 200f * (fy - fz)

        return floatArrayOf(L, a, labB)
    }

    fun deltaE(lab1: FloatArray, lab2: FloatArray): Float {
        val dL = lab1[0] - lab2[0]
        val da = lab1[1] - lab2[1]
        val db = lab1[2] - lab2[2]
        return sqrt(dL * dL + da * da + db * db)
    }

    fun findConflicts(colors: List<Rgb>): List<Pair<Int, Int>> {
        val types = listOf(CVDType.Protanopia, CVDType.Deuteranopia, CVDType.Tritanopia)
        val conflicts = mutableSetOf<Pair<Int, Int>>()

        for (type in types) {
            val simLabs = colors.map { rgbToLab(simulate(it, type)) }
            for (i in colors.indices) {
                for (j in i + 1 until colors.size) {
                    if (deltaE(simLabs[i], simLabs[j]) < 11.5f) {
                        conflicts.add(Pair(i, j))
                    }
                }
            }
        }
        return conflicts.toList()
    }
}
