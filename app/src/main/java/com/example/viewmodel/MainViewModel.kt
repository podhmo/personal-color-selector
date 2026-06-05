package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.math.*
import com.example.model.ColorNode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AppState(
    val colors: List<ColorNode> = emptyList(),
    val harmonyRule: HarmonyMath.Rule = HarmonyMath.Rule.Analogous,
    val cvdType: CVDMath.CVDType = CVDMath.CVDType.None,
    val conflicts: List<Pair<Int, Int>> = emptyList(),
    
    // Accessibility check states
    val contrastBgColor: ColorNode = ColorNode(id = "bg", hex = "#FFFFFF", rgb = Rgb(255, 255, 255), hsv = Hsv(0f, 0f, 100f), isBase = false),
    val contrastFgColor: ColorNode = ColorNode(id = "fg", hex = "#000000", rgb = Rgb(0, 0, 0), hsv = Hsv(0f, 0f, 0f), isBase = false)
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    init {
        // Initialize with default base color (e.g., Red)
        val initialBase = Hsv(0f, 100f, 100f)
        applyHarmony(initialBase, HarmonyMath.Rule.Analogous, 2)
        
        val initialColors = _state.value.colors
        if (initialColors.size >= 3) {
             _state.update { 
                 it.copy(
                     contrastBgColor = initialColors[0].copy(isBase = false, id = "bg"), 
                     contrastFgColor = initialColors[2].copy(isBase = false, id = "fg")
                 ) 
             }
        }
    }

    private fun applyHarmony(baseHsv: Hsv, rule: HarmonyMath.Rule, baseIndex: Int) {
        val hsvList = if (rule != HarmonyMath.Rule.Custom) {
            HarmonyMath.applyHarmony(baseHsv, rule)
        } else {
            // Keep existing colors if switching to custom, shouldn't happen from initial state
            _state.value.colors.map { it.hsv }
        }

        // We assume index 2 is the mathematical base for the formulas.
        // If the user selects a DIFFERENT index as base, we might shift the visual applying, 
        // but design doc assumes C3 is base for the formulas.
        // Let's just always use the formulas as C1..C5 and maintain the current baseIndex.
        
        // Actually, if they chose a harmony rule, the mathematical anchor is what we gave it.
        val newColors = hsvList.mapIndexed { index, hsv ->
            val rgb = ColorMath.hsvToRgb(hsv.h, hsv.s, hsv.v)
            ColorNode(
                id = "c${index + 1}",
                hex = ColorMath.rgbToHex(rgb),
                rgb = rgb,
                hsv = hsv,
                isBase = index == baseIndex
            )
        }

        _state.update { 
            it.copy(
                colors = newColors,
                harmonyRule = rule,
                conflicts = CVDMath.findConflicts(newColors.map { c -> c.rgb })
            )
        }
    }

    fun updateColor(index: Int, newHsv: Hsv) {
        val st = _state.value
        val node = st.colors[index]

        if (node.isBase) {
            // Updating the base color recalculates the whole harmony
            applyHarmony(newHsv, st.harmonyRule, index)
        } else {
            // Updating a non-base color switches to Custom rule
            val newColors = st.colors.toMutableList()
            val rgb = ColorMath.hsvToRgb(newHsv.h, newHsv.s, newHsv.v)
            newColors[index] = node.copy(
                hex = ColorMath.rgbToHex(rgb),
                rgb = rgb,
                hsv = newHsv
            )
            val newRule = HarmonyMath.Rule.Custom
            
            _state.update {
                it.copy(
                    colors = newColors,
                    harmonyRule = newRule,
                    conflicts = CVDMath.findConflicts(newColors.map { c -> c.rgb })
                )
            }
        }
    }

    fun setHarmonyRule(rule: HarmonyMath.Rule) {
        val st = _state.value
        val baseNode = st.colors.find { it.isBase } ?: st.colors[2]
        val baseIndex = st.colors.indexOf(baseNode).takeIf { it >= 0 } ?: 2
        applyHarmony(baseNode.hsv, rule, baseIndex)
    }

    fun setBaseColorIndex(index: Int) {
        val st = _state.value
        if (st.colors[index].isBase) return

        val newColors = st.colors.mapIndexed { i, node ->
            node.copy(isBase = i == index)
        }
        
        _state.update { it.copy(colors = newColors) }
        
        // When changing base, recalculate current harmony based on the new base's color
        val newBaseNode = newColors[index]
        applyHarmony(newBaseNode.hsv, st.harmonyRule, index)
    }

    fun setCvdType(type: CVDMath.CVDType) {
        _state.update { it.copy(cvdType = type) }
    }

    fun updateContrastBgColor(newHsv: Hsv) {
        val rgb = ColorMath.hsvToRgb(newHsv.h, newHsv.s, newHsv.v)
        val newNode = _state.value.contrastBgColor.copy(
            hex = ColorMath.rgbToHex(rgb),
            rgb = rgb,
            hsv = newHsv
        )
        _state.update { it.copy(contrastBgColor = newNode) }
    }

    fun updateContrastFgColor(newHsv: Hsv) {
        val rgb = ColorMath.hsvToRgb(newHsv.h, newHsv.s, newHsv.v)
        val newNode = _state.value.contrastFgColor.copy(
            hex = ColorMath.rgbToHex(rgb),
            rgb = rgb,
            hsv = newHsv
        )
        _state.update { it.copy(contrastFgColor = newNode) }
    }

    fun smartAdjustFg(targetRatio: Float) {
        val st = _state.value
        val bg = st.contrastBgColor
        val fg = st.contrastFgColor

        val newHsv = ColorMath.smartAdjustContrast(bg.rgb, fg.hsv, targetRatio)
        updateContrastFgColor(newHsv)
    }
}
