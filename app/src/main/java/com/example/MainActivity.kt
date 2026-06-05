package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.math.CVDMath
import com.example.math.HarmonyMath
import com.example.ui.components.ColorWheel
import com.example.ui.components.ContrastChecker
import com.example.ui.components.PaletteBar
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsState()

            MyApplicationTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { 
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Palette,
                                        contentDescription = "Logo",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text(
                                        "ChromaLogic", 
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            ),
                            actions = {
                                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                val context = androidx.compose.ui.platform.LocalContext.current
                                IconButton(onClick = { 
                                    val cssString = buildString {
                                        appendLine(":root {")
                                        state.colors.forEachIndexed { index, colorNode ->
                                            val name = if (colorNode.isBase) "base" else "palette-${index + 1}"
                                            appendLine("  --color-${name}: ${colorNode.hex};")
                                        }
                                        appendLine("  --color-bg: ${state.contrastBgColor.hex};")
                                        appendLine("  --color-fg: ${state.contrastFgColor.hex};")
                                        append("}")
                                    }
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(cssString))
                                    android.widget.Toast.makeText(context, "Copied CSS to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                                        contentDescription = "Export CSS",
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        
                        // 1. Tabs
                        var currentTab by remember { mutableStateOf(0) }
                        androidx.compose.material3.TabRow(
                            selectedTabIndex = currentTab,
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            divider = {},
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            androidx.compose.material3.Tab(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 }
                            ) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Primary color", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            androidx.compose.material3.Tab(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 }
                            ) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                                    Icon(androidx.compose.material.icons.Icons.Default.ColorLens, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Color wheel", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            androidx.compose.material3.Tab(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 }
                            ) {
                                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                                    Icon(androidx.compose.material.icons.Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("APCA Limit", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Wheel or Rectangle
                        if (currentTab == 1) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .padding(vertical = 16.dp, horizontal = 12.dp)
                            ) {
                                ColorWheel(
                                    colors = state.colors,
                                    onColorChanged = { index, hsv -> viewModel.updateColor(index, hsv) },
                                    onBaseSelected = { index -> viewModel.setBaseColorIndex(index) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 300.dp)
                                        .padding(bottom = 24.dp)
                                )
                                com.example.ui.components.HarmonySelector(
                                    selectedRule = state.harmonyRule,
                                    onRuleSelected = { viewModel.setHarmonyRule(it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else if (currentTab == 2) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                var apcaMode by remember { mutableStateOf(com.example.ui.components.ApcaMode.BackgroundToForeground) }
                                var targetLc by remember { mutableStateOf(75f) }
                                val baseNode = if (apcaMode == com.example.ui.components.ApcaMode.BackgroundToForeground) state.contrastFgColor else state.contrastBgColor
                                val fixedNode = if (apcaMode == com.example.ui.components.ApcaMode.BackgroundToForeground) state.contrastBgColor else state.contrastFgColor

                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    FilterChip(
                                        selected = apcaMode == com.example.ui.components.ApcaMode.BackgroundToForeground,
                                        onClick = { apcaMode = com.example.ui.components.ApcaMode.BackgroundToForeground },
                                        label = { Text("Bg\u2192Fg") }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    FilterChip(
                                        selected = apcaMode == com.example.ui.components.ApcaMode.ForegroundToBackground,
                                        onClick = { apcaMode = com.example.ui.components.ApcaMode.ForegroundToBackground },
                                        label = { Text("Fg\u2192Bg") }
                                    )
                                    Spacer(Modifier.weight(1f))
                                    // LC Target Dropdown (Simple button cycling for now to save space)
                                    TextButton(onClick = { 
                                        targetLc = when (targetLc) {
                                            45f -> 60f
                                            60f -> 75f
                                            75f -> 90f
                                            else -> 45f
                                        }
                                    }) {
                                        Text("Lc ${targetLc.toInt()}")
                                    }
                                }

                                com.example.ui.components.ApcaPicker(
                                    baseColor = baseNode,
                                    fixedColor = fixedNode,
                                    mode = apcaMode,
                                    targetLc = targetLc,
                                    onColorSelected = { hsv -> 
                                        if (apcaMode == com.example.ui.components.ApcaMode.BackgroundToForeground) {
                                            viewModel.updateContrastFgColor(hsv)
                                        } else {
                                            viewModel.updateContrastBgColor(hsv)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 460.dp)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
                                    .padding(16.dp)
                            ) {
                                val baseNode = state.colors.find { it.isBase } ?: state.colors.firstOrNull()
                                if (baseNode != null) {
                                    val baseIndex = state.colors.indexOf(baseNode)
                                    com.example.ui.components.PrimaryColorPicker(
                                        currentColor = baseNode,
                                        onColorChanged = { hsv -> viewModel.updateColor(baseIndex, hsv) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // 2. Harmony Rule & CVD Simulations
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // CVD Type Bento
                            var expandedCvd by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                                    .padding(12.dp)
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = expandedCvd,
                                    onExpandedChange = { expandedCvd = it }
                                ) {
                                    Column(modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)) {
                                        Text("VISION SIMULATION", style = MaterialTheme.typography.labelSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, modifier = Modifier.alpha(0.6f))
                                        Text(state.cvdType.name, style = MaterialTheme.typography.titleSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                                    }
                                    ExposedDropdownMenu(
                                        expanded = expandedCvd,
                                        onDismissRequest = { expandedCvd = false }
                                    ) {
                                        CVDMath.CVDType.values().forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type.name) },
                                                onClick = {
                                                    viewModel.setCvdType(type)
                                                    expandedCvd = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Palette Bar
                        if (state.colors.isNotEmpty()) {
                            PaletteBar(
                                colors = state.colors,
                                conflicts = state.conflicts,
                                cvdType = state.cvdType,
                                onColorBaseSelect = { index -> viewModel.setBaseColorIndex(index) }
                            )
                        }

                        // 4. Contrast Checker
                        ContrastChecker(
                            bgColor = state.contrastBgColor,
                            fgColor = state.contrastFgColor,
                            paletteColors = state.colors,
                            onBgColorUpdate = { viewModel.updateContrastBgColor(it) },
                            onFgColorUpdate = { viewModel.updateContrastFgColor(it) },
                            onSmartAdjust = { target -> viewModel.smartAdjustFg(target) }
                        )
                    }
                }
            }
        }
    }
}
