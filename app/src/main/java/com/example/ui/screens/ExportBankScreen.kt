package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PresetSpec
import com.example.ui.theme.*
import com.example.ui.viewmodel.PresetViewModel

@Composable
fun ExportBankScreen(
    viewModel: PresetViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val activeSpec by viewModel.activePreset.collectAsState()
    val allPresets by viewModel.allPresets.collectAsState()
    val statusMessage by viewModel.exportStatusMessage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MahoganyDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section Title
        Text(
            text = "PRESET BANK & EXPORT MANAGER",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = StudioAmber
        )
        Text(
            text = "Presets Bank & File Exports",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status Banner
        statusMessage?.let { msg ->
            Surface(
                color = Color(0xFF1E3A2B),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2E6B4B), RoundedCornerShape(10.dp))
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StudioCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(onClick = { viewModel.clearStatusMessage() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        // Export Actions Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ACTIVE PRESET EXPORT OPTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold80
                )
                Text(
                    text = activeSpec.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportPresetJsonFile(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_json_preset_btn")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("EXPORT JSON SPEC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.export24BitAudioWav(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_wav_preset_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("24-BIT WAV RENDER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Library Bank
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FACTORY & USER PRESETS BANK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioAmber
                )

                Spacer(modifier = Modifier.height(12.dp))

                allPresets.forEach { preset ->
                    val isSelected = preset.id == activeSpec.id

                    Surface(
                        color = if (isSelected) Color(0xFF382A15) else MahoganyRack,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                1.dp,
                                if (isSelected) Gold60 else MahoganyBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.selectPreset(preset) }
                            .testTag("preset_bank_item_${preset.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = preset.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Gold80 else Color.White
                                )
                                Text(
                                    text = preset.instrumentType,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB0A498)
                                )
                            }

                            if (isSelected) {
                                Surface(
                                    color = BrassAccent,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // MIDI CC & Sysex Reference Table
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MIDI CC & SYSTEM EXCLUSIVE MAPPING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold80
                )

                Spacer(modifier = Modifier.height(8.dp))

                MidiCcRow("Bank Select MSB / LSB", "CC00 / CC32 = 0x01 / 0x07")
                MidiCcRow("Program Change", "PC = 0 (Concert Grand C7)")
                MidiCcRow("Sustain Pedal", "CC64 (Damper 0-127)")
                MidiCcRow("Layer 2 Volume", "CC11 (Expression Control)")
                MidiCcRow("Reverb Wet Level", "CC91 (Effects 1 Depth)")
                MidiCcRow("Chorus Depth", "CC93 (Effects 3 Depth)")
            }
        }
    }
}

@Composable
private fun MidiCcRow(label: String, cc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFFC0B8AE))
        Text(cc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StudioCyan)
    }
}
