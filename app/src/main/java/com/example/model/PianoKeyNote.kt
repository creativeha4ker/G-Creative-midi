package com.example.model

import kotlin.math.pow

data class PianoKeyNote(
    val midiNote: Int,
    val noteName: String,
    val octave: Int,
    val isBlackKey: Boolean,
    val frequencyHz: Float
) {
    companion object {
        private val NOTE_NAMES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
        
        fun fromMidi(midiNote: Int): PianoKeyNote {
            val nameIndex = (midiNote % 12 + 12) % 12
            val octave = (midiNote / 12) - 1
            val baseName = NOTE_NAMES[nameIndex]
            val noteName = "$baseName$octave"
            val isBlack = baseName.contains("#")
            // A4 = MIDI note 69 = 440.0 Hz
            val frequency = 440.0f * (2.0f.pow((midiNote - 69) / 12.0f))
            return PianoKeyNote(midiNote, noteName, octave, isBlack, frequency)
        }
        
        fun generateRange(startMidi: Int = 36, endMidi: Int = 84): List<PianoKeyNote> { // C2 to C6 default view
            return (startMidi..endMidi).map { fromMidi(it) }
        }
    }
}
