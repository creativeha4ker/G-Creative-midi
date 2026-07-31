package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PianoKeyNote
import com.example.ui.components.DSPKnobControl
import com.example.ui.theme.*
import com.example.ui.viewmodel.PresetViewModel

@Composable
fun SoundEngineControlsScreen(
    viewModel: PresetViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val spec by viewModel.activePreset.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MahoganyDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DSP SOUND ENGINE RACK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioAmber
                )
                Text(
                    text = "Parameters & DSP Tweak Studio",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            TextButton(
                onClick = { viewModel.resetToFactoryDefault() },
                modifier = Modifier.testTag("reset_factory_specs_btn")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("RESET SPECS", fontSize = 11.sp, color = StudioCyan, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Dual Layer Engine Rack Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Layers, contentDescription = null, tint = Gold60)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DUAL LAYER ENGINE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Switch(
                        checked = spec.isDualLayerEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateActivePreset(spec.copy(isDualLayerEnabled = enabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StudioAmber
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Layer 1 Controls
                Text("Layer 1: ${spec.layer1Name} (0-127 Velocity)", fontSize = 12.sp, color = Gold80, fontWeight = FontWeight.SemiBold)
                DSPKnobControl(
                    title = "Layer 1 Master Volume",
                    valueText = "${(spec.layer1Volume * 100).toInt()}%",
                    valueProgress = spec.layer1Volume,
                    onValueChange = { vol -> viewModel.updateActivePreset(spec.copy(layer1Volume = vol)) },
                    accentColor = Gold60
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Layer 2 Controls
                Text("Layer 2: ${spec.layer2Name}", fontSize = 12.sp, color = Gold80, fontWeight = FontWeight.SemiBold)
                DSPKnobControl(
                    title = "Layer 2 Blend Volume",
                    valueText = "${(spec.layer2Volume * 100).toInt()}%",
                    valueProgress = spec.layer2Volume,
                    onValueChange = { vol -> viewModel.updateActivePreset(spec.copy(layer2Volume = vol)) },
                    accentColor = StudioAmber
                )

                Spacer(modifier = Modifier.height(8.dp))

                DSPKnobControl(
                    title = "Layer 2 Attack Delay",
                    valueText = "${spec.layer2AttackDelayMs} ms",
                    valueProgress = spec.layer2AttackDelayMs / 50f,
                    onValueChange = { delay ->
                        val delayMs = (delay * 50).toInt().coerceIn(0, 50)
                        viewModel.updateActivePreset(spec.copy(layer2AttackDelayMs = delayMs))
                    },
                    accentColor = StudioCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Split Mode Engine Rack Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CallSplit, contentDescription = null, tint = StudioAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("KEYBOARD SPLIT MODE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Switch(
                        checked = spec.isSplitModeEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateActivePreset(spec.copy(isSplitModeEnabled = enabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StudioAmber
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val splitNote = remember(spec.splitPointMidiNote) {
                    PianoKeyNote.fromMidi(spec.splitPointMidiNote)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Split Point Note:", fontSize = 12.sp, color = Gold80)
                    Surface(
                        color = SplitIndicatorC4,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${splitNote.noteName} (MIDI ${spec.splitPointMidiNote})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Slider(
                    value = (spec.splitPointMidiNote - 36) / 48f, // C2 (36) to C6 (84)
                    onValueChange = { norm ->
                        val midi = (36 + norm * 48).toInt().coerceIn(36, 84)
                        viewModel.updateActivePreset(spec.copy(splitPointMidiNote = midi))
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = SplitIndicatorC4,
                        activeTrackColor = SplitIndicatorC4,
                        inactiveTrackColor = MahoganyRack
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("LEFT ZONE (<${splitNote.noteName})", fontSize = 10.sp, color = StudioAmber, fontWeight = FontWeight.Bold)
                        Text(spec.leftZoneVoiceName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RIGHT ZONE (≥${splitNote.noteName})", fontSize = 10.sp, color = StudioCyan, fontWeight = FontWeight.Bold)
                        Text(spec.rightZoneVoiceName, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. DSP Effects Rack Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Equalizer, contentDescription = null, tint = StudioCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("DSP EFFECTS & RESONANCE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reverb
                DSPKnobControl(
                    title = "Reverb Wet Mix (${spec.reverbType})",
                    valueText = "${(spec.reverbWetMix * 100).toInt()}%",
                    valueProgress = spec.reverbWetMix / 0.5f,
                    onValueChange = { norm ->
                        viewModel.updateActivePreset(spec.copy(reverbWetMix = norm * 0.5f))
                    },
                    accentColor = StudioCyan
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chorus (Layer 2)
                DSPKnobControl(
                    title = "Layer 2 Chorus Depth",
                    valueText = "${(spec.chorusDepth * 100).toInt()}%",
                    valueProgress = spec.chorusDepth / 0.4f,
                    onValueChange = { norm ->
                        viewModel.updateActivePreset(spec.copy(chorusDepth = norm * 0.4f))
                    },
                    accentColor = Gold60
                )

                Spacer(modifier = Modifier.height(10.dp))

                // EQ Boost
                DSPKnobControl(
                    title = "EQ 2-5kHz Clarity Boost",
                    valueText = "+${"%.1f".format(spec.eqBoostDb)} dB",
                    valueProgress = spec.eqBoostDb / 6.0f,
                    onValueChange = { norm ->
                        viewModel.updateActivePreset(spec.copy(eqBoostDb = norm * 6.0f))
                    },
                    accentColor = StudioAmber
                )

                Spacer(modifier = Modifier.height(12.dp))

                // String Resonance Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sustain String Resonance", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Simulates sympathy harmonics on un-damped piano strings", fontSize = 10.sp, color = Color(0xFFB0A498))
                    }

                    Switch(
                        checked = spec.stringResonanceSimulation,
                        onCheckedChange = { enabled ->
                            viewModel.updateActivePreset(spec.copy(stringResonanceSimulation = enabled))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StudioAmber
                        )
                    )
                }
            }
        }
    }
}
