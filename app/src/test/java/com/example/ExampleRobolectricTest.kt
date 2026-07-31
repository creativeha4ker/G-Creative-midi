package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.FactoryPresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Concert Grand", appName)
  }

  @Test
  fun `verify steinway c7 exact specs`() {
    val preset = FactoryPresets.STEINWAY_YAMAHA_C7
    assertEquals("Concert Grand (Steinway/Yamaha C7)", preset.name)
    assertTrue(preset.isDualLayerEnabled)
    assertEquals(1.0f, preset.layer1Volume)
    assertEquals(0.35f, preset.layer2Volume)
    assertEquals(15, preset.layer2AttackDelayMs)
    assertTrue(preset.isSplitModeEnabled)
    assertEquals(60, preset.splitPointMidiNote)
    assertEquals(0.225f, preset.reverbWetMix)
    assertEquals(0.15f, preset.chorusDepth)
    assertEquals(3.0f, preset.eqBoostDb)
  }
}
