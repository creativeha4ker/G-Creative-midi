package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.model.PianoKeyNote
import com.example.model.PresetSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.*

class PianoSynthesizerEngine {

    private val sampleRate = 44100
    private var audioTrack: AudioTrack? = null
    private var isEngineRunning = false
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    var currentSpec: PresetSpec = PresetSpec()
        private set

    private var isSustainPedalPressed = false
    private val activeNoteVelocities = ConcurrentHashMap<Int, ActiveVoice>()

    // State flow for live visualizer waveform amplitude & spectrum
    private val _liveAudioAmplitudes = MutableStateFlow(FloatArray(32) { 0f })
    val liveAudioAmplitudes: StateFlow<FloatArray> = _liveAudioAmplitudes.asStateFlow()

    private val _isPlayingDemo = MutableStateFlow(false)
    val isPlayingDemo: StateFlow<Boolean> = _isPlayingDemo.asStateFlow()

    private class ActiveVoice(
        val midiNote: Int,
        val velocity: Int, // 1 - 127
        val startTimeMs: Long,
        var isReleased: Boolean = false,
        var releaseTimeMs: Long = 0L
    )

    fun startEngine() {
        if (isEngineRunning) return
        try {
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufferSize * 2, 4096)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isEngineRunning = true

            engineScope.launch {
                renderAudioLoop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopEngine() {
        isEngineRunning = false
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioTrack = null
    }

    fun updateSpec(spec: PresetSpec) {
        this.currentSpec = spec
    }

    fun setSustainPedal(pressed: Boolean) {
        this.isSustainPedalPressed = pressed
        if (!pressed) {
            // Damp notes that were released while sustain was active
            val now = System.currentTimeMillis()
            activeNoteVelocities.values.forEach { voice ->
                if (voice.isReleased && voice.releaseTimeMs == 0L) {
                    voice.releaseTimeMs = now
                }
            }
        }
    }

    fun triggerNoteOn(midiNote: Int, velocity: Int = 100) {
        val voice = ActiveVoice(
            midiNote = midiNote,
            velocity = velocity.coerceIn(1, 127),
            startTimeMs = System.currentTimeMillis()
        )
        activeNoteVelocities[midiNote] = voice
    }

    fun triggerNoteOff(midiNote: Int) {
        val voice = activeNoteVelocities[midiNote] ?: return
        if (isSustainPedalPressed && currentSpec.sustainPedalEnabled) {
            voice.isReleased = true
            // Will release when pedal is un-pressed
        } else {
            voice.isReleased = true
            voice.releaseTimeMs = System.currentTimeMillis()
        }
    }

    fun playDemoSequence(onComplete: () -> Unit = {}) {
        if (_isPlayingDemo.value) return
        _isPlayingDemo.value = true

        engineScope.launch {
            // A rich chord progression highlighting C4 split, dual layer, Steinway C7 lead and sustain resonance
            // C3 (Bass split), G3 (Bass split), C4 (Split boundary), E4 (Lead Grand + Layer 2), G4, C5, E5
            val demoNotes = listOf(
                Pair(48, 100), // C3 Bass
                Pair(55, 95),  // G3 Bass
                Pair(60, 110), // C4 Middle C
                Pair(64, 105), // E4
                Pair(67, 108), // G4
                Pair(72, 115), // C5
                Pair(76, 120), // E5
                Pair(79, 125)  // G5
            )

            setSustainPedal(true)

            for ((note, vel) in demoNotes) {
                triggerNoteOn(note, vel)
                delay(180)
            }

            delay(1200)

            // Play arpeggiated melodic lead to demonstrate velocity dynamics (soft vs hard)
            val softNotes = listOf(Pair(72, 35), Pair(74, 42), Pair(76, 48))
            val hardNotes = listOf(Pair(79, 98), Pair(81, 115), Pair(84, 127))

            for ((note, vel) in softNotes) {
                triggerNoteOn(note, vel)
                delay(220)
                triggerNoteOff(note)
            }

            delay(300)

            for ((note, vel) in hardNotes) {
                triggerNoteOn(note, vel)
                delay(240)
                triggerNoteOff(note)
            }

            delay(2000)
            setSustainPedal(false)
            activeNoteVelocities.clear()

            _isPlayingDemo.value = false
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    private suspend fun renderAudioLoop() = withContext(Dispatchers.Default) {
        val bufferFrames = 512
        val shortBuffer = ShortArray(bufferFrames * 2) // Stereo: Left & Right

        // DSP Reverb Comb Filter Buffers
        val reverbDelaySamples = (sampleRate * 0.08).toInt() // 80ms hall delay
        val reverbBufferL = FloatArray(reverbDelaySamples)
        val reverbBufferR = FloatArray(reverbDelaySamples)
        var reverbIndex = 0

        var sampleTimeIndex = 0L

        val vizAmplitudes = FloatArray(32)

        while (isEngineRunning) {
            val spec = currentSpec
            val now = System.currentTimeMillis()

            var framePeak = 0.0f

            for (i in 0 until bufferFrames) {
                val timeInSec = (sampleTimeIndex + i).toDouble() / sampleRate
                var leftSample = 0.0f
                var rightSample = 0.0f

                // Clean up dead voices
                val iterator = activeNoteVelocities.entries.iterator()
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    val voice = entry.value
                    val ageMs = now - voice.startTimeMs
                    val ageSec = ageMs / 1000.0

                    if (voice.isReleased && voice.releaseTimeMs > 0L) {
                        val releaseAgeSec = (now - voice.releaseTimeMs) / 1000.0
                        if (releaseAgeSec > 1.2) { // Voice fully faded out
                            iterator.remove()
                            continue
                        }
                    }

                    // Calculate Envelope (ADSR)
                    val noteKey = PianoKeyNote.fromMidi(voice.midiNote)
                    val freq = noteKey.frequencyHz.toDouble()
                    val normVel = voice.velocity / 127.0f

                    // Velocity curve: Soft touch (1-50 mellow), Hard touch (90-127 bright)
                    val hammerCurve = normVel.pow(spec.velocityCurveExponent)

                    // Release envelope
                    val envelope = if (voice.isReleased && voice.releaseTimeMs > 0L) {
                        val relSec = (now - voice.releaseTimeMs) / 1000.0
                        max(0.0, 1.0 - (relSec / 1.0)).pow(2.0)
                    } else {
                        // Sustain exponential decay
                        val decayRate = if (isSustainPedalPressed) 0.15 else 0.65
                        exp(-decayRate * ageSec)
                    }

                    if (envelope <= 0.001) continue

                    // Check Split Mode
                    val isLeftZone = spec.isSplitModeEnabled && voice.midiNote < spec.splitPointMidiNote

                    var voiceLeft = 0.0f
                    var voiceRight = 0.0f

                    if (isLeftZone) {
                        // Left Zone: Bass Piano / Upright Bass tone
                        val bassFreq = freq * 0.5 // Sub octave growl
                        val fundamental = sin(2.0 * PI * bassFreq * timeInSec)
                        val secondHarmonic = 0.5 * sin(2.0 * PI * freq * timeInSec)
                        val triangleBody = (2.0 / PI) * asin(sin(2.0 * PI * bassFreq * timeInSec))

                        val bassSignal = ((0.5 * fundamental + 0.3 * triangleBody + 0.2 * secondHarmonic) * envelope * hammerCurve).toFloat()
                        voiceLeft += bassSignal * 0.9f
                        voiceRight += bassSignal * 0.7f
                    } else {
                        // Right Zone / Full Range: Acoustic Grand Steinway C7
                        // Layer 1: Steinway C7 Acoustic Grand
                        val f1 = sin(2.0 * PI * freq * timeInSec)
                        val f2 = 0.5 * sin(2.0 * PI * (freq * 2.0) * timeInSec)
                        val f3 = 0.25 * sin(2.0 * PI * (freq * 3.0) * timeInSec)
                        val f4 = 0.12 * sin(2.0 * PI * (freq * 4.0) * timeInSec)
                        val f5 = 0.06 * sin(2.0 * PI * (freq * 5.0) * timeInSec)

                        // Brightness shift based on velocity
                        val brightness = if (voice.velocity >= 90) 1.5f else if (voice.velocity <= 50) 0.6f else 1.0f
                        val layer1Signal = ((f1 + f2 * brightness + f3 * brightness + f4 + f5) * 0.4 * envelope * hammerCurve * spec.layer1Volume).toFloat()

                        voiceLeft += layer1Signal
                        voiceRight += layer1Signal

                        // Layer 2: Warm Electric Piano / String Pad (35% volume default, 15ms delay)
                        if (spec.isDualLayerEnabled && ageMs >= spec.layer2AttackDelayMs) {
                            val epAgeSec = (ageMs - spec.layer2AttackDelayMs) / 1000.0
                            val epAttackEnvelope = min(1.0, epAgeSec / 0.12) * envelope // Smooth 120ms attack swell
                            
                            // Chorus LFO effect on Layer 2 (15% depth default)
                            val chorusLfo = if (spec.chorusEnabledOnLayer2Only) {
                                1.0 + spec.chorusDepth * sin(2.0 * PI * 1.5 * timeInSec)
                            } else 1.0

                            val epWave = (sin(2.0 * PI * freq * chorusLfo * timeInSec) + 0.3 * sin(2.0 * PI * freq * 2.01 * timeInSec)).toFloat()
                            val layer2Signal = (epWave * 0.25f * epAttackEnvelope.toFloat() * spec.layer2Volume)

                            voiceLeft += layer2Signal * 0.85f
                            voiceRight += layer2Signal * 1.15f // Subtle stereo spread for EP/Pad
                        }

                        // String Resonance Simulation
                        if (spec.stringResonanceSimulation && isSustainPedalPressed) {
                            val resonanceHarmonic = 0.04f * sin(2.0 * PI * (freq * 1.5) * timeInSec).toFloat() * envelope.toFloat()
                            voiceLeft += resonanceHarmonic
                            voiceRight += resonanceHarmonic
                        }
                    }

                    leftSample += voiceLeft
                    rightSample += voiceRight
                }

                // EQ High-Mid Clarity Boost (+3dB at 2-5kHz)
                if (spec.eqBoostDb > 0.0f) {
                    val eqGain = 1.0f + (spec.eqBoostDb / 20.0f) // +3dB boost scaling
                    leftSample *= eqGain
                    rightSample *= eqGain
                }

                // DSP Hall Reverb (22.5% wet mix)
                val reverbWet = spec.reverbWetMix.coerceIn(0f, 0.8f)
                if (reverbWet > 0f) {
                    val delayedL = reverbBufferL[reverbIndex]
                    val delayedR = reverbBufferR[reverbIndex]

                    reverbBufferL[reverbIndex] = leftSample + delayedL * 0.45f
                    reverbBufferR[reverbIndex] = rightSample + delayedR * 0.45f

                    leftSample = leftSample * (1.0f - reverbWet) + delayedL * reverbWet
                    rightSample = rightSample * (1.0f - reverbWet) + delayedR * reverbWet

                    reverbIndex = (reverbIndex + 1) % reverbDelaySamples
                }

                // Master Soft Clipper
                leftSample = leftSample.coerceIn(-0.98f, 0.98f)
                rightSample = rightSample.coerceIn(-0.98f, 0.98f)

                val shortL = (leftSample * 32767.0f).toInt().toShort()
                val shortR = (rightSample * 32767.0f).toInt().toShort()

                shortBuffer[i * 2] = shortL
                shortBuffer[i * 2 + 1] = shortR

                framePeak = max(framePeak, max(abs(leftSample), abs(rightSample)))
            }

            // Write PCM buffer to AudioTrack
            audioTrack?.write(shortBuffer, 0, shortBuffer.size)
            sampleTimeIndex += bufferFrames

            // Update spectrum visualizer bars
            if (sampleTimeIndex % (bufferFrames * 2) == 0L) {
                for (b in vizAmplitudes.indices) {
                    val targetAmp = (framePeak * sin((b + 1) * 0.2 + sampleTimeIndex * 0.001).absoluteValue).coerceIn(0.05, 1.0).toFloat()
                    vizAmplitudes[b] = vizAmplitudes[b] * 0.6f + targetAmp * 0.4f
                }
                _liveAudioAmplitudes.value = vizAmplitudes.copyOf()
            }
        }
    }
}
