package com.example.audio

import android.content.Context
import com.example.model.PresetSpec
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

object PresetExporter {

    fun exportPresetJson(context: Context, spec: PresetSpec): File {
        val fileName = "${spec.id}_preset_spec.json"
        val file = File(context.cacheDir, fileName)
        
        val jsonContent = """
        {
          "preset_name": "${spec.name}",
          "instrument_type": "${spec.instrumentType}",
          "output_format": {
            "sample_rate_hz": ${spec.sampleRateHz},
            "bit_depth": ${spec.bitDepth},
            "channels": "Stereo",
            "format": "MIDI-Compatible Instrument Preset File"
          },
          "sound_engine_settings": {
            "layer_mode": "${if (spec.isDualLayerEnabled) "Dual Layer (2 Sounds Combined)" else "Single Layer"}",
            "layer_1": {
              "name": "${spec.layer1Name}",
              "volume_percentage": ${(spec.layer1Volume * 100).toInt()}%,
              "velocity_range": "${spec.layer1MinVelocity}-${spec.layer1MaxVelocity}"
            },
            "layer_2": {
              "name": "${spec.layer2Name}",
              "volume_percentage": ${(spec.layer2Volume * 100).toInt()}%,
              "attack_delay_ms": ${spec.layer2AttackDelayMs}ms
            },
            "split_mode": {
              "enabled": ${spec.isSplitModeEnabled},
              "split_point": "C4 (Middle C, MIDI Note ${spec.splitPointMidiNote})",
              "left_zone_below_c4": "${spec.leftZoneVoiceName}",
              "right_zone_c4_above": "${spec.rightZoneVoiceName}"
            }
          },
          "dynamics": {
            "velocity_sensitivity": "${spec.velocitySensitivityProfile}",
            "soft_touch": "${spec.softTouchDescription}",
            "hard_touch": "${spec.hardTouchDescription}",
            "curve_exponent": ${spec.velocityCurveExponent}
          },
          "effects": {
            "reverb": {
              "type": "${spec.reverbType}",
              "wet_mix": "${(spec.reverbWetMix * 100).toInt()}%"
            },
            "chorus": {
              "target": "Layer 2 Only",
              "depth": "${(spec.chorusDepth * 100).toInt()}%"
            },
            "eq": {
              "boost_db": "+${spec.eqBoostDb}dB",
              "frequency_range": "${spec.eqFrequencyRange}"
            },
            "sustain_pedal": {
              "enabled": ${spec.sustainPedalEnabled},
              "string_resonance_simulation": ${spec.stringResonanceSimulation}
            }
          }
        }
        """.trimIndent()

        file.writeText(jsonContent)
        return file
    }

