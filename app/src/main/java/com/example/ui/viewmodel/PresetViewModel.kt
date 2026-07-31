package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.PianoSynthesizerEngine
import com.example.audio.PresetExporter
import com.example.data.AppDatabase
import com.example.data.PresetRepository
import com.example.model.FactoryPresets
import com.example.model.PresetSpec
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

class PresetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PresetRepository
    val synthEngine = PianoSynthesizerEngine()

    val allPresets: StateFlow<List<PresetSpec>>

    private val _activePreset = MutableStateFlow<PresetSpec>(FactoryPresets.STEINWAY_YAMAHA_C7)
    val activePreset: StateFlow<PresetSpec> = _activePreset.asStateFlow()

    private val _isSustainPressed = MutableStateFlow(false)
    val isSustainPressed: StateFlow<Boolean> = _isSustainPressed.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow<String?>(null)
    val exportStatusMessage: StateFlow<String?> = _exportStatusMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PresetRepository(db.presetDao())

        allPresets = repository.allPresets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(FactoryPresets.STEINWAY_YAMAHA_C7)
        )

        synthEngine.startEngine()
        synthEngine.updateSpec(_activePreset.value)
    }

    override fun onCleared() {
        super.onCleared()
        synthEngine.stopEngine()
    }

    fun selectPreset(spec: PresetSpec) {
        _activePreset.value = spec
        synthEngine.updateSpec(spec)
    }

    fun updateActivePreset(updatedSpec: PresetSpec) {
        _activePreset.value = updatedSpec
        synthEngine.updateSpec(updatedSpec)
        viewModelScope.launch {
            repository.savePreset(updatedSpec)
        }
    }

    fun resetToFactoryDefault() {
        val currentId = _activePreset.value.id
        viewModelScope.launch {
            repository.resetFactoryPreset(currentId)
            val reset = repository.getPresetById(currentId)
            _activePreset.value = reset
            synthEngine.updateSpec(reset)
            _exportStatusMessage.value = "Reset to Factory Default specifications."
        }
    }

    fun triggerNoteOn(midiNote: Int, velocity: Int = 100) {
        synthEngine.triggerNoteOn(midiNote, velocity)
    }

    fun triggerNoteOff(midiNote: Int) {
        synthEngine.triggerNoteOff(midiNote)
    }

    fun toggleSustainPedal() {
        val newSustain = !_isSustainPressed.value
        _isSustainPressed.value = newSustain
        synthEngine.setSustainPedal(newSustain)
    }

    fun playDemoSequence() {
        synthEngine.playDemoSequence()
    }

    fun exportPresetJsonFile(context: Context) {
        viewModelScope.launch {
            try {
                val spec = _activePreset.value
                val file = PresetExporter.exportPresetJson(context, spec)
                shareFile(context, file, "application/json", "Share Preset Spec JSON")
                _exportStatusMessage.value = "Exported JSON Preset Spec: ${file.name}"
            } catch (e: Exception) {
                _exportStatusMessage.value = "Failed to export JSON: ${e.localizedMessage}"
            }
        }
    }

    fun export24BitAudioWav(context: Context) {
        viewModelScope.launch {
            try {
                _exportStatusMessage.value = "Rendering 44.1kHz / 24-bit Stereo Audio WAV..."
                val spec = _activePreset.value
                val file = PresetExporter.renderPresetDemoWav24Bit(context, spec)
                shareFile(context, file, "audio/wav", "Share 24-bit 44.1kHz PCM Audio")
                _exportStatusMessage.value = "Exported 24-bit 44.1kHz WAV: ${file.name}"
            } catch (e: Exception) {
                _exportStatusMessage.value = "Failed to render WAV: ${e.localizedMessage}"
            }
        }
    }

    fun clearStatusMessage() {
        _exportStatusMessage.value = null
    }

    private fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
