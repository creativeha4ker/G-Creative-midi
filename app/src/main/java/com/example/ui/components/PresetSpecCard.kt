package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PresetSpec
import com.example.ui.theme.*

@Composable
fun PresetSpecCard(
    spec: PresetSpec,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONCERT GRAND SOUND PRESET",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioAmber
                    )
                    Text(
                        text = spec.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Surface(
                    color = Color(0xFF3B2E0C),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, Gold60, RoundedCornerShape(20.dp))
                ) {
                    Text(
                        text = "Steinway/Yamaha C7",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold80,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MahoganyBorder
            )

            // Section 1: Sound Engine Settings
            SpecSectionHeader("SOUND ENGINE SETTINGS", Icons.Default.Tune)
            
            SpecRowItem(
                label = "Layer Mode",
                value = if (spec.isDualLayerEnabled) "Dual Layer (2 Sounds Combined)" else "Single Layer",
                detail = "Layer 1: ${spec.layer1Name} (${(spec.layer1Volume * 100).toInt()}%, Vel ${spec.layer1MinVelocity}-${spec.layer1MaxVelocity})\nLayer 2: ${spec.layer2Name} (${(spec.layer2Volume * 100).toInt()}%, Attack Delay ${spec.layer2AttackDelayMs}ms)"
            )

            SpecRowItem(
                label = "Split Mode",
                value = if (spec.isSplitModeEnabled) "Enabled @ C4 (Middle C / Note ${spec.splitPointMidiNote})" else "Disabled",
                detail = "Left Zone (<C4): ${spec.leftZoneVoiceName}\nRight Zone (≥C4): ${spec.rightZoneVoiceName}"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MahoganyBorder
            )

            // Section 2: Dynamics & Hammer Response
            SpecSectionHeader("DYNAMICS & TOUCH SENSITIVITY", Icons.Default.Speed)

            SpecRowItem(
                label = "Velocity Curve",
                value = spec.velocitySensitivityProfile,
                detail = "${spec.softTouchDescription}\n${spec.hardTouchDescription}"
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MahoganyBorder
            )

            // Section 3: Effects
            SpecSectionHeader("EFFECTS & RESONANCE RACK", Icons.Default.Equalizer)

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Reverb", fontSize = 11.sp, color = Gold80)
                    Text("${spec.reverbType} (${(spec.reverbWetMix * 100).toInt()}% wet)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Chorus", fontSize = 11.sp, color = Gold80)
                    Text("Layer 2 (${(spec.chorusDepth * 100).toInt()}% depth)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("EQ Boost", fontSize = 11.sp, color = Gold80)
                    Text("+${spec.eqBoostDb}dB @ ${spec.eqFrequencyRange}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StudioCyan)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Sustain Pedal", fontSize = 11.sp, color = Gold80)
                    Text("Enabled + String Resonance", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MahoganyBorder
            )

            // Section 4: Output Format
            SpecSectionHeader("OUTPUT FORMAT", Icons.Default.GraphicEq)

            Text(
                text = "MIDI-Compatible Instrument Preset, Stereo, ${spec.sampleRateHz}Hz / ${spec.bitDepth}-bit",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioAmber
            )
        }
    }
}

@Composable
private fun SpecSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = StudioAmber,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StudioAmber
        )
    }
}

@Composable
private fun SpecRowItem(label: String, value: String, detail: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, color = Gold80, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Text(
            text = detail,
            fontSize = 11.sp,
            color = Color(0xFFB0A498),
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
