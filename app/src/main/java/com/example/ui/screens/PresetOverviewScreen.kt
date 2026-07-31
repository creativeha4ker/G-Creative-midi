package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.PlayablePianoKeyboard
import com.example.ui.components.PresetSpecCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.PresetViewModel

@Composable
fun PresetOverviewScreen(
    viewModel: PresetViewModel,
    onNavigateToEngineControls: () -> Unit,
    onNavigateToPlayStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val activeSpec by viewModel.activePreset.collectAsState()
    val isSustainPressed by viewModel.isSustainPressed.collectAsState()
    val isDemoActive by viewModel.synthEngine.isPlayingDemo.collectAsState()
    val amplitudes by viewModel.synthEngine.liveAudioAmplitudes.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MahoganyDark)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Hero Studio Banner Image with Gradient Overlay
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .border(1.dp, MahoganyBorder, RoundedCornerShape(16.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_studio_banner_1785519319377),
                    contentDescription = "Concert Hall Grand Piano",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xEC140F0C)),
                                startY = 0f,
                                endY = 400f
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = BrassAccent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "OFFICIAL PRESET SPECIFICATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Concert Grand Piano (Steinway/Yamaha C7)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.playDemoSequence() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioAmber,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                enabled = !isDemoActive,
                modifier = Modifier
                    .weight(1f)
                    .testTag("play_demo_arpeggio_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isDemoActive) "PLAYING DEMO..." else "AUDITION PRESET",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { viewModel.export24BitAudioWav(context) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold80),
                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Gold60)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_wav_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("EXPORT 24-BIT WAV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Audio Spectrum Visualizer
        AudioWaveformVisualizer(
            amplitudes = amplitudes,
            isDemoActive = isDemoActive
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Exact Preset Specifications Breakdown Card
        PresetSpecCard(spec = activeSpec)

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Keyboard Audition Pad
        Card(
            colors = CardDefaults.cardColors(containerColor = MahoganyPanel),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MahoganyBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE AUDITION KEYBED (C3 - E5)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gold80
                    )

                    TextButton(onClick = onNavigateToPlayStudio) {
                        Text("FULL STUDIO KEYBOARD", fontSize = 11.sp, color = StudioCyan, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StudioCyan, modifier = Modifier.size(16.dp))
                    }
                }

                PlayablePianoKeyboard(
                    spec = activeSpec,
                    isSustainPressed = isSustainPressed,
                    onNoteDown = { note, vel -> viewModel.triggerNoteOn(note, vel) },
                    onNoteUp = { note -> viewModel.triggerNoteOff(note) },
                    onSustainToggle = { viewModel.toggleSustainPedal() }
                )
            }
        }
    }
}
