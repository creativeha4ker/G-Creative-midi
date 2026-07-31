package com.example.data

import com.example.model.FactoryPresets
import com.example.model.PresetSpec
import kotlinx.coroutines.flow.Flow

class PresetRepository(private val presetDao: PresetDao) {

    val allPresets: Flow<List<PresetSpec>> = presetDao.getAllPresets()

    suspend fun getPresetById(id: String): PresetSpec {
        return presetDao.getPresetById(id) ?: FactoryPresets.STEINWAY_YAMAHA_C7
    }

    suspend fun savePreset(preset: PresetSpec) {
        val updatedPreset = preset.copy(
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        presetDao.insertOrUpdatePreset(updatedPreset)
    }

    suspend fun resetFactoryPreset(id: String) {
        val factory = FactoryPresets.ALL_FACTORY_PRESETS.find { it.id == id }
        if (factory != null) {
            presetDao.insertOrUpdatePreset(factory.copy(lastUpdatedTimestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deletePreset(id: String) {
        presetDao.deleteCustomPreset(id)
    }
}
