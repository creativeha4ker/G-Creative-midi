package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class PresetSpec(
    @PrimaryKey val id: String = "steinway_c7_concert_grand",
    val name: String = "Concert Grand (Steinway/Yamaha C7)",
    val instrumentType: String = "Concert Grand Piano (Steinway/Yamaha C7 tonal character)",
    
    // Layer Mode
    val isDualLayerEnabled: Boolean = true,
    val layer1Name: String = "Acoustic Grand Piano",
    val layer1Volume: Float = 1.0f, // 100%
    val layer1MinVelocity: Int = 0,
    val layer1MaxVelocity: Int = 127,
    
    val layer2Name: String = "Warm Electric Piano / String Pad",
    val layer2Volume: Float = 0.35f, // 35%
    val layer2AttackDelayMs: Int = 15, // 15ms
    
    // Split Mode
    val isSplitModeEnabled: Boolean = true,
    val splitPointMidiNote: Int = 60, // C4 (Middle C)
    val leftZoneVoiceName: String = "Bass Piano / Upright Bass tone",
    val leftZoneMinNote: Int = 21, // A0
    val leftZoneMaxNote: Int = 59, // B3
    val rightZoneVoiceName: String = "Grand Piano lead tone",
    val rightZoneMinNote: Int = 60, // C4
    val rightZoneMaxNote: Int = 108, // C8
    
    // Dynamics
    val velocitySensitivityProfile: String = "Full range, realistic hammer-strike curve",
    val softTouchDescription: String = "Soft touch (vel 1-50): mellow, warm tone",
    val hardTouchDescription: String = "Hard touch (vel 90-127): bright, powerful tone",
    val velocityCurveExponent: Float = 1.25f, // Realistic hammer strike curve
    
    // Effects
    val reverbType: String = "Hall",
    val reverbWetMix: Float = 0.225f, // 20-25% wet mix (22.5%)
    val chorusEnabledOnLayer2Only: Boolean = true,
    val chorusDepth: Float = 0.15f, // 15% subtle depth
    val eqBoostDb: Float = 3.0f, // +3dB boost
    val eqFrequencyRange: String = "2-5kHz clarity boost",
    val sustainPedalEnabled: Boolean = true,
    val stringResonanceSimulation: Boolean = true,
    val stringResonanceDecaySeconds: Float = 2.5f,
    
    // Output Specs
    val sampleRateHz: Int = 44100,
    val bitDepth: Int = 24,
    val isStereo: Boolean = true,
    
    val isFactoryPreset: Boolean = true,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

object FactoryPresets {
    val STEINWAY_YAMAHA_C7 = PresetSpec()
    
    val POP_GRAND_BRIGHT = PresetSpec(
        id = "bright_pop_grand",
        name = "Studio Bright Grand",
        instrumentType = "Yamaha C7 Bright Studio",
        isDualLayerEnabled = false,
        layer1Volume = 1.0f,
        isSplitModeEnabled = false,
        reverbWetMix = 0.15f,
        eqBoostDb = 4.5f,
        isFactoryPreset = true
    )
    
    val AMBIENT_PAD_PIANO = PresetSpec(
        id = "ambient_pad_piano",
        name = "Celestial Pad & Steinway",
        instrumentType = "Steinway D Concert & Lush Pad",
        isDualLayerEnabled = true,
        layer2Volume = 0.55f,
        layer2AttackDelayMs = 40,
        reverbWetMix = 0.40f,
        chorusDepth = 0.30f,
        isFactoryPreset = true
    )
    
    val JAZZ_TRIO_SPLIT = PresetSpec(
        id = "jazz_trio_split",
        name = "Acoustic Jazz Split",
        instrumentType = "Upright Acoustic Bass / C7 Lead",
        isDualLayerEnabled = false,
        isSplitModeEnabled = true,
        splitPointMidiNote = 60,
        leftZoneVoiceName = "Acoustic Pizzicato Double Bass",
        rightZoneVoiceName = "Steinway C7 Warm Jazz",
        reverbWetMix = 0.18f,
        isFactoryPreset = true
    )
    
    val ALL_FACTORY_PRESETS = listOf(STEINWAY_YAMAHA_C7, POP_GRAND_BRIGHT, AMBIENT_PAD_PIANO, JAZZ_TRIO_SPLIT)
}
