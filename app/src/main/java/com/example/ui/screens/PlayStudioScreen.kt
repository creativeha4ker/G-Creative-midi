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
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.PlayablePianoKeyboard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PresetViewModel

@Composable
fun PlayStudioScreen(
    viewModel: PresetViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val spec by viewModel.activePreset.collectAsState()
    val isSustainPressed by viewModel.isSustainPressed.collectAsState()
    val amplitudes by viewModel.synthEngine.liveAudioAmplitudes.collectAsState()
    val isDemoActive by viewModel.synthEngine.isPlayingDemo.collectAsState()

    var activeOctaveOffset by remember { mutableIntStateOf(0) } // -12, 0, +12
    var lastTappedNoteInfo by remember { mutableStateOf<String?>(null) }

    val startNote = 48 + activeOctaveOffset // C3 default
    val endNote = 76 + activeOctaveOffset   // E5 default

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MahoganyDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Room Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LIVE PERFORMANCE STUDIO",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioCyan
                )
                Text(
                    text = spec.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            // Octave Shift Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { if (activeOctaveOffset > -12) activeOctaveOffset -= 12 },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MahoganyRack)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Octave Down", tint = Gold80)
                }

                Text(
                    text = when(activeOctaveOffset) {
                        -12 -> "OCT -1"
                        12 -> "OCT +1"
                        else -> "OCT 0"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioAmber,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                IconButton(
                    onClick = { if (activeOctaveOffset < 12) activeOctaveOffset += 12 },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MahoganyRack)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Octave Up", tint = Gold80)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Audio Visualizer
        AudioWaveformVisualizer(
            amplitudes = amplitudes,
            isDemoActive = isDemoActive
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Touch Velocity Dynamic Tester Pad
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HAMMER STRIKE DYNAMICS TESTER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold80
                    )

                    lastTappedNoteInfo?.let { info ->
                        Text(info, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudioCyan)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val midi = 60 // C4
                            val vel = 35  // Soft Touch
                            viewModel.triggerNoteOn(midi, vel)
                            lastTappedNoteInfo = "C4 Soft Touch (Vel $vel)"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E241B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("soft_touch_test_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SOFT TOUCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Gold80)
                            Text("Vel 1-50 (Mellow Warm)", fontSize = 9.sp, color = Color(0xFFB0A498))
                        }
                    }

                    Button(
                        onClick = {
                            val midi = 60 // C4
                            val vel = 115 // Hard Touch
                            viewModel.triggerNoteOn(midi, vel)
                            lastTappedNoteInfo = "C4 Hard Strike (Vel $vel)"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3410)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hard_touch_test_btn")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HARD STRIKE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrassAccent)
                            Text("Vel 90-127 (Bright Power)", fontSize = 9.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Keybed
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "INTERACTIVE CONCERT KEYBOARD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                PlayablePianoKeyboard(
                    spec = spec,
                    isSustainPressed = isSustainPressed,
                    onNoteDown = { note, vel ->
                        viewModel.triggerNoteOn(note, vel)
                        lastTappedNoteInfo = "Note $note | Vel $vel"
                    },
                    onNoteUp = { note -> viewModel.triggerNoteOff(note) },
                    onSustainToggle = { viewModel.toggleSustainPedal() },
                    startMidiNote = startNote,
                    endMidiNote = endNote
                )
            }
        }
    }
}
