package com.example.math

import kotlin.math.*

object HarmonyMath {
    enum class Rule {
        Analogous, Monochromatic, Triad, Complementary, SplitComplementary,
        DoubleSplitComplementary, Square, Compound, Shades, Custom
    }

    fun applyHarmony(base: Hsv, rule: Rule): List<Hsv> {
        val h = base.h
        val s = base.s
        val v = base.v

        return when (rule) {
            Rule.Analogous -> listOf(
                Hsv(h - 30f, s, v),
                Hsv(h - 15f, s, v),
                Hsv(h, s, v),
                Hsv(h + 15f, s, v),
                Hsv(h + 30f, s, v)
            )
            Rule.Monochromatic -> listOf(
                Hsv(h, s * 0.2f, min(v * 1.0f, 100f)),
                Hsv(h, s * 0.6f, min(v * 0.8f, 100f)),
                Hsv(h, s, v),
                Hsv(h, s * 0.8f, v * 0.6f),
                Hsv(h, s * 0.4f, v * 0.4f)
            )
            Rule.Triad -> listOf(
                Hsv(h - 120f, s, v * 0.7f),
                Hsv(h - 120f, s * 0.7f, v),
                Hsv(h, s, v),
                Hsv(h + 120f, s * 0.7f, v),
                Hsv(h + 120f, s, v * 0.7f)
            )
            Rule.Complementary -> listOf(
                Hsv(h, s * 0.5f, v),
                Hsv(h, s, v * 0.7f),
                Hsv(h, s, v),
                Hsv(h + 180f, s, v),
                Hsv(h + 180f, s * 0.5f, v * 0.7f)
            )
            Rule.SplitComplementary -> listOf(
                Hsv(h + 150f, s * 0.8f, v),
                Hsv(h + 150f, s, v),
                Hsv(h, s, v),
                Hsv(h + 210f, s, v),
                Hsv(h + 210f, s * 0.8f, v)
            )
            Rule.DoubleSplitComplementary -> listOf(
                Hsv(h - 30f, s, v),
                Hsv(h + 30f, s, v),
                Hsv(h, s, v),
                Hsv(h + 150f, s, v),
                Hsv(h + 210f, s, v)
            )
            Rule.Square -> listOf(
                Hsv(h + 90f, s, v),
                Hsv(h + 180f, s, v),
                Hsv(h, s, v),
                Hsv(h + 270f, s, v),
                Hsv(h, s * 0.5f, v * 0.5f)
            )
            Rule.Compound -> listOf(
                Hsv(h + 30f, s * 0.9f, v * 0.9f),
                Hsv(h + 180f, s * 0.6f, v * 0.9f),
                Hsv(h, s, v),
                Hsv(h + 180f, s * 0.9f, v * 0.6f),
                Hsv(h - 30f, s * 0.9f, v * 0.9f)
            )
            Rule.Shades -> listOf(
                Hsv(h, s, v * 0.2f),
                Hsv(h, s, v * 0.5f),
                Hsv(h, s, v),
                Hsv(h, s, v * 0.8f),
                Hsv(h, s, v * 0.6f)
            )
            Rule.Custom -> listOf(
                Hsv(h, s, v),
                Hsv(h, s, v),
                Hsv(h, s, v),
                Hsv(h, s, v),
                Hsv(h, s, v)
            ) // Usually we don't recalculate array on Custom
        }.map { c -> Hsv(ColorMath.normalizeHue(c.h), c.s.coerceIn(0f, 100f), c.v.coerceIn(0f, 100f)) }
    }
}
