package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PianoKeyNote
import com.example.model.PresetSpec
import com.example.ui.theme.*

@Composable
fun PlayablePianoKeyboard(
    spec: PresetSpec,
    isSustainPressed: Boolean,
    onNoteDown: (midiNote: Int, velocity: Int) -> Unit,
    onNoteUp: (midiNote: Int) -> Unit,
    onSustainToggle: () -> Unit,
    modifier: Modifier = Modifier,
    startMidiNote: Int = 48, // C3
    endMidiNote: Int = 76    // E5
) {
    val scrollState = rememberScrollState()
    val allNotes = remember(startMidiNote, endMidiNote) {
        PianoKeyNote.generateRange(startMidiNote, endMidiNote)
    }

    val whiteNotes = remember(allNotes) { allNotes.filter { !it.isBlackKey } }
    val activeNotes = remember { mutableStateMapOf<Int, Int>() } // MIDI note to velocity

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MahoganyDark)
            .padding(8.dp)
    ) {
        // Keyboard Control Header: Zone Labels & Sustain Pedal Toggle Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Zone mapping status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (spec.isSplitModeEnabled) {
                    Surface(
                        color = Color(0xFF382318),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(1.dp, MahoganyBorder, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "LEFT (<C4): ${spec.leftZoneVoiceName.take(16)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(" | ", color = MahoganyBorder)

                    Surface(
                        color = Color(0xFF1E2F26),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.border(1.dp, Color(0xFF2E5E44), RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "RIGHT (≥C4): ${spec.rightZoneVoiceName.take(16)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Full Keyboard: ${spec.layer1Name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gold80
                    )
                }
            }

            // Sustain Pedal Toggle Button
            Button(
                onClick = onSustainToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSustainPressed) BrassAccent else MahoganyRack,
                    contentColor = if (isSustainPressed) Color.Black else Gold80
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("sustain_pedal_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Sustain Pedal",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSustainPressed) "PEDAL DOWN" else "SUSTAIN PEDAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Piano Key Bed Canvas with Scrollable Row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, MahoganyBorder, RoundedCornerShape(12.dp))
                .background(Color(0xFF0F0B09))
                .horizontalScroll(scrollState)
        ) {
            val keyWidthPx = 54f
            val keyHeightPx = 180f
            val blackKeyWidthPx = 32f
            val blackKeyHeightPx = 110f

            val totalWidthDp = (whiteNotes.size * 54).dp

            Box(
                modifier = Modifier
                    .width(totalWidthDp)
                    .fillMaxHeight()
                    .pointerInput(allNotes, spec) {
                        detectTapGestures(
                            onPress = { offset ->
                                // Calculate tapped note and velocity by touch position (Y position = velocity force)
                                val x = offset.x
                                val y = offset.y

                                // Velocity sensitivity profile (Soft touch upper key 1-50, Hard touch bottom 90-127)
                                val normY = (y / keyHeightPx).coerceIn(0f, 1f)
                                val tapVelocity = (30 + normY * 97).toInt()

                                // Check black keys first (overlapping on top)
                                var struckNote: PianoKeyNote? = null

                                var currX = 0f
                                for (wNote in whiteNotes) {
                                    // Check if black key exists to right or left
                                    val leftBlackMidi = wNote.midiNote - 1
                                    val rightBlackMidi = wNote.midiNote + 1

                                    if (y <= blackKeyHeightPx) {
                                        // Left black key check
                                        val leftBlack = allNotes.find { it.midiNote == leftBlackMidi && it.isBlackKey }
                                        if (leftBlack != null) {
                                            val bX = currX - (blackKeyWidthPx / 2)
                                            if (x >= bX && x <= bX + blackKeyWidthPx) {
                                                struckNote = leftBlack
                                                break
                                            }
                                        }

                                        // Right black key check
                                        val rightBlack = allNotes.find { it.midiNote == rightBlackMidi && it.isBlackKey }
                                        if (rightBlack != null) {
                                            val bX = currX + keyWidthPx - (blackKeyWidthPx / 2)
                                            if (x >= bX && x <= bX + blackKeyWidthPx) {
                                                struckNote = rightBlack
                                                break
                                            }
                                        }
                                    }

                                    if (x >= currX && x < currX + keyWidthPx) {
                                        struckNote = wNote
                                        break
                                    }
                                    currX += keyWidthPx
                                }

                                struckNote?.let { note ->
                                    activeNotes[note.midiNote] = tapVelocity
                                    onNoteDown(note.midiNote, tapVelocity)
                                    tryAwaitRelease()
                                    activeNotes.remove(note.midiNote)
                                    onNoteUp(note.midiNote)
                                }
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    var currentX = 0f

                    // 1. Draw White Keys
                    whiteNotes.forEach { note ->
                        val isPressed = activeNotes.containsKey(note.midiNote)
                        val isSplitBoundary = spec.isSplitModeEnabled && note.midiNote == spec.splitPointMidiNote
                        val isLeftZone = spec.isSplitModeEnabled && note.midiNote < spec.splitPointMidiNote

                        // Base White Key Color
                        val keyColor = if (isPressed) {
                            Gold80
                        } else if (isLeftZone) {
                            Color(0xFFF0EAE1) // Subtle warm tint for left bass zone
                        } else {
                            IvoryKey
                        }

                        // Key Body
                        drawRect(
                            color = keyColor,
                            topLeft = Offset(currentX + 1f, 0f),
                            size = Size(keyWidthPx - 2f, keyHeightPx)
                        )

                        // Key Border / Shadow
                        drawRect(
                            color = Color(0xFFC0B8AE),
                            topLeft = Offset(currentX + 1f, keyHeightPx - 8f),
                            size = Size(keyWidthPx - 2f, 8f)
                        )

                        drawRect(
                            color = Color(0xFF261D18),
                            topLeft = Offset(currentX, 0f),
                            size = Size(1f, keyHeightPx)
                        )

                        // Draw C4 Split Marker Line
                        if (isSplitBoundary) {
                            drawRect(
                                color = SplitIndicatorC4,
                                topLeft = Offset(currentX, 0f),
                                size = Size(4f, keyHeightPx)
                            )
                        }

                        currentX += keyWidthPx
                    }

                    // 2. Draw Black Keys
                    currentX = 0f
                    whiteNotes.forEach { whiteNote ->
                        val rightBlackMidi = whiteNote.midiNote + 1
                        val blackNote = allNotes.find { it.midiNote == rightBlackMidi && it.isBlackKey }

                        if (blackNote != null) {
                            val blackX = currentX + keyWidthPx - (blackKeyWidthPx / 2)
                            val isPressed = activeNotes.containsKey(blackNote.midiNote)
                            val isLeftZone = spec.isSplitModeEnabled && blackNote.midiNote < spec.splitPointMidiNote

                            val keyColor = if (isPressed) {
                                StudioAmber
                            } else {
                                EbonyKey
                            }

                            drawRect(
                                color = keyColor,
                                topLeft = Offset(blackX, 0f),
                                size = Size(blackKeyWidthPx, blackKeyHeightPx)
                            )

                            // Top Bevel
                            drawRect(
                                color = if (isPressed) StudioAmber else Color(0xFF38322E),
                                topLeft = Offset(blackX + 2f, 0f),
                                size = Size(blackKeyWidthPx - 4f, blackKeyHeightPx - 6f)
                            )
                        }
                        currentX += keyWidthPx
                    }
                }

                // Overlay Note Labels (e.g., C3, C4, C5)
                Row(modifier = Modifier.fillMaxSize()) {
                    whiteNotes.forEach { note ->
                        Box(
                            modifier = Modifier
                                .width(54.dp)
                                .fillMaxHeight()
                                .padding(bottom = 12.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (note.noteName.startsWith("C")) {
                                Surface(
                                    color = if (note.midiNote == spec.splitPointMidiNote) SplitIndicatorC4 else MahoganyRack,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = note.noteName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