    /**
     * Renders a 44.1kHz / 24-bit stereo WAV audio file for the Steinway C7 preset demo chord
     */
    fun renderPresetDemoWav24Bit(context: Context, spec: PresetSpec): File {
        val fileName = "${spec.id}_demo_24bit_44k.wav"
        val file = File(context.cacheDir, fileName)

        val sampleRate = 44100
        val durationSeconds = 3.5
        val totalFrames = (sampleRate * durationSeconds).toInt()
        val bytesPerSample = 3 // 24-bit = 3 bytes per channel
        val numChannels = 2 // Stereo
        val dataSize = totalFrames * numChannels * bytesPerSample

        val pcmData = ByteArray(dataSize)
        var byteIndex = 0

        // Notes: C3 (48), G3 (55), C4 (60), E4 (64), G4 (67)
        val chordNotes = listOf(48 to 0.8f, 55 to 0.7f, 60 to 1.0f, 64 to 0.9f, 67 to 0.95f)

        for (frame in 0 until totalFrames) {
            val t = frame.toDouble() / sampleRate
            var leftSample = 0.0f
            var rightSample = 0.0f

            val decay = exp(-0.8 * t) // Natural acoustic decay

            for ((midiNote, normVel) in chordNotes) {
                val freq = 440.0 * (2.0.pow((midiNote - 69) / 12.0))
                val isLeftZone = spec.isSplitModeEnabled && midiNote < spec.splitPointMidiNote

                if (isLeftZone) {
                    val bassWave = sin(2.0 * PI * (freq * 0.5) * t) + 0.4 * sin(2.0 * PI * freq * t)
                    val signal = (bassWave * 0.35 * normVel * decay).toFloat()
                    leftSample += signal
                    rightSample += signal * 0.8f
                } else {
                    val f1 = sin(2.0 * PI * freq * t)
                    val f2 = 0.5 * sin(2.0 * PI * (freq * 2.0) * t)
                    val f3 = 0.25 * sin(2.0 * PI * (freq * 3.0) * t)
                    val l1 = ((f1 + f2 + f3) * 0.35 * normVel * decay * spec.layer1Volume).toFloat()

                    leftSample += l1
                    rightSample += l1

                    if (spec.isDualLayerEnabled && t >= (spec.layer2AttackDelayMs / 1000.0)) {
                        val chorusLfo = 1.0 + spec.chorusDepth * sin(2.0 * PI * 1.5 * t)
                        val l2Wave = sin(2.0 * PI * freq * chorusLfo * t)
                        val l2 = (l2Wave * 0.2 * normVel * decay * spec.layer2Volume).toFloat()

                        leftSample += l2 * 0.85f
                        rightSample += l2 * 1.15f
                    }
                }
            }

            // EQ Boost (+3dB)
            if (spec.eqBoostDb > 0f) {
                val eqGain = 1.0f + (spec.eqBoostDb / 20.0f)
                leftSample *= eqGain
                rightSample *= eqGain
            }

            // Clip prevention
            leftSample = leftSample.coerceIn(-0.99f, 0.99f)
            rightSample = rightSample.coerceIn(-0.99f, 0.99f)

            // Convert Float (-1.0 to +1.0) to 24-bit Signed PCM Int (-8388608 to +8388607)
            val intL = (leftSample * 8388607.0f).toInt()
            val intR = (rightSample * 8388607.0f).toInt()

            // Write Left Channel (24-bit Little Endian)
            pcmData[byteIndex++] = (intL and 0xFF).toByte()
            pcmData[byteIndex++] = ((intL shr 8) and 0xFF).toByte()
            pcmData[byteIndex++] = ((intL shr 16) and 0xFF).toByte()

            // Write Right Channel (24-bit Little Endian)
            pcmData[byteIndex++] = (intR and 0xFF).toByte()
            pcmData[byteIndex++] = ((intR shr 8) and 0xFF).toByte()
            pcmData[byteIndex++] = ((intR shr 16) and 0xFF).toByte()
        }

        FileOutputStream(file).use { out ->
            val header = createWav24BitHeader(
                totalFrames = totalFrames,
                numChannels = numChannels,
                sampleRate = sampleRate,
                bitsPerSample = 24
            )
            out.write(header)
            out.write(pcmData)
        }

        return file
    }

    private fun createWav24BitHeader(
        totalFrames: Int,
        numChannels: Int,
        sampleRate: Int,
        bitsPerSample: Int
    ): ByteArray {
        val bytesPerSample = bitsPerSample / 8
        val blockAlign = numChannels * bytesPerSample
        val byteRate = sampleRate * blockAlign
        val dataSize = totalFrames * blockAlign
        val totalSize = 36 + dataSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)

        header.put("RIFF".toByteArray())
        header.putInt(totalSize)
        header.put("WAVE".toByteArray())

        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size for PCM
        header.putShort(1.toShort()) // AudioFormat 1 = PCM
        header.putShort(numChannels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign.toShort())
        header.putShort(bitsPerSample.toShort())

        header.put("data".toByteArray())
        header.putInt(dataSize)

        return header.array()
    }
}
